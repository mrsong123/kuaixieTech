package com.kuaixietech.integrationsdk.social;
import java.util.*;
/** One decrypted configuration snapshot supplied by the approved runtime resolver; never a browser DTO. */
public record SocialServiceContext(String category,String provider,String mode,@com.fasterxml.jackson.annotation.JsonIgnore Map<String,String> values,long revision) {
 public SocialServiceContext {if(category==null||provider==null||!Set.of("SANDBOX","LIVE").contains(mode)||values==null||revision<0)throw new IllegalArgumentException("SERVICE_CONTEXT_INVALID");values=Map.copyOf(values);}
 public String required(String key){String value=values.get(key);if(value==null||value.isBlank())throw new IllegalStateException("SERVICE_CREDENTIAL_REQUIRED");return value;}
 public void require(String expectedCategory,String expectedProvider,boolean supportsSandbox){if(!category.equals(expectedCategory)||!provider.equals(expectedProvider))throw new IllegalArgumentException("SERVICE_PROVIDER_MISMATCH");if(mode.equals("SANDBOX")&&!supportsSandbox)throw new IllegalStateException("SERVICE_OFFICIAL_SANDBOX_UNAVAILABLE");}
 @Override public String toString(){return "SocialServiceContext["+category+"/"+provider+", mode="+mode+", revision="+revision+", credentials=REDACTED]";}
}
