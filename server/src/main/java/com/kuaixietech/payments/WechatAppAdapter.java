package com.kuaixietech.payments;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Component;
import static com.kuaixietech.payments.PaymentModels.*;
@Component
public class WechatAppAdapter extends WechatNativeAdapter {
  public WechatAppAdapter(PaymentHttp http){super(http);}
  @Override public String id(){return "wechat_app_v3";}
  @Override public Set<String> platforms(){return Set.of("app");}
  @Override public Checkout create(Merchant m,String reference,String currency,BigInteger amount,BigInteger baseCnyMinor,Instant expiresAt){
    if(!currency.equals("CNY"))throw new IllegalArgumentException("UNSUPPORTED_PAYMENT_CURRENCY");
    try{
      String appId=secrets.value(m,"APP_ID"),merchant=secrets.value(m,"MERCHANT_ID");
      var response=call(m,"POST","/v3/pay/transactions/app",json.writeValueAsString(Map.of("appid",appId,"mchid",merchant,"description","Kuaixie wallet recharge","out_trade_no",reference,
        "notify_url",http.endpoint(m.settings().get("notifyUrl"),false).toString().replaceAll("/+$","")+"/"+m.id()+"/"+reference,"time_expire",expiresAt.atOffset(ZoneOffset.UTC).toString(),"amount",Map.of("total",amount.longValueExact(),"currency",currency))));
      String prepay=response.path("prepay_id").asText();if(prepay.isBlank())throw new IllegalStateException("INVALID_PREPAY_RESPONSE");
      String stamp=Long.toString(Instant.now().getEpochSecond()),nonce=UUID.randomUUID().toString().replace("-","");
      String signature=PaymentSecrets.sign(secrets.privateKey(m),appId+"\n"+stamp+"\n"+nonce+"\n"+prepay+"\n");
      return new Checkout("wechat_app",json.writeValueAsString(Map.of("appid",appId,"partnerid",merchant,"prepayid",prepay,"package","Sign=WXPay","noncestr",nonce,"timestamp",stamp,"sign",signature)));
    }catch(IllegalArgumentException|IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("WECHAT_PREPAY_FAILED");}
  }
}
