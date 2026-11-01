package com.kuaixietech.payments;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.integration.*;
import java.security.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static com.kuaixietech.payments.PaymentModels.*;
class PaymentCredentialVaultTest {
 Merchant merchant(){return new Merchant("id","merchant","wechat_native_v3","wechat","web",List.of("CNY"),"WECHAT_MAIN",0,300,true,1,Map.of());}
 @Test void inlinePemCanSignAndVerifyWithoutServerKeyFiles()throws Exception{var pair=KeyPairGenerator.getInstance("RSA");pair.initialize(2048);var keys=pair.generateKeyPair();String privatePem="-----BEGIN PRIVATE KEY-----\n"+Base64.getEncoder().encodeToString(keys.getPrivate().getEncoded())+"\n-----END PRIVATE KEY-----";String publicPem="-----BEGIN PUBLIC KEY-----\n"+Base64.getEncoder().encodeToString(keys.getPublic().getEncoded())+"\n-----END PUBLIC KEY-----";var config=mock(IntegrationConfiguration.class);when(config.exists("credentials","WECHAT_MAIN")).thenReturn(true);when(config.runtime("credentials","WECHAT_MAIN")).thenReturn(Map.of("PRIVATE_KEY_PEM",privatePem,"PLATFORM_PUBLIC_KEY_PEM",publicPem));var vault=new PaymentSecrets(new IntegrationCredentials(config));assertTrue(vault.present(merchant(),"PRIVATE_KEY_FILE","PLATFORM_PUBLIC_KEY_FILE"));String signature=PaymentSecrets.sign(vault.privateKey(merchant()),"receipt");assertTrue(PaymentSecrets.verify(vault.publicKey(merchant(),"PLATFORM_PUBLIC_KEY_FILE"),"receipt",signature));assertFalse(PaymentSecrets.verify(vault.publicKey(merchant(),"PLATFORM_PUBLIC_KEY_FILE"),"changed",signature));}
 @Test void merchantChangeCannotReuseAnOldIdentitySnapshot(){var config=mock(IntegrationConfiguration.class);when(config.exists("credentials","WECHAT_MAIN")).thenReturn(true);when(config.runtime("credentials","WECHAT_MAIN")).thenReturn(Map.of("APP_ID","app1","MERCHANT_ID","merchant1"));var vault=new PaymentSecrets(new IntegrationCredentials(config));var snapshot=vault.snapshot(merchant());when(config.runtime("credentials","WECHAT_MAIN")).thenReturn(Map.of("APP_ID","app2","MERCHANT_ID","merchant1"));assertThrows(IllegalStateException.class,()->vault.value(snapshot,"APP_ID"));}
}
