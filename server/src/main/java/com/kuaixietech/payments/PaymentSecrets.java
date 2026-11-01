package com.kuaixietech.payments;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import static com.kuaixietech.payments.PaymentModels.*;

@org.springframework.stereotype.Component
final class PaymentSecrets {
  private final com.kuaixietech.integration.IntegrationCredentials credentials;
  PaymentSecrets(com.kuaixietech.integration.IntegrationCredentials credentials){this.credentials=credentials;}
  String value(Merchant merchant,String suffix){
    String prefix=merchant.credentialPrefix();
    if(prefix==null||!prefix.matches("[A-Z][A-Z0-9_]{2,39}"))throw new IllegalStateException("CREDENTIALS_NOT_CONFIGURED");
    String value=credentials.required(prefix,suffix);
    if(value==null||value.isBlank())throw new IllegalStateException("CREDENTIALS_NOT_CONFIGURED");
    String identity=merchant.settings().get("_identity_"+suffix);
    if(identity!=null&&!identity.equals(value))throw new IllegalStateException("MERCHANT_IDENTITY_CHANGED");
    return value;
  }
  Merchant snapshot(Merchant m){
    List<String> fields=m.adapter().startsWith("wechat_")?List.of("APP_ID","MERCHANT_ID"):m.adapter().equals("alipay_rsa2")?List.of("APP_ID"):m.adapter().equals("hosted_bank_v1")?List.of("MERCHANT_ID"):List.of();
    var settings=new HashMap<>(m.settings());for(String field:fields)settings.put("_identity_"+field,value(m,field));
    return new Merchant(m.id(),m.name(),m.adapter(),m.method(),m.platform(),m.currencies(),m.credentialPrefix(),m.spreadBps(),m.quoteTtlSeconds(),m.enabled(),m.revision(),Map.copyOf(settings));
  }
  boolean present(Merchant merchant,String... names){
    try{for(String name:names){if(name.endsWith("_FILE")){String inline=credentials.optional(merchant.credentialPrefix(),name.replace("_FILE","_PEM"));if(inline!=null&&!inline.isBlank())continue;}String value=value(merchant,name);if(name.endsWith("_FILE")&&!Files.isRegularFile(Path.of(value)))return false;}return true;}
    catch(RuntimeException e){return false;}
  }
  PrivateKey privateKey(Merchant merchant){
    try{String inline=credentials.optional(merchant.credentialPrefix(),"PRIVATE_KEY_PEM");byte[] bytes=inline!=null&&!inline.isBlank()?decode(inline):pem(value(merchant,"PRIVATE_KEY_FILE"));return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));}
    catch(Exception e){throw new IllegalStateException("INVALID_PRIVATE_KEY_CONFIGURATION");}
  }
  PublicKey publicKey(Merchant merchant,String name){
    try{String inline=credentials.optional(merchant.credentialPrefix(),name.replace("_FILE","_PEM"));String path=inline==null||inline.isBlank()?value(merchant,name):null;String content=path==null?inline:Files.readString(Path.of(path),StandardCharsets.US_ASCII);
      if(content.contains("BEGIN CERTIFICATE")){try(var in=new java.io.ByteArrayInputStream(content.getBytes(StandardCharsets.US_ASCII))){var certificate=(java.security.cert.X509Certificate)java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(in);certificate.checkValidity();return certificate.getPublicKey();}}
      return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decode(content)));}
    catch(Exception e){throw new IllegalStateException("INVALID_PUBLIC_KEY_CONFIGURATION");}
  }
  private static byte[] pem(String path)throws Exception{
    if(Files.size(Path.of(path))>65536)throw new IllegalArgumentException("KEY_FILE_TOO_LARGE");
    String value=Files.readString(Path.of(path),StandardCharsets.US_ASCII);
    return decode(value);
  }
  private static byte[] decode(String value){if(value.length()>65536)throw new IllegalArgumentException("KEY_TOO_LARGE");return Base64.getDecoder().decode(value.replaceAll("-----[^-]+-----","").replaceAll("\\s", ""));}
  static String sign(PrivateKey key,String value){
    try{Signature signer=Signature.getInstance("SHA256withRSA");signer.initSign(key);signer.update(value.getBytes(StandardCharsets.UTF_8));return Base64.getEncoder().encodeToString(signer.sign());}
    catch(Exception e){throw new IllegalStateException("REQUEST_SIGNING_FAILED");}
  }
  static boolean verify(PublicKey key,String value,String signature){
    try{Signature verifier=Signature.getInstance("SHA256withRSA");verifier.initVerify(key);verifier.update(value.getBytes(StandardCharsets.UTF_8));return verifier.verify(Base64.getDecoder().decode(signature));}
    catch(Exception e){return false;}
  }
}
