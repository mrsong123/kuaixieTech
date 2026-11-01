package com.kuaixietech.integrationsdk;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.kuaixietech.integrationsdk.SdkContext.Mode.*;

class ClientFactoriesTest {
 private final ClientFactories factories=new ClientFactories("");
 @Test void contextCopiesAndRedactsSecrets() {
  var values=new HashMap<String,String>();values.put("secret_key","DO_NOT_PRINT_SECRET");
  var context=new SdkContext(LIVE,"tencent/trtc",7,values);values.put("secret_key","changed");
  assertEquals("DO_NOT_PRINT_SECRET",context.values().get("secret_key"));
  assertThrows(UnsupportedOperationException.class,()->context.values().put("other","value"));
  assertFalse(context.toString().contains("DO_NOT_PRINT_SECRET"));
  assertTrue(context.toString().contains("configRevision=7"));
  assertThrows(IllegalArgumentException.class,()->new SdkContext(LIVE,"tencent/trtc",0,Map.of()));
 }
 @Test void requiresExplicitRevisionAndNeverFallsBackFromSandbox() {
  assertEquals("SDK_CONFIG_REVISION_REQUIRED",assertThrows(IllegalArgumentException.class,
   ()->factories.create("RTC","tencent",LIVE,Map.of())).getMessage());
  assertEquals("SDK_MODE_UNSUPPORTED",assertThrows(IllegalArgumentException.class,
   ()->factories.create(new SdkContext(SANDBOX,"tencent/trtc",1,Map.of()))).getMessage());
  assertFalse(factories.supports("RAIL","12306"));
  assertFalse(factories.supports("SMS","huawei"));
 }
 @Test void configurationDoesNotClaimBusinessAcceptanceOrConsumeDefaultCredentials() {
  assertTrue(factories.descriptors().stream().noneMatch(d->d.consumerWired()||d.businessVerified()));
  assertEquals("SDK_REQUIRED_FIELD_MISSING",assertThrows(IllegalArgumentException.class,
   ()->factories.create(new SdkContext(LIVE,"aws/s3",1,Map.of("region","us-east-1")))).getMessage());
  assertEquals(Set.of(SANDBOX,LIVE),factories.descriptor("alipay/pay").supportedModes());
 }
 @Test void tokenClientHasBoundedScopeAndIdempotentClose() {
  var context=new SdkContext(LIVE,"tencent/trtc",3,Map.of("sdk_app_id","1400000000","secret_key","synthetic-local-test-secret"));
  var handle=factories.create(context);
  var signer=handle.client(ClientFactories.TrtcTokens.class);
  assertThrows(IllegalArgumentException.class,()->signer.sign("buyer",3601));
  assertThrows(IllegalArgumentException.class,()->signer.sign("",60));
  assertFalse(signer.sign("buyer",60).isBlank());
  assertFalse(handle.toString().contains("synthetic-local-test-secret"));
  handle.close();handle.close();assertTrue(handle.closed());
  assertThrows(IllegalStateException.class,handle::client);
  var validation=factories.validateLocal(context);
  assertEquals("local_configured",validation.get("status"));
  assertEquals(false,validation.get("businessVerified"));
 }
}
