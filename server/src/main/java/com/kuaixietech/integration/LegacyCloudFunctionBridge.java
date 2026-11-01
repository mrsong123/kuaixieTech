package com.kuaixietech.integration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
/**
 * Configurable transport bridge for the eleven legacy cloud functions. Not a vendor implementation.
 * Preserves request JSON and returns upstream response JSON; no fake success when unconfigured.
 * Never expose this as an arbitrary-URL proxy or unauthenticated controller.
 */
@Service
@EnableConfigurationProperties(LegacyCloudFunctionSettings.class)
public class LegacyCloudFunctionBridge {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

  private final LegacyCloudFunctionSettings settings;
  private IntegrationConfiguration configuration;private IntegrationHttp transport;
  @org.springframework.beans.factory.annotation.Autowired public void integrationConfiguration(IntegrationConfiguration configuration,IntegrationHttp transport){this.configuration=configuration;this.transport=transport;}
  private final ObjectMapper json;
  private final HttpClient client=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
  public LegacyCloudFunctionBridge(LegacyCloudFunctionSettings settings,ObjectMapper json){this.settings=settings;this.json=json;}
  public JsonNode invoke(LegacyCloudFunction function,Map<String,?> payload) {
  if(couplerMode!=null)couplerMode.requireLiveNewCall("legacy_cloud_call");
    LegacyCloudFunctionSettings.Endpoint endpoint=settings.getEndpoints().get(function.key());
    if(configuration!=null&&configuration.exists("http-adapter",function.key())){var values=configuration.runtime("http-adapter",function.key());if(values==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"CLOUD_FUNCTION_DISABLED");endpoint=new LegacyCloudFunctionSettings.Endpoint();endpoint.setEnabled(true);endpoint.setUrl(transport.endpoint(values.get("url"),false));endpoint.setBearerToken(values.get("bearer_token"));endpoint.setTimeoutSeconds(Integer.parseInt(values.get("timeout_seconds")));}
    if(endpoint==null||!endpoint.isEnabled()||endpoint.getUrl()==null)
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"CLOUD_FUNCTION_NOT_CONFIGURED:"+function.key());
    URI url=endpoint.getUrl();
    if(!"https".equalsIgnoreCase(url.getScheme())||url.getHost()==null||url.getUserInfo()!=null||url.getFragment()!=null)
      throw new IllegalStateException("Cloud function endpoint must be an HTTPS URL without embedded credentials: "+function.key());
    if(endpoint.getTimeoutSeconds()<1||endpoint.getTimeoutSeconds()>120)throw new IllegalStateException("Invalid timeout for "+function.key());
    String token=endpoint.getBearerToken();
    if(token==null||token.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"CLOUD_FUNCTION_CREDENTIAL_NOT_CONFIGURED:"+function.key());
    try {
      String content=json.writeValueAsString(payload==null?Map.of():payload);
      HttpRequest request=HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(endpoint.getTimeoutSeconds()))
          .header("Content-Type","application/json").header("Authorization","Bearer "+token)
          .POST(HttpRequest.BodyPublishers.ofString(content)).build();
      if(transport!=null){transport.endpoint(url.toString(),false);return json.readTree(transport.send(request));}
      HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()<200||response.statusCode()>=300)
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"CLOUD_FUNCTION_UPSTREAM_STATUS:"+function.key()+":"+response.statusCode());
      // Never synthesize success: upstream must provide valid JSON.
      return json.readTree(response.body());
    } catch(InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"CLOUD_FUNCTION_INTERRUPTED:"+function.key(),e);
    } catch(IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"CLOUD_FUNCTION_UNAVAILABLE:"+function.key(),e);
    }
  }
}
