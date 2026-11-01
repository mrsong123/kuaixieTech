package com.kuaixietech.storage;
import com.kuaixietech.integration.*;
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
import org.springframework.stereotype.Service;
/** Private objects only: Alibaba OSS REST V1 and Tencent COS XML V5. Never grant a public ACL. */
@Service
public class PrivateObjectStorage {
 private final IntegrationConfiguration config;private final IntegrationHttp http;
 public PrivateObjectStorage(IntegrationConfiguration config,IntegrationHttp http){this.config=config;this.http=http;}
 public record Binding(String vendor,String bucket,Map<String,String> fields){
  public Binding {fields=Map.copyOf(fields);}
  @Override public String toString(){return "Binding[vendor="+vendor+",bucket="+bucket+"]";}
 }
 public String reference(Binding b,String key){
  signed(b,"GET",key,"","",null,Instant.now());
  long revision=Long.parseLong(b.fields().getOrDefault("_coupler_config_revision","0"));
  if(revision<0)throw new IllegalArgumentException("UPLOAD_REVISION_INVALID");
  return "v1:"+revision+":"+b.vendor()+"://"+b.bucket()+"/"+key;
 }
 private record ObjectReference(Binding binding,String key){}
 private ObjectReference resolve(String reference){
  long revision=0;String location=reference;
  if(reference.startsWith("v1:")){int end=reference.indexOf(':',3);if(end<0)throw new IllegalArgumentException("UPLOAD_REFERENCE_INVALID");revision=Long.parseLong(reference.substring(3,end));if(revision<0)throw new IllegalArgumentException("UPLOAD_REVISION_INVALID");location=reference.substring(end+1);}
  var u=URI.create(location);String vendor=u.getScheme(),bucket=u.getHost();
  if(!Set.of("aliyun","tencent").contains(vendor)||bucket==null||u.getUserInfo()!=null||u.getPort()!=-1||u.getRawQuery()!=null||u.getRawFragment()!=null||u.getRawPath()==null||!u.getRawPath().matches("/private/[a-f0-9-]{36}/[a-f0-9-]{36}"))throw new IllegalArgumentException("UPLOAD_REFERENCE_INVALID");
  String product=vendor.equals("aliyun")?"oss":"cos";
  var fields=revision>0?config.historicalLiveRuntime(vendor,product,revision):config.legacyLiveRuntime(vendor,product);
  if(fields==null||!bucket.equals(fields.get("bucket")))throw new IllegalStateException("UPLOAD_STORAGE_CONFIGURATION_CHANGED");
  return new ObjectReference(new Binding(vendor,bucket,fields),u.getRawPath().substring(1));
 }
 public void delete(String storageKey){var object=resolve(storageKey);http.delete(signed(object.binding(),"DELETE",object.key(),"","",null,Instant.now()));}

 public Binding active(){if(config.couplerManaged("aliyun","oss")){Binding selected=null;for(String vendor:List.of("aliyun","tencent")){var fields=config.runtime(vendor,vendor.equals("aliyun")?"oss":"cos");if(fields!=null){if(selected!=null)throw new IllegalStateException("UPLOAD_MULTIPLE_ACTIVE_PROVIDERS");selected=new Binding(vendor,fields.get("bucket"),fields);}}if(selected==null)throw new IllegalStateException("UPLOAD_PROVIDER_NOT_CONFIGURED");return selected;}if(config.exists("platform","storage-routing")){var routing=config.runtime("platform","storage-routing");if(routing==null)throw new IllegalStateException("UPLOAD_ROUTING_DISABLED");String vendor=routing.get("vendor");var fields=config.runtime(vendor,vendor.equals("aliyun")?"oss":"cos");if(fields==null)throw new IllegalStateException("UPLOAD_PROVIDER_NOT_CONFIGURED");return new Binding(vendor,fields.get("bucket"),fields);}Binding result=null;for(String vendor:List.of("aliyun","tencent")){var fields=config.runtime(vendor,vendor.equals("aliyun")?"oss":"cos");if(fields!=null){if(result!=null)throw new IllegalStateException("UPLOAD_MULTIPLE_ACTIVE_PROVIDERS");result=new Binding(vendor,fields.get("bucket"),fields);}}if(result==null)throw new IllegalStateException("UPLOAD_PROVIDER_NOT_CONFIGURED");return result;}
 public void put(Binding b,String key,String mime,byte[] bytes){String md5=Base64.getEncoder().encodeToString(digest("MD5",bytes));var request=signed(b,"PUT",key,mime,md5,bytes,Instant.now());String etag=http.write(request);String expected=HexFormat.of().formatHex(digest("MD5",bytes));if(!etag.replace("\"","").equalsIgnoreCase(expected))throw new IllegalStateException("UPLOAD_RECEIPT_UNCONFIRMED");}
 public byte[] get(String storageKey,int limit){var object=resolve(storageKey);return http.read(signed(object.binding(),"GET",object.key(),"","",null,Instant.now()),limit);}

 HttpRequest signed(Binding b,String method,String key,String mime,String md5,byte[] bytes,Instant now){if(!key.matches("private/[a-f0-9-]{36}/[a-f0-9-]{36}")||!b.bucket().matches("[a-z0-9][a-z0-9-]{1,62}"))throw new IllegalArgumentException("UPLOAD_OBJECT_KEY_REJECTED");String host;var f=b.fields();if(b.vendor().equals("aliyun")){String endpoint=f.get("endpoint").replaceFirst("^https://","").replaceFirst("/$","");if(!endpoint.matches("oss-[a-z0-9-]+\\.aliyuncs\\.com"))throw new IllegalArgumentException("UPLOAD_OSS_ENDPOINT_REJECTED");host=b.bucket()+"."+endpoint;}else{String region=f.get("region");if(!region.matches("[a-z0-9-]{3,40}")||!b.bucket().matches("[a-z0-9-]+-[0-9]{5,20}"))throw new IllegalArgumentException("UPLOAD_COS_ENDPOINT_REJECTED");host=b.bucket()+".cos."+region+".myqcloud.com";}
  var builder=HttpRequest.newBuilder(http.endpoint("https://"+host+"/"+key,true)).timeout(Duration.ofSeconds(30));var headers=new TreeMap<String,String>();headers.put("host",host);if(method.equals("PUT")){headers.put("content-md5",md5);headers.put("content-type",mime);headers.put(b.vendor().equals("aliyun")?"x-oss-object-acl":"x-cos-acl","private");}
  if(b.vendor().equals("aliyun")){String date=DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.US).withZone(ZoneOffset.UTC).format(now);builder.header("Date",date);String canonical=method+"\n"+md5+"\n"+mime+"\n"+date+"\n"+(method.equals("PUT")?"x-oss-object-acl:private\n":"")+"/"+b.bucket()+"/"+key;builder.header("Authorization","OSS "+f.get("access_key_id")+":"+Base64.getEncoder().encodeToString(hmac(f.get("access_key_secret"),canonical)));}
  else{String time=(now.getEpochSecond()-30)+";"+(now.getEpochSecond()+300);String canonical=headers.entrySet().stream().map(e->encode(e.getKey())+"="+encode(e.getValue())).collect(Collectors.joining("&"));String httpString=method.toLowerCase(Locale.ROOT)+"\n/"+key+"\n\n"+canonical+"\n";String toSign="sha1\n"+time+"\n"+sha(httpString)+"\n";String signKey=HexFormat.of().formatHex(hmac(f.get("secret_key"),time));builder.header("Authorization","q-sign-algorithm=sha1&q-ak="+encode(f.get("secret_id"))+"&q-sign-time="+time+"&q-key-time="+time+"&q-header-list="+String.join(";",headers.keySet())+"&q-url-param-list=&q-signature="+HexFormat.of().formatHex(hmac(signKey,toSign)));}
  headers.forEach((name,value)->{if(!name.equals("host"))builder.header(name,value);});return builder.method(method,bytes==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofByteArray(bytes)).build();
 }
 static String encode(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8).replace("+","%20").replace("*","%2A").replace("%7E","~");}
 static byte[] digest(String algorithm,byte[] bytes){try{return MessageDigest.getInstance(algorithm).digest(bytes);}catch(Exception e){throw new IllegalStateException("UPLOAD_HASH_UNAVAILABLE");}}
 static byte[] hmac(String key,String text){try{var mac=Mac.getInstance("HmacSHA1");mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"HmacSHA1"));return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("UPLOAD_SIGNING_UNAVAILABLE");}}
 static String sha(String text){return HexFormat.of().formatHex(digest("SHA-1",text.getBytes(StandardCharsets.UTF_8)));}
}
