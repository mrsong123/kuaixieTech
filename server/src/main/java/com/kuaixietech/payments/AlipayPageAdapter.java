package com.kuaixietech.payments;

import com.fasterxml.jackson.databind.*;
import java.math.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import static com.kuaixietech.payments.PaymentModels.*;

/** Domestic Alipay page/WAP products, RSA2. Cross-border products are separate merchant capabilities. */
@Component
public class AlipayPageAdapter implements Adapter {
  protected PaymentSecrets secrets=new PaymentSecrets(new com.kuaixietech.integration.IntegrationCredentials(null));
  @org.springframework.beans.factory.annotation.Autowired public void configureSecrets(PaymentSecrets secrets){this.secrets=secrets;}
  private final PaymentHttp http;private final ObjectMapper json=new ObjectMapper();
  public AlipayPageAdapter(PaymentHttp http){this.http=http;}
  public String id(){return "alipay_rsa2";}
  public Set<String> methods(){return Set.of("alipay");}
  public Set<String> platforms(){return Set.of("web","h5","app");}
  public Set<String> currencies(){return Set.of("CNY");}
  public boolean configured(Merchant m){return secrets.present(m,"APP_ID","PRIVATE_KEY_FILE","PLATFORM_PUBLIC_KEY_FILE")&&m.settings().containsKey("notifyUrl")&&m.settings().containsKey("returnUrl");}
  public Checkout create(Merchant m,String reference,String currency,BigInteger amount,BigInteger baseCnyMinor,Instant expiresAt){
    if(!currency.equals("CNY"))throw new IllegalArgumentException("UNSUPPORTED_PAYMENT_CURRENCY");
    boolean wap=m.platform().equals("h5"),app=m.platform().equals("app");
    var params=parameters(m,app?"alipay.trade.app.pay":wap?"alipay.trade.wap.pay":"alipay.trade.page.pay",Map.of("out_trade_no",reference,"total_amount",CurrencyAmounts.major(amount,"CNY"),"subject","Kuaixie wallet recharge","time_expire",LocalDateTime.ofInstant(expiresAt,ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),"product_code",app?"QUICK_MSECURITY_PAY":wap?"QUICK_WAP_WAY":"FAST_INSTANT_TRADE_PAY"));
    params.put("notify_url",http.endpoint(m.settings().get("notifyUrl"),false).toString().replaceAll("/+$","")+"/"+m.id()+"/"+reference);
    params.put("return_url",http.endpoint(m.settings().get("returnUrl"),false).toString());
    sign(m,params);if(app)return new Checkout("alipay_app",form(params));return new Checkout("redirect","https://openapi.alipay.com/gateway.do?"+form(params));
  }
  public ProviderPayment query(Merchant m,String reference){
    var params=parameters(m,"alipay.trade.query",Map.of("out_trade_no",reference));sign(m,params);
    String body=http.send(HttpRequest.newBuilder(URI.create("https://openapi.alipay.com/gateway.do")).timeout(Duration.ofSeconds(15)).header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8").POST(HttpRequest.BodyPublishers.ofString(form(params))).build());
    try{
      JsonNode envelope=json.readTree(body);String raw=responseObject(body,"alipay_trade_query_response");
      if(!PaymentSecrets.verify(secrets.publicKey(m,"PLATFORM_PUBLIC_KEY_FILE"),raw,envelope.path("sign").asText()))throw new IllegalStateException("INVALID_PROVIDER_SIGNATURE");
      JsonNode result=envelope.path("alipay_trade_query_response");
      if(!"10000".equals(result.path("code").asText()))throw new IllegalStateException("ALIPAY_QUERY_REJECTED");
      boolean paid=Set.of("TRADE_SUCCESS","TRADE_FINISHED").contains(result.path("trade_status").asText());
      return new ProviderPayment(result.path("out_trade_no").asText(),result.path("trade_no").asText(),"CNY",CurrencyAmounts.minor(result.path("total_amount").asText(),"CNY"),paid);
    }catch(IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("ALIPAY_QUERY_FAILED");}
  }
  private TreeMap<String,String> parameters(Merchant m,String method,Map<String,Object> content){
    try{var params=new TreeMap<String,String>();params.put("app_id",secrets.value(m,"APP_ID"));params.put("method",method);params.put("format","JSON");params.put("charset","utf-8");params.put("sign_type","RSA2");params.put("timestamp",LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));params.put("version","1.0");params.put("biz_content",json.writeValueAsString(content));return params;}
    catch(Exception e){throw new IllegalStateException("ALIPAY_REQUEST_CONFIGURATION_INVALID");}
  }
  private void sign(Merchant m,TreeMap<String,String> params){params.put("sign",PaymentSecrets.sign(secrets.privateKey(m),params.entrySet().stream().map(e->e.getKey()+"="+e.getValue()).collect(Collectors.joining("&"))));}
  private String form(Map<String,String> params){return params.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(Collectors.joining("&"));}
  /** Verify the exact signed response object, preserving whitespace and escaped text. */
  static String responseObject(String body,String key){
    int index=body.indexOf('"'+key+'"');if(index<0)throw new IllegalArgumentException("RESPONSE_OBJECT_MISSING");
    index=body.indexOf(':',index)+1;while(index<body.length()&&Character.isWhitespace(body.charAt(index)))index++;
    int start=index,depth=0;boolean quoted=false,escaped=false;
    if(index>=body.length()||body.charAt(index)!='{')throw new IllegalArgumentException("INVALID_RESPONSE_OBJECT");
    for(;index<body.length();index++){
      char c=body.charAt(index);if(quoted){if(escaped)escaped=false;else if(c=='\\')escaped=true;else if(c=='"')quoted=false;}
      else if(c=='"')quoted=true;else if(c=='{')depth++;else if(c=='}'&&--depth==0)return body.substring(start,index+1);
    }
    throw new IllegalArgumentException("INVALID_RESPONSE_OBJECT");
  }
}
