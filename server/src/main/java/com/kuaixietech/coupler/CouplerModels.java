package com.kuaixietech.coupler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.util.*;
public final class CouplerModels {
 private CouplerModels(){}
 public enum Mode {SANDBOX,LIVE}
 public record Runtime(Mode mode,long revision){}
 public record Switch(@NotNull Mode mode,@Min(0) long expectedRevision){}
 public record Save(@NotNull @Size(max=80) Map<@NotBlank String,@NotNull @Size(max=16000) String> values,@NotNull @Size(max=80) List<String> clearFields,boolean enabled,boolean selected,@Min(0) long expectedRevision){}
 public record Field(String key,String label,boolean secret,boolean required,String placeholder,String inputType,List<Map<String,String>> options){}
 public record Provider(String category,String code,String name,String vendor,String product,String capability,String docs,List<Mode> supportedModes,boolean sdkAvailable,boolean consumerWired,List<String> implementedOperations,List<Field> fields){}
 public record Category(String code,String name,String description,int sort,List<Provider> providers){}
 /** Decrypted once for a trusted server-side consumer; never serialize it to an API response. */
 public record Resolved(Mode mode,String category,String provider,long configRevision,long scopeRevision,@JsonIgnore Map<String,String> values){
  public Resolved{values=Map.copyOf(values);if(configRevision<1)throw new IllegalArgumentException("COUPLER_CONFIG_REVISION_REQUIRED");}
  @Override public String toString(){return "Resolved[mode="+mode+", category="+category+", provider="+provider+", configRevision="+configRevision+", fields="+new TreeSet<>(values.keySet())+"]";}
 }
 public record LegacyRoute(boolean managed,Map<String,String> values){}
}
