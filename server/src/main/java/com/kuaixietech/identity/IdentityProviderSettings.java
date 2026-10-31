package com.kuaixietech.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Server-to-server identity management endpoint. No client-provided URL allowed. */
@ConfigurationProperties(prefix = "suixinggou.identity-provider")
public class IdentityProviderSettings {
    private boolean enabled = false;
    private String url = "";
    private String bearerToken = "";
    private int timeoutSeconds = 8;
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean value){this.enabled=value;}
    public String getUrl(){return url;}
    public void setUrl(String url){this.url=url;}
    public String getBearerToken(){return bearerToken;}
    public void setBearerToken(String bearerToken){this.bearerToken=bearerToken;}
    public int getTimeoutSeconds(){return timeoutSeconds;}
    public void setTimeoutSeconds(int value){this.timeoutSeconds=value;}
}
