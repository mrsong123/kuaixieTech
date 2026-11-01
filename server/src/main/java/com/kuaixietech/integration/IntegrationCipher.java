package com.kuaixietech.integration;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class IntegrationCipher {
 private final byte[] key;private final SecureRandom random=new SecureRandom();
 public IntegrationCipher(@Value("${app.integrations.encryption-key:}") String encoded){byte[] decoded=null;try{if(encoded!=null&&!encoded.isBlank())decoded=Base64.getDecoder().decode(encoded);}catch(IllegalArgumentException ignored){}key=decoded!=null&&decoded.length==32?decoded:null;}
 public boolean ready(){return key!=null;}
 private Cipher cipher(int mode,byte[] iv,String scope)throws Exception{if(!ready())throw new IllegalStateException("INTEGRATION_ENCRYPTION_KEY_REQUIRED");Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(mode,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));c.updateAAD(scope.getBytes(StandardCharsets.UTF_8));return c;}
 public String seal(String scope,String text){try{byte[] iv=new byte[12];random.nextBytes(iv);var b64=Base64.getEncoder();return "gcm1:"+b64.encodeToString(iv)+":"+b64.encodeToString(cipher(Cipher.ENCRYPT_MODE,iv,scope).doFinal(text.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("INTEGRATION_ENCRYPTION_UNAVAILABLE");}}
 public String open(String scope,String stored){try{var parts=stored.split(":",-1);if(parts.length!=3||!parts[0].equals("gcm1"))throw new IllegalArgumentException();byte[] iv=Base64.getDecoder().decode(parts[1]);if(iv.length!=12)throw new IllegalArgumentException();return new String(cipher(Cipher.DECRYPT_MODE,iv,scope).doFinal(Base64.getDecoder().decode(parts[2])),StandardCharsets.UTF_8);}catch(Exception e){throw new IllegalStateException("INTEGRATION_CREDENTIAL_UNREADABLE");}}
}
