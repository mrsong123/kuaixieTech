package com.kuaixietech.payments;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.stereotype.Component;

/** Session proxy and normal CA verification remain in force; redirects are never followed. */
@Component
public class PaymentHttp {
  private final HttpClient client;
  private final Set<String> allowedGatewayHosts;
  public PaymentHttp(){
    var builder=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER);
    String proxy=System.getenv("HTTPS_PROXY");
    if(proxy!=null&&!proxy.isBlank()){
      URI uri=URI.create(proxy);builder.proxy(ProxySelector.of(new InetSocketAddress(uri.getHost(),uri.getPort()<0?80:uri.getPort())));
    }
    client=builder.build();
    allowedGatewayHosts=new HashSet<>();
    for(String host:System.getenv().getOrDefault("PAYMENT_GATEWAY_ALLOWED_HOSTS","").split(","))if(!host.isBlank())allowedGatewayHosts.add(host.trim().toLowerCase(Locale.ROOT));
  }
  public URI endpoint(String value,boolean custom){
    URI uri=URI.create(value);
    if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getFragment()!=null||uri.getPort()!=-1&&uri.getPort()!=443)
      throw new IllegalArgumentException("HTTPS_ENDPOINT_REQUIRED");
    if(custom&&uri.getRawQuery()!=null)throw new IllegalArgumentException("GATEWAY_ENDPOINT_QUERY_NOT_ALLOWED");
    if(custom&&!allowedGatewayHosts.contains(uri.getHost().toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("GATEWAY_HOST_NOT_ALLOWED");
    return uri;
  }
  public String send(HttpRequest request){return exchange(request).body();}
  public HttpResponse<String> exchange(HttpRequest request){
    try{
      var response=client.send(request,HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()<200||response.statusCode()>=300||response.body().length()>2_000_000)throw new IllegalStateException("PROVIDER_RESPONSE_REJECTED");
      return response;
    }catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("PROVIDER_REQUEST_INTERRUPTED");}
    catch(Exception e){throw new IllegalStateException("PROVIDER_REQUEST_FAILED");}
  }
}
