package com.kuaixietech.integration;
import org.springframework.stereotype.Component;
@Component
public class IntegrationCredentials {
 private final IntegrationConfiguration configuration;
 public IntegrationCredentials(IntegrationConfiguration configuration){this.configuration=configuration;}
 public String optional(String prefix,String suffix){if(prefix==null||!prefix.matches("[A-Z][A-Z0-9_]{2,39}")||!suffix.matches("[A-Z][A-Z0-9_]{1,63}"))throw new IllegalStateException("CREDENTIALS_NOT_CONFIGURED");if(configuration!=null&&configuration.exists("credentials",prefix)){var fields=configuration.runtime("credentials",prefix);if(fields==null)throw new IllegalStateException("CREDENTIAL_BUNDLE_DISABLED");return fields.get(suffix);}return System.getenv(prefix+"_"+suffix);}
 public String required(String prefix,String suffix){String value=optional(prefix,suffix);if(value==null||value.isBlank())throw new IllegalStateException("CREDENTIALS_NOT_CONFIGURED");return value;}
}
