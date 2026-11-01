package com.kuaixietech.payments;

import com.fasterxml.jackson.databind.*;
import java.math.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import static com.kuaixietech.payments.PaymentModels.*;

/** Explicit bridge contract for a chosen bank/acquirer. Never handles PAN/CVV. */
@Component
public class HostedBankAdapter implements Adapter {
  protected PaymentSecrets secrets=new PaymentSecrets(new com.kuaixietech.integration.IntegrationCredentials(null));
  @org.springframework.beans.factory.annotation.Autowired public void configureSecrets(PaymentSecrets secrets){this.secrets=secrets;}
  private final PaymentHttp http;private final ObjectMapper json=new ObjectMapper();
  public HostedBankAdapter(PaymentHttp http){this.http=http;}
  public String id(){return "hosted_bank_v1";}
  public Set<String> methods(){return Set.of("bank_card","credit_card");}
  public Set<String> platforms(){return Set.of("web","h5","app","mp_weixin");}
  public Set<String> currencies(){return Set.of();}
  public boolean configured(Merchant m){
    try{return secrets.present(m,"MERCHANT_ID","SIGNING_SECRET")&&secrets.value(m,"SIGNING_SECRET").length()>=32&&http.endpoint(m.settings().get("gatewayUrl"),true)!=null&&m.settings().containsKey("checkoutHosts")&&m.settings().containsKey("notifyUrl");}
    catch(Exception e){return false;}
  }
  @Override public Set<String> availableCurrencies(Merchant m){
    JsonNode capabilities=call(m,"/capabilities",Map.of());
    if(!supports(capabilities.path("settlementCurrencies"),"CNY")||!supports(capabilities.path("methods"),m.method())||!supports(capabilities.path("platforms"),m.platform()))return Set.of();
    Set<String> result=new HashSet<>();
    capabilities.path("currencies").forEach(node->{try{result.add(CurrencyAmounts.currency(node.asText()).getCurrencyCode());}catch(Exception ignored){}});
    return result;
  }
  public Checkout create(Merchant m,String reference,String currency,BigInteger amount,BigInteger baseCnyMinor,Instant expiresAt){
    if(!availableCurrencies(m).contains(currency))throw new IllegalStateException("CURRENCY_NOT_SUPPORTED_BY_ACQUIRER");
    JsonNode result=call(m,"/payments",Map.of("reference",reference,"currency",currency,"amountMinor",amount.toString(),"method",m.method(),"platform",m.platform(),"settlementCurrency","CNY","baseCnyMinor",baseCnyMinor.toString(),"expiresAt",expiresAt.getEpochSecond(),"notifyUrl",http.endpoint(m.settings().get("notifyUrl"),false).toString().replaceAll("/+$","")+"/"+m.id()+"/"+reference));
    if(!reference.equals(result.path("reference").asText()))throw new IllegalStateException("REFERENCE_MISMATCH");
    String value=result.path("checkoutUrl").asText();URI checkout=http.endpoint(value,false);
    Set<String> hosts=new HashSet<>(Arrays.asList(m.settings().get("checkoutHosts").split(",")));
    if(!hosts.contains(checkout.getHost()))throw new IllegalStateException("CHECKOUT_HOST_NOT_ALLOWED");
    return new Checkout("redirect",value);
  }
  public ProviderPayment query(Merchant m,String reference){
    JsonNode result=call(m,"/query",Map.of("reference",reference));
    if(!"CNY".equals(result.path("settlementCurrency").asText()))throw new IllegalStateException("SETTLEMENT_CURRENCY_MISMATCH");
    return new ProviderPayment(result.path("reference").asText(),result.path("transactionId").asText(),result.path("currency").asText(),new BigInteger(result.path("amountMinor").asText()),"paid".equals(result.path("status").asText()));
  }
  private boolean supports(JsonNode values,String value){for(JsonNode item:values)if(value.equals(item.asText()))return true;return false;}
  private JsonNode call(Merchant m,String path,Map<String,Object> payload){
    try{
      Map<String,Object> input=new LinkedHashMap<>(payload);input.put("merchant",secrets.value(m,"MERCHANT_ID"));input.put("requestedAt",Instant.now().getEpochSecond());input.put("nonce",UUID.randomUUID().toString());
      String encoded=Base64.getUrlEncoder().withoutPadding().encodeToString(json.writeValueAsBytes(input));
      String secret=secrets.value(m,"SIGNING_SECRET");
      String request=json.writeValueAsString(Map.of("payload",encoded,"signature",mac(secret,encoded)));
      URI endpoint=http.endpoint(m.settings().get("gatewayUrl").replaceAll("/+$","")+path,true);
      JsonNode envelope=json.readTree(http.send(HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(15)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(request)).build()));
      String received=envelope.path("payload").asText(),signature=envelope.path("signature").asText();
      if(!MessageDigest.isEqual(mac(secret,received).getBytes(StandardCharsets.US_ASCII),signature.getBytes(StandardCharsets.US_ASCII)))throw new IllegalStateException("INVALID_PROVIDER_SIGNATURE");
      JsonNode result=json.readTree(Base64.getUrlDecoder().decode(received));
      long now=Instant.now().getEpochSecond();
      if(!secrets.value(m,"MERCHANT_ID").equals(result.path("merchant").asText())||result.path("issuedAt").asLong()>now+30||result.path("issuedAt").asLong()<now-300||result.path("expiresAt").asLong()<now||!input.get("nonce").equals(result.path("nonce").asText()))throw new IllegalStateException("INVALID_GATEWAY_RESPONSE");
      return result;
    }catch(IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("BANK_GATEWAY_UNAVAILABLE");}
  }
  static String mac(String key,String payload){
    try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));}
    catch(Exception e){throw new IllegalStateException("SIGNING_FAILED");}
  }
}
