package com.kuaixietech.payments;

import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Component;
import static com.kuaixietech.payments.PaymentModels.*;

/** WeChat Pay API v3 native QR product. CNY only; other WeChat products need their own adapter. */
@Component
public class WechatNativeAdapter implements Adapter {
  protected PaymentSecrets secrets=new PaymentSecrets(new com.kuaixietech.integration.IntegrationCredentials(null));
  @org.springframework.beans.factory.annotation.Autowired public void configureSecrets(PaymentSecrets secrets){this.secrets=secrets;}
  protected final PaymentHttp http;protected final ObjectMapper json=new ObjectMapper();
  public WechatNativeAdapter(PaymentHttp http){this.http=http;}
  public String id(){return "wechat_native_v3";}
  public Set<String> methods(){return Set.of("wechat");}
  public Set<String> platforms(){return Set.of("web");}
  public Set<String> currencies(){return Set.of("CNY");}
  public boolean configured(Merchant m){return secrets.present(m,"MERCHANT_ID","APP_ID","MERCHANT_SERIAL","PRIVATE_KEY_FILE","PLATFORM_SERIAL","PLATFORM_PUBLIC_KEY_FILE")&&m.settings().containsKey("notifyUrl");}
  public Checkout create(Merchant m,String reference,String currency,BigInteger amount,BigInteger baseCnyMinor,Instant expiresAt){
    if(!"CNY".equals(currency))throw new IllegalArgumentException("UNSUPPORTED_PAYMENT_CURRENCY");
    try{
      String notify=http.endpoint(m.settings().get("notifyUrl"),false).toString().replaceAll("/+$","")+"/"+m.id()+"/"+reference;
      String body=json.writeValueAsString(Map.of("appid",secrets.value(m,"APP_ID"),"mchid",secrets.value(m,"MERCHANT_ID"),
        "description","Kuaixie wallet CNY recharge","out_trade_no",reference,"notify_url",notify,"time_expire",expiresAt.atOffset(ZoneOffset.UTC).toString(),
        "amount",Map.of("total",amount.longValueExact(),"currency",currency)));
      JsonNode result=call(m,"POST","/v3/pay/transactions/native",body);
      String url=result.path("code_url").asText();
      if(!url.startsWith("weixin://wxpay/"))throw new IllegalStateException("INVALID_CHECKOUT_RESPONSE");
      return new Checkout("qr_text",url);
    }catch(IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("WECHAT_CREATE_FAILED");}
  }
  public ProviderPayment query(Merchant m,String reference){
    String merchant=secrets.value(m,"MERCHANT_ID");
    JsonNode result=call(m,"GET","/v3/pay/transactions/out-trade-no/"+reference+"?mchid="+merchant,"");
    if(!merchant.equals(result.path("mchid").asText())||!secrets.value(m,"APP_ID").equals(result.path("appid").asText()))throw new IllegalStateException("MERCHANT_MISMATCH");
    return new ProviderPayment(result.path("out_trade_no").asText(),result.path("transaction_id").asText(),result.path("amount").path("currency").asText(),
      result.path("amount").path("total").bigIntegerValue(),"SUCCESS".equals(result.path("trade_state").asText()));
  }
  protected JsonNode call(Merchant m,String method,String path,String body){
    try{
      String timestamp=Long.toString(Instant.now().getEpochSecond()),nonce=UUID.randomUUID().toString().replace("-","");
      String signature=PaymentSecrets.sign(secrets.privateKey(m),method+"\n"+path+"\n"+timestamp+"\n"+nonce+"\n"+body+"\n");
      String auth="WECHATPAY2-SHA256-RSA2048 mchid=\""+secrets.value(m,"MERCHANT_ID")+"\",nonce_str=\""+nonce+"\",timestamp=\""+timestamp+"\",serial_no=\""+secrets.value(m,"MERCHANT_SERIAL")+"\",signature=\""+signature+"\"";
      var builder=HttpRequest.newBuilder(URI.create("https://api.mch.weixin.qq.com"+path)).timeout(Duration.ofSeconds(15))
        .header("Authorization",auth).header("Accept","application/json").header("Content-Type","application/json")
        .method(method,body.isEmpty()?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body));
      var response=http.exchange(builder.build());
      String serial=response.headers().firstValue("Wechatpay-Serial").orElse(""),time=response.headers().firstValue("Wechatpay-Timestamp").orElse(""),n=response.headers().firstValue("Wechatpay-Nonce").orElse(""),sig=response.headers().firstValue("Wechatpay-Signature").orElse("");
      if(!secrets.value(m,"PLATFORM_SERIAL").equals(serial)||Math.abs(Instant.now().getEpochSecond()-Long.parseLong(time))>300||
        !PaymentSecrets.verify(secrets.publicKey(m,"PLATFORM_PUBLIC_KEY_FILE"),time+"\n"+n+"\n"+response.body()+"\n",sig))throw new IllegalStateException("INVALID_PROVIDER_SIGNATURE");
      return json.readTree(response.body());
    }catch(IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("WECHAT_QUERY_FAILED");}
  }
}
