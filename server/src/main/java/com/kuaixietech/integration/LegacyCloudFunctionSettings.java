package com.kuaixietech.integration;
import java.net.URI;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
/** Only endpoint configuration; credentials are resolved from environment by Spring. */
@ConfigurationProperties(prefix="suixinggou.cloud-functions")
public class LegacyCloudFunctionSettings {
  private Map<String,Endpoint> endpoints=Map.of();
  public Map<String,Endpoint> getEndpoints(){return endpoints;}
  public void setEndpoints(Map<String,Endpoint> endpoints){this.endpoints=endpoints;}
  public static class Endpoint {
    private boolean enabled;
    private URI url;
    private String bearerToken;
    private int timeoutSeconds=10;
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean enabled){this.enabled=enabled;}
    public URI getUrl(){return url;}
    public void setUrl(URI url){this.url=url;}
    public String getBearerToken(){return bearerToken;}
    public void setBearerToken(String bearerToken){this.bearerToken=bearerToken;}
    public int getTimeoutSeconds(){return timeoutSeconds;}
    public void setTimeoutSeconds(int timeoutSeconds){this.timeoutSeconds=timeoutSeconds;}
  }
}
