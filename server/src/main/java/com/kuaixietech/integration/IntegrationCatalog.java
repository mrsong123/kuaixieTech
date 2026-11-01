package com.kuaixietech.integration;
import java.util.*;
/** Explicit protocol catalog. Configuration never converts an unimplemented protocol into a live adapter. */
public final class IntegrationCatalog {
 private IntegrationCatalog(){}
 public record Field(String key,boolean secret,boolean required){}
 public record Product(String vendor,String code,String category,String mode,String docs,List<Field> fields){}
 private static Field field(String name){return new Field(name,name.matches(".*(secret|token|key|password|pem).*"),true);}
 private static Product p(String vendor,String code,String category,String mode,String docs,String...fields){return new Product(vendor,code,category,mode,docs,Arrays.stream(fields).map(IntegrationCatalog::field).toList());}
 public static List<Product> all(){var list=new ArrayList<Product>();
  list.add(p("aliyun","sms","sms","implemented","https://help.aliyun.com/zh/sms/developer-reference/api-dysmsapi-2017-05-25-sendsms","access_key_id","access_key_secret","sign_name","template_code","region"));
  list.add(p("tencent","sms","sms","implemented","https://cloud.tencent.com/document/api/382/55981","secret_id","secret_key","sdk_app_id","sign_name","template_id","region"));
  list.add(p("aliyun","directmail","email","implemented","https://help.aliyun.com/zh/direct-mail/api-singlesendmail","access_key_id","access_key_secret","region","account_name","from_alias","subject","text_template"));
  list.add(p("wechat","miniprogram","identity","implemented","https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html","app_id","app_secret"));
  list.add(p("http-adapter","identity-provider","identity","bridge","","url","bearer_token","timeout_seconds"));
  for(var f:LegacyCloudFunction.values())list.add(p("http-adapter",f.key(),"adapter","bridge","","url","bearer_token","timeout_seconds"));
  list.add(p("platform","storage-routing","storage","implemented","","vendor"));
  list.add(p("aliyun","oss","storage","implemented","https://help.aliyun.com/zh/oss/developer-reference/","access_key_id","access_key_secret","bucket","endpoint"));
  list.add(p("tencent","cos","storage","implemented","https://cloud.tencent.com/document/product/436","secret_id","secret_key","bucket","region"));
  list.add(p("aliyun","privacy-number","rtc","reference_only","https://help.aliyun.com/product/56990.html","access_key_id","access_key_secret","pool_key"));
  list.add(p("aliyun","green","moderation","reference_only","https://help.aliyun.com/zh/content-moderation/","access_key_id","access_key_secret","region"));
  list.add(p("amap","maps","map","reference_only","https://lbs.amap.com/api/webservice/summary","web_key","rest_key"));
  list.add(p("kdniao","logistics","logistics","reference_only","https://www.kdniao.com/documents","ebusiness_id","api_key"));
  list.add(p("sf","express","logistics","reference_only","https://open.sf-express.com/","partner_id","check_word","monthly_card"));
  list.add(p("cursor","agent-api","ai","reference_only","https://docs.cursor.com/en/cloud-agent/api/overview","api_key","api_base"));
  return List.copyOf(list);
 }
 public static Product credentialBundle(String prefix){if(!prefix.matches("[A-Z][A-Z0-9_]{2,39}"))throw new IllegalArgumentException("INTEGRATION_CREDENTIAL_PREFIX_INVALID");return new Product("credentials",prefix,"credentials","implemented","",List.of(new Field("APP_ID",false,false),new Field("MERCHANT_ID",false,false),new Field("MERCHANT_SERIAL",false,false),new Field("PLATFORM_SERIAL",false,false),new Field("PRIVATE_KEY_PEM",true,false),new Field("PLATFORM_PUBLIC_KEY_PEM",true,false),new Field("SIGNING_SECRET",true,false),new Field("BEARER_TOKEN",true,false),new Field("API_KEY",true,false)));}
 public static Product product(String vendor,String code){if(vendor.equals("credentials"))return credentialBundle(code);return all().stream().filter(p->p.vendor().equals(vendor)&&p.code().equals(code)).findFirst().orElseThrow(()->new IllegalArgumentException("INTEGRATION_UNKNOWN_PRODUCT"));}
}
