package com.kuaixietech.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="suixinggou.auth.jwt")
public class VerifiedJwtSettings {
    private boolean enabled=false;
    private String issuer="";
    private String jwkSetUri="";
    private String audience="";
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean enabled){this.enabled=enabled;}
    public String getIssuer(){return issuer;}
    public void setIssuer(String issuer){this.issuer=issuer;}
    public String getJwkSetUri(){return jwkSetUri;}
    public void setJwkSetUri(String jwkSetUri){this.jwkSetUri=jwkSetUri;}
    public String getAudience(){return audience;}
    public void setAudience(String audience){this.audience=audience;}
}
