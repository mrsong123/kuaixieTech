package com.kuaixietech.integrationsdk;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;
/** Explicit immutable configuration snapshot. No global mode, ADC, instance role or environment-secret fallback. */
public record SdkContext(Mode mode,String providerKey,long configRevision,@JsonIgnore Map<String,String> values) {
 public enum Mode { SANDBOX,LIVE }
 public SdkContext {Objects.requireNonNull(mode,"SDK_MODE_REQUIRED");if(providerKey==null||providerKey.isBlank()||configRevision<1)throw new IllegalArgumentException("SDK_CONTEXT_REQUIRED");values=Map.copyOf(Objects.requireNonNull(values,"SDK_VALUES_REQUIRED"));}
 @Override public String toString(){return "SdkContext[mode="+mode+", providerKey="+providerKey+", configRevision="+configRevision+", configuredFieldNames="+new TreeSet<>(values.keySet())+"]";}
}
