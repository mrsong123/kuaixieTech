package com.kuaixietech.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Configurable HTTPS IdP adapter. The configured server MUST implement the
 * documented request/response contract (see README_BATCH_13.md).
 * This bridge never invents a successful password change.
 */
@Service
@EnableConfigurationProperties(IdentityProviderSettings.class)
public class HttpIdentityProviderClient implements IdentityProviderClient {
    private final IdentityProviderSettings settings;
    private final ObjectMapper mapper;
    private final HttpClient client=HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER).build();
    public HttpIdentityProviderClient(IdentityProviderSettings settings,ObjectMapper mapper){
        this.settings=settings;this.mapper=mapper;
    }
    @Override public String resolveUserByLoginIdentifier(String loginIdentifier){
        JsonNode body=request("resolve-login",Map.of("loginIdentifier",loginIdentifier));
        if(!body.hasNonNull("result"))throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_BAD_RESPONSE");
        String result=body.get("result").asText();
        if("NOT_FOUND".equals(result))return null;
        if(!"OK".equals(result)||!body.hasNonNull("userId"))
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_BAD_RESPONSE");
        String id=body.get("userId").asText();
        if(!id.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_BAD_SUBJECT");
        return id;
    }
    @Override public Outcome changePassword(String userId,String oldSecret,String newSecret){
        return call("change-password",Map.of("userId",userId,"oldPassword",oldSecret,"newPassword",newSecret));
    }
    @Override public Outcome resetPassword(String userId,String newSecret,String operationId){
        return call("reset-password",Map.of("userId",userId,"newPassword",newSecret,"operationId",operationId));
    }
    @Override public Outcome rebindContact(String userId,String channel,String target,String loginIdentifier,String operationId){
        return call("rebind-contact",Map.of("userId",userId,"channel",channel,"target",target,"loginIdentifier",loginIdentifier,"operationId",operationId));
    }
    private Outcome call(String operation, Map<String,String> args){
        JsonNode body=request(operation,args);
        if(!body.hasNonNull("result"))
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_BAD_RESPONSE");
        try {return Outcome.valueOf(body.get("result").asText());}
        catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_UNKNOWN_RESULT");}
    }
    private JsonNode request(String operation,Map<String,String> args){
        if(!settings.isEnabled())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IDENTITY_PROVIDER_NOT_CONFIGURED");
        String url=settings.getUrl(),secret=settings.getBearerToken();
        if(url==null||url.isBlank()||secret==null||secret.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IDENTITY_PROVIDER_CREDENTIALS_MISSING");
        URI base;
        try {base=URI.create(url);}catch(IllegalArgumentException e){throw new IllegalStateException("Invalid IdP URL",e);}
        if(!"https".equalsIgnoreCase(base.getScheme())||base.getHost()==null||base.getUserInfo()!=null
                ||base.getFragment()!=null||base.getQuery()!=null||base.getPort()==0)
            throw new IllegalStateException("IdP endpoint must be an HTTPS base URL without credentials/query");
        if(settings.getTimeoutSeconds()<1||settings.getTimeoutSeconds()>30)
            throw new IllegalStateException("IdP timeout outside 1..30 s");
        try {
            Map<String,Object> payload=new LinkedHashMap<>();
            payload.put("operation",operation);payload.putAll(args);
            HttpRequest request=HttpRequest.newBuilder(base)
                .timeout(Duration.ofSeconds(settings.getTimeoutSeconds()))
                .header("Content-Type","application/json")
                .header("Authorization","Bearer "+secret)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload))).build();
            HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());
            // A 2xx + explicit known result code is required; empty or arbitrary responses are NOT success.
            if(response.statusCode()<200||response.statusCode()>=300)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_HTTP_ERROR");
            JsonNode body=mapper.readTree(response.body());
            if(body==null)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_BAD_RESPONSE");
            return body;
        } catch(InterruptedException e){Thread.currentThread().interrupt();throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_INTERRUPTED",e);}
          catch(IOException e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"IDENTITY_PROVIDER_UNREACHABLE",e);}
    }
}
