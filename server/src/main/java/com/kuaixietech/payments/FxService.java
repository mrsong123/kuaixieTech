package com.kuaixietech.payments;

import com.fasterxml.jackson.databind.*;
import java.math.*;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import static com.kuaixietech.payments.PaymentModels.*;

/** Rates have provenance and expiry. No fallback constants or invented exchange rates. */
@Service
public class FxService {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

  private com.kuaixietech.integration.IntegrationCredentials credentials=new com.kuaixietech.integration.IntegrationCredentials(null);
  @org.springframework.beans.factory.annotation.Autowired public void configureCredentials(com.kuaixietech.integration.IntegrationCredentials credentials){this.credentials=credentials;}
  private final PaymentStore store;private final PaymentHttp http;
  private final java.util.concurrent.ConcurrentHashMap<String,Rate> cache=new java.util.concurrent.ConcurrentHashMap<>();
  private final ObjectMapper json=new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
  public FxService(PaymentStore store,PaymentHttp http){this.store=store;this.http=http;}
  public Rate rate(String currency,boolean transaction){
    CurrencyAmounts.currency(currency);Instant now=Instant.now();
    if(currency.equals("CNY"))return new Rate("CNY","CNY",BigDecimal.ONE,"identity",now,now.plusSeconds(900));
    for(FxSource source:store.sources())if(source.enabled()&&(!transaction||source.transactionEnabled())){
      try{
        String key=source.toString()+":"+currency;Rate cached=cache.get(key);
        if(cached!=null&&cached.validUntil().isAfter(now)&&cached.asOf().isAfter(now.minusSeconds(source.maxAgeSeconds())))return cached;
        Rate fetched=fetch(source,currency,now);cache.put(key,fetched);return fetched;
      }catch(RuntimeException ignored){/* Try the next explicitly configured source; never invent a rate. */}
    }
    throw new IllegalStateException("FRESH_FX_RATE_UNAVAILABLE");
  }
  public Rate reference(String base,String target){
    CurrencyAmounts.currency(base);CurrencyAmounts.currency(target);
    if(base.equals(target)){Instant now=Instant.now();return new Rate(base,target,BigDecimal.ONE,"identity",now,now.plusSeconds(900));}
    Rate from=rate(base,false),to=rate(target,false);
    return new Rate(base,target,to.value().divide(from.value(),18,RoundingMode.HALF_UP),from.source()+"/"+to.source(),from.asOf().isBefore(to.asOf())?from.asOf():to.asOf(),from.validUntil().isBefore(to.validUntil())?from.validUntil():to.validUntil());
  }
  Rate fetch(FxSource source,String currency,Instant now){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("fx_reference");
    try{
      boolean open="open_er_api".equals(source.kind());
      if(!open&&!"rates_json_v1".equals(source.kind()))throw new IllegalArgumentException("UNSUPPORTED_FX_SOURCE");
      URI endpoint=http.endpoint(open?"https://open.er-api.com/v6/latest/CNY":source.endpoint(),!open);
      var builder=HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(10)).header("Accept","application/json").GET();
      if(!open&&source.credentialPrefix()!=null&&!source.credentialPrefix().isBlank()){
        String token=credentials.required(source.credentialPrefix(),"BEARER_TOKEN");
        if(token==null||token.isBlank())throw new IllegalStateException("FX_CREDENTIALS_NOT_CONFIGURED");builder.header("Authorization","Bearer "+token);
      }
      JsonNode response=json.readTree(http.send(builder.build()));
      String base=response.path(open?"base_code":"base").asText();
      if(!"CNY".equals(base)||open&&!"success".equals(response.path("result").asText()))throw new IllegalStateException("FX_BASE_MISMATCH");
      Instant asOf=open?Instant.ofEpochSecond(response.path("time_last_update_unix").asLong()):Instant.parse(response.path("asOf").asText());
      Instant expiry=open?Instant.ofEpochSecond(response.path("time_next_update_unix").asLong()):Instant.parse(response.path("expiresAt").asText());
      if(asOf.isAfter(now.plusSeconds(30))||asOf.isBefore(now.minusSeconds(source.maxAgeSeconds()))||!expiry.isAfter(now))throw new IllegalStateException("STALE_FX_RATE");
      JsonNode node=response.path(open?"rates":"rates").get(currency);
      if(node==null||!node.isNumber()&&!node.isTextual())throw new IllegalStateException("FX_CURRENCY_UNAVAILABLE");
      BigDecimal value=new BigDecimal(node.asText());
      if(value.signum()<=0||value.compareTo(new BigDecimal("1000000000000"))>=0||value.precision()>30||Math.abs(value.scale())>18)throw new IllegalStateException("INVALID_FX_RATE");
      return new Rate(base,currency,value,source.id(),asOf,expiry);
    }catch(IllegalStateException e){throw e;}catch(Exception e){throw new IllegalStateException("FX_SOURCE_UNAVAILABLE");}
  }
}
