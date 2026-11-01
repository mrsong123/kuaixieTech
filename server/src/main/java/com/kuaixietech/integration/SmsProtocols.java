package com.kuaixietech.integration;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
/** Public Alibaba RPC and Tencent TC3 protocols; no retries on ambiguous provider responses. */
@Component
public class SmsProtocols {
 private final IntegrationHttp http;private final ObjectMapper json;
 public SmsProtocols(IntegrationHttp http,ObjectMapper json){this.http=http;this.json=json;}
 public record Receipt(boolean accepted,String providerReference){}
 public Receipt send(String vendor,Map<String,String> fields,String target,String code,String reference){if(!target.matches("\\+[1-9][0-9]{7,14}")||!code.matches("[0-9]{6}"))throw new IllegalArgumentException("SMS_INPUT_REJECTED");try{HttpRequest request=vendor.equals("aliyun")?aliyun(fields,target,code,reference,Instant.now(),UUID.randomUUID().toString()):vendor.equals("tencent")?tencent(fields,target,code,reference,Instant.now()):null;if(request==null)throw new IllegalArgumentException("SMS_VENDOR_UNSUPPORTED");return parse(vendor,target,reference,json.readTree(http.send(request)));}catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalStateException("SMS_DISPATCH_UNCONFIRMED");}}
 HttpRequest aliyun(Map<String,String> f,String phone,String code,String reference,Instant now,String nonce)throws Exception{
  // This endpoint is the mainland SendSms API. International phone numbers require a separately contracted product.
  if(!phone.matches("\\+861[3-9][0-9]{9}"))throw new IllegalArgumentException("SMS_REGION_UNSUPPORTED");
  var p=new TreeMap<String,String>();p.put("AccessKeyId",f.get("access_key_id"));p.put("Action","SendSms");p.put("Format","JSON");p.put("Version","2017-05-25");p.put("RegionId",f.get("region"));p.put("SignatureMethod","HMAC-SHA1");p.put("SignatureVersion","1.0");p.put("SignatureNonce",nonce);p.put("Timestamp",DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC).format(now));p.put("PhoneNumbers",phone.substring(3));p.put("SignName",f.get("sign_name"));p.put("TemplateCode",f.get("template_code"));p.put("TemplateParam",json.writeValueAsString(Map.of("code",code)));p.put("OutId",reference);
  String canonical=form(p);String sign=Base64.getEncoder().encodeToString(hmac("HmacSHA1",(f.get("access_key_secret")+"&").getBytes(StandardCharsets.UTF_8),"POST&%2F&"+encode(canonical)));p.put("Signature",sign);
  return HttpRequest.newBuilder(http.endpoint("https://dysmsapi.aliyuncs.com/",true)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form(p))).build();
 }
 HttpRequest tencent(Map<String,String> f,String phone,String code,String reference,Instant now)throws Exception{
  String body=json.writeValueAsString(new TreeMap<>(Map.of("PhoneNumberSet",List.of(phone),"SmsSdkAppId",f.get("sdk_app_id"),"SignName",f.get("sign_name"),"TemplateId",f.get("template_id"),"TemplateParamSet",List.of(code),"SessionContext",reference)));String host="sms.tencentcloudapi.com",contentType="application/json; charset=utf-8";String canonical="POST\n/\n\ncontent-type:"+contentType+"\nhost:"+host+"\n\ncontent-type;host\n"+sha(body);String date=DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(now),scope=date+"/sms/tc3_request";String toSign="TC3-HMAC-SHA256\n"+now.getEpochSecond()+"\n"+scope+"\n"+sha(canonical);byte[] day=hmac("HmacSHA256",("TC3"+f.get("secret_key")).getBytes(StandardCharsets.UTF_8),date),service=hmac("HmacSHA256",day,"sms"),signing=hmac("HmacSHA256",service,"tc3_request");String authorization="TC3-HMAC-SHA256 Credential="+f.get("secret_id")+"/"+scope+", SignedHeaders=content-type;host, Signature="+HexFormat.of().formatHex(hmac("HmacSHA256",signing,toSign));
  return HttpRequest.newBuilder(http.endpoint("https://"+host+"/",true)).timeout(Duration.ofSeconds(10)).header("Content-Type",contentType).header("Authorization",authorization).header("X-TC-Action","SendSms").header("X-TC-Version","2021-01-11").header("X-TC-Timestamp",Long.toString(now.getEpochSecond())).header("X-TC-Region",f.get("region")).POST(HttpRequest.BodyPublishers.ofString(body)).build();
 }
 Receipt parse(String vendor,String target,String reference,JsonNode root){if(root==null||!root.isObject())throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");if(vendor.equals("aliyun")){if(!root.hasNonNull("Code")||!root.hasNonNull("RequestId"))throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");if(!root.path("Code").asText().equals("OK"))return new Receipt(false,root.path("RequestId").asText());if(root.path("BizId").asText().isBlank())throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");return new Receipt(true,root.path("BizId").asText());}
  var response=root.path("Response");if(response.path("RequestId").asText().isBlank())throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");if(response.has("Error")){if(response.path("Error").path("Code").asText().isBlank())throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");return new Receipt(false,response.path("RequestId").asText());}var statuses=response.path("SendStatusSet");if(!statuses.isArray()||statuses.size()!=1)throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");var status=statuses.get(0);if(!target.equals(status.path("PhoneNumber").asText())||!reference.equals(status.path("SessionContext").asText())||status.path("Code").asText().isBlank())throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");if(!status.path("Code").asText().equals("Ok"))return new Receipt(false,response.path("RequestId").asText());if(status.path("SerialNo").asText().isBlank())throw new IllegalStateException("SMS_RESPONSE_UNCONFIRMED");return new Receipt(true,status.path("SerialNo").asText());
 }
 static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8).replace("+","%20").replace("*","%2A").replace("%7E","~");}
 static String form(Map<String,String> p){return p.entrySet().stream().map(e->encode(e.getKey())+"="+encode(e.getValue())).collect(Collectors.joining("&"));}
 static byte[] hmac(String alg,byte[] key,String value)throws Exception{var mac=Mac.getInstance(alg);mac.init(new SecretKeySpec(key,alg));return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));}
 static String sha(String value)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
}
