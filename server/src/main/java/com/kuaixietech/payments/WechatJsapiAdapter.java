package com.kuaixietech.payments;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Component;
import static com.kuaixietech.payments.PaymentModels.*;
@Component
public class WechatJsapiAdapter extends WechatNativeAdapter {
  private final PaymentStore store;
  public WechatJsapiAdapter(PaymentHttp http,PaymentStore store){super(http);this.store=store;}
  @Override public String id(){return "wechat_jsapi_v3";}
  @Override public Set<String> platforms(){return Set.of("mp_weixin");}
  @Override public void validatePayer(Merchant merchant,String user){
    var row=store.row("SELECT openid FROM t_profiles WHERE id=? AND deleted_at IS NULL",user);
    if(row==null||PaymentStore.s(row,"openid")==null||PaymentStore.s(row,"openid").isBlank())throw new IllegalArgumentException("WECHAT_LOGIN_REQUIRED");
  }
  @Override public Checkout create(Merchant m,String reference,String currency,BigInteger amount,BigInteger baseCnyMinor,Instant expiresAt){
    if(!currency.equals("CNY"))throw new IllegalArgumentException("UNSUPPORTED_PAYMENT_CURRENCY");
    var owner=store.row("SELECT u.openid FROM t_currency_payments p JOIN t_profiles u ON u.id=p.user_id WHERE p.id=?",reference);
    String openid=owner==null?null:PaymentStore.s(owner,"openid");
    if(openid==null||openid.isBlank())throw new IllegalArgumentException("WECHAT_LOGIN_REQUIRED");
    try{
      String appId=secrets.value(m,"APP_ID");
      var response=call(m,"POST","/v3/pay/transactions/jsapi",json.writeValueAsString(Map.of("appid",appId,"mchid",secrets.value(m,"MERCHANT_ID"),"description","Kuaixie wallet recharge","out_trade_no",reference,
        "notify_url",http.endpoint(m.settings().get("notifyUrl"),false).toString().replaceAll("/+$","")+"/"+m.id()+"/"+reference,"time_expire",expiresAt.atOffset(ZoneOffset.UTC).toString(),"amount",Map.of("total",amount.longValueExact(),"currency",currency),"payer",Map.of("openid",openid))));
      String prepay=response.path("prepay_id").asText();if(prepay.isBlank())throw new IllegalStateException("INVALID_PREPAY_RESPONSE");
      String stamp=Long.toString(Instant.now().getEpochSecond()),nonce=UUID.randomUUID().toString().replace("-",""),pack="prepay_id="+prepay;
      String signature=PaymentSecrets.sign(secrets.privateKey(m),appId+"\n"+stamp+"\n"+nonce+"\n"+pack+"\n");
      return new Checkout("wechat_jsapi",json.writeValueAsString(Map.of("timeStamp",stamp,"nonceStr",nonce,"package",pack,"signType","RSA","paySign",signature)));
    }catch(IllegalArgumentException|IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("WECHAT_PREPAY_FAILED");}
  }
}
