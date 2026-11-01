package com.kuaixietech.coupler;
import com.kuaixietech.integrationsdk.*;
import com.kuaixietech.integrationsdk.social.*;
import com.kuaixietech.integrationsdk.paymentprotocol.*;
import com.kuaixietech.integration.EmailProtocols;
import java.util.*;
import org.springframework.stereotype.Service;
import static com.kuaixietech.coupler.CouplerModels.*;
/** Local checks construct and close SDK clients; they never send a paid request or perform a remote probe. */
@Service
public class CouplerAdapterRegistry {
 private final ClientFactories factories=new ClientFactories();private final PublicServiceAdapters publicApis;private final EmailProtocols email;
 public CouplerAdapterRegistry(PublicServiceAdapters publicApis,EmailProtocols email){this.publicApis=publicApis;this.email=email;}
 public void validate(Provider p,Mode mode,long revision,Map<String,String> values){
  if(!p.supportedModes().contains(mode))throw new IllegalArgumentException("COUPLER_MODE_UNSUPPORTED");
  if(p.category().equals("FEATURES")){for(String value:values.values())if(!Set.of("true","false").contains(value))throw new IllegalArgumentException("COUPLER_BOOLEAN_REQUIRED");return;}
  if(p.capability().equals("reference_only"))throw new IllegalArgumentException("COUPLER_IMPLEMENTATION_UNAVAILABLE");
  if(Set.of("AI","TRANSLATE","MAP").contains(p.category())&&p.implementedOperations().size()>0){publicApis.validate(p.category(),p.code(),values);return;}
  if(p.category().equals("EMAIL")&&p.code().equals("aliyun")){email.validateConfiguration(values);return;}
  if(p.category().equals("SMS")){if(!values.getOrDefault("region","").matches("[a-z0-9-]{2,40}"))throw new IllegalArgumentException("COUPLER_REGION_INVALID");return;}
  if(p.category().equals("OSS")){if(!values.getOrDefault("bucket","").matches("[a-z0-9][a-z0-9-]{1,62}"))throw new IllegalArgumentException("COUPLER_BUCKET_INVALID");if(p.code().equals("aliyun")&&!values.getOrDefault("endpoint","").replaceFirst("^https://","").replaceFirst("/$","").matches("oss-[a-z0-9-]+[.]aliyuncs[.]com"))throw new IllegalArgumentException("COUPLER_ENDPOINT_INVALID");if(p.code().equals("tencent")&&(!values.getOrDefault("bucket","").matches("[a-z0-9-]+-[0-9]{5,20}")||!values.getOrDefault("region","").matches("[a-z0-9-]{3,40}")))throw new IllegalArgumentException("COUPLER_ENDPOINT_INVALID");return;}
  if(p.category().equals("MINIAPP")||p.category().equals("SUBSCRIBE")){if(!values.getOrDefault("app_id","").matches("wx[A-Za-z0-9]{8,40}")||values.getOrDefault("app_secret","").isBlank())throw new IllegalArgumentException("COUPLER_WECHAT_APP_INVALID");return;}
  if(p.category().equals("RTC")&&p.code().equals("tencent")){try{long app=Long.parseLong(values.getOrDefault("sdk_app_id","")),ttl=Long.parseLong(values.getOrDefault("usersig_ttl_seconds",""));if(app<1||app>0xffffffffL||ttl<60||ttl>900||values.getOrDefault("secret_key","").length()<32||!values.getOrDefault("advanced_permission_control","").equals("true"))throw new IllegalArgumentException();}catch(Exception e){throw new IllegalArgumentException("COUPLER_RTC_CONFIGURATION_INVALID");}return;}
  if(p.category().equals("ALIPAY")){PaymentEnvironmentPolicy.alipayGateway(mode.name());localAlipayKeys(values);return;}
  if(factories.supports(p.category(),p.code())){factories.validateLocal(new SdkContext(SdkContext.Mode.valueOf(mode.name()),factories.descriptor(p.category(),p.code()).providerKey(),revision,values));return;}
  throw new IllegalArgumentException("COUPLER_IMPLEMENTATION_UNAVAILABLE");
 }
 private void localAlipayKeys(Map<String,String> values){try{if(!values.getOrDefault("app_id","").matches("[0-9]{8,32}"))throw new IllegalArgumentException();var factory=java.security.KeyFactory.getInstance("RSA");String privatePem=values.getOrDefault("private_key_pem","");byte[] privateBytes=java.util.Base64.getDecoder().decode(privatePem.replaceAll("-----[^-]+-----","").replaceAll("\\s",""));var privateKey=factory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(privateBytes));var signer=java.security.Signature.getInstance("SHA256withRSA");signer.initSign(privateKey);signer.update(new byte[]{1});signer.sign();String publicPem=values.getOrDefault("platform_public_key_pem","");if(publicPem.contains("BEGIN CERTIFICATE")){try(var stream=new java.io.ByteArrayInputStream(publicPem.getBytes(java.nio.charset.StandardCharsets.US_ASCII))){var cert=(java.security.cert.X509Certificate)java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(stream);cert.checkValidity();if(!cert.getPublicKey().getAlgorithm().equals("RSA"))throw new IllegalArgumentException();}}else factory.generatePublic(new java.security.spec.X509EncodedKeySpec(java.util.Base64.getDecoder().decode(publicPem.replaceAll("-----[^-]+-----","").replaceAll("\\s",""))));}catch(Exception e){throw new IllegalArgumentException("COUPLER_ALIPAY_KEYS_INVALID");}}
}
