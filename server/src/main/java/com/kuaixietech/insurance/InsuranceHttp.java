package com.kuaixietech.insurance;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.stereotype.Component;
import static com.kuaixietech.insurance.InsuranceModels.*;
/** Transport for native insurer adapters. No shared made-up provider protocol or signature scheme. */
@Component
public class InsuranceHttp {
 private final HttpClient client;private final Set<String> allowed=new HashSet<>();
 public InsuranceHttp(){var builder=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER);String proxy=System.getenv("HTTPS_PROXY");if(proxy!=null&&!proxy.isBlank()){URI p=URI.create(proxy);builder.proxy(ProxySelector.of(new InetSocketAddress(p.getHost(),p.getPort()<0?80:p.getPort())));}client=builder.build();for(String host:System.getenv().getOrDefault("INSURANCE_API_ALLOWED_HOSTS","").split(","))if(!host.isBlank())allowed.add(host.trim().toLowerCase(Locale.ROOT));}
 static void validate(Product product,URI endpoint,Set<String> allowed){InsuranceUrlPolicy.require(endpoint.toString(),product.serviceHosts());if(!allowed.contains(endpoint.getHost().toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("INSURANCE_API_HOST_NOT_ALLOWED");}
 public HttpResponse<String> exchange(Product product,HttpRequest request){
  validate(product,request.uri(),allowed);
  if(request.timeout().isEmpty()||request.timeout().get().compareTo(Duration.ofSeconds(30))>0)throw new IllegalArgumentException("INSURANCE_REQUEST_TIMEOUT_REQUIRED");
  try{var response=client.send(request,HttpResponse.BodyHandlers.ofString());if(response.statusCode()<200||response.statusCode()>=300||response.body().length()>2_000_000)throw new IllegalStateException("INSURANCE_PROVIDER_RESPONSE_REJECTED");return response;}
  catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("INSURANCE_PROVIDER_QUERY_INTERRUPTED");}
  catch(Exception e){throw new IllegalStateException("INSURANCE_PROVIDER_QUERY_FAILED");}
 }
}
