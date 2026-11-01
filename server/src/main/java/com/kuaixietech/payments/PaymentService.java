package com.kuaixietech.payments;

import com.kuaixietech.security.DomainAccessGuard;
import java.math.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import static com.kuaixietech.payments.PaymentModels.*;

@Service
public class PaymentService {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

  protected PaymentSecrets secrets=new PaymentSecrets(new com.kuaixietech.integration.IntegrationCredentials(null));
  @org.springframework.beans.factory.annotation.Autowired public void configureSecrets(PaymentSecrets secrets){this.secrets=secrets;}
  private final PaymentStore store;private final FxService fx;private final DomainAccessGuard guard;
  private final PaymentTransactions transactions;private final Map<String,Adapter> adapters;
  public PaymentService(PaymentStore store,FxService fx,DomainAccessGuard guard,PaymentTransactions transactions,List<Adapter> adapters){
    this.store=store;this.fx=fx;this.guard=guard;this.transactions=transactions;this.adapters=adapters.stream().collect(Collectors.toUnmodifiableMap(Adapter::id,a->a));
  }
  public List<Map<String,Object>> adapterInfo(){return adapters.values().stream().sorted(java.util.Comparator.comparing(Adapter::id)).map(adapter->Map.<String,Object>of("id",adapter.id(),"methods",adapter.methods(),"platforms",adapter.platforms(),"currencies",adapter.currencies())).toList();}
  Adapter adapter(Merchant merchant){Adapter adapter=adapters.get(merchant.adapter());if(adapter==null)throw new IllegalArgumentException("UNSUPPORTED_ADAPTER");return adapter;}
  public List<Map<String,Object>> capabilities(String platform){
    if(!Set.of("web","h5","mp_weixin","app").contains(platform))throw new IllegalArgumentException("INVALID_PLATFORM");
    var result=new ArrayList<Map<String,Object>>();
    for(Merchant merchant:store.merchants())if(merchant.enabled()&&merchant.platform().equals(platform)){
      Adapter adapter=adapter(merchant);boolean configured=adapter.configured(merchant);
      Set<String> available=Set.of();String status=configured?"configured":"credentials_missing";
      if(configured)try{available=adapter.availableCurrencies(merchant);}catch(RuntimeException e){status="provider_unavailable";}
      Set<String> supported=available;
      List<String> currencies=merchant.currencies().stream().filter(supported::contains).sorted().toList();
      result.add(Map.of("id",merchant.id(),"name",merchant.name(),"method",merchant.method(),"platform",platform,"currencies",currencies,"status",status,"settlementCurrency","CNY","spreadBps",merchant.spreadBps()));
    }
    return result;
  }
  public Quote quote(QuoteInput input){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("payment_quote");
    String user=guard.requireActiveProfile();CurrencyAmounts.currency(input.paymentCurrency());
    BigInteger base=CurrencyAmounts.minor(input.amountCny(),"CNY");
    if(base.signum()<=0||base.compareTo(BigInteger.valueOf(100_000_000))>0)throw new IllegalArgumentException("INVALID_CNY_AMOUNT");
    Merchant merchant=store.merchant(input.merchantId());
    if(merchant==null||!merchant.enabled())throw new IllegalArgumentException("MERCHANT_UNAVAILABLE");
    Adapter adapter=adapter(merchant);
    if(!adapter.configured(merchant)||!merchant.currencies().contains(input.paymentCurrency())||!adapter.availableCurrencies(merchant).contains(input.paymentCurrency()))throw new IllegalStateException("PAYMENT_CURRENCY_UNAVAILABLE");
    adapter.validatePayer(merchant,user);
    merchant=secrets.snapshot(merchant);
    Rate rate=fx.rate(input.paymentCurrency(),true);
    int spread=input.paymentCurrency().equals("CNY")?0:merchant.spreadBps();
    BigInteger converted=CurrencyAmounts.convert(base,"CNY",input.paymentCurrency(),rate.value(),spread);
    if(converted.signum()<=0||converted.toString().length()>24)throw new IllegalArgumentException("AMOUNT_OUTSIDE_CURRENCY_RANGE");
    Instant expiry=Instant.now().plusSeconds(merchant.quoteTtlSeconds());if(expiry.isAfter(rate.validUntil()))expiry=rate.validUntil();
    if(!expiry.isAfter(Instant.now().plusSeconds(15)))throw new IllegalStateException("FX_RATE_EXPIRING_RETRY");
    String id=UUID.randomUUID().toString();
    store.db.update("INSERT INTO t_payment_quotes(id,user_id,merchant_id,merchant_revision,merchant_snapshot,base_minor,payment_currency,payment_minor,payment_minor_digits,fx_rate,spread_bps,rate_source,rate_as_of,expires_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
      id,user,merchant.id(),merchant.revision(),store.json(merchant),base,input.paymentCurrency(),converted,CurrencyAmounts.currency(input.paymentCurrency()).getDefaultFractionDigits(),rate.value(),spread,rate.source(),Timestamp.from(rate.asOf()),Timestamp.from(expiry));
    return new Quote(id,merchant.id(),merchant.name(),merchant.method(),merchant.platform(),"CNY",CurrencyAmounts.major(base,"CNY"),input.paymentCurrency(),CurrencyAmounts.major(converted,input.paymentCurrency()),rate.value().toPlainString(),spread,rate.source(),rate.asOf(),expiry,"CNY");
  }
  public Payment create(ConfirmInput input){
    if(couplerMode!=null)couplerMode.requireLiveNewCall("payment_create");
    String user=guard.requireActiveProfile();Map<String,Object> payment;
    try{payment=transactions.reserve(user,input);}catch(DuplicateKeyException conflict){
      payment=store.row("SELECT * FROM t_currency_payments WHERE user_id=? AND request_id=?",user,input.requestId().toLowerCase(Locale.ROOT));
      if(payment==null||!input.quoteId().equals(payment.get("quote_id")))throw new IllegalArgumentException("IDEMPOTENCY_KEY_REUSED");
    }
    String id=PaymentStore.s(payment,"id");
    // Existing attempts are queried, never submitted again with a different external reference.
    if(!"creating".equals(payment.get("status"))||payment.get("checkout_kind")!=null)return view(payment);
    Map<String,Object> quote=store.quote(PaymentStore.s(payment,"quote_id"));
    if(quote==null||!PaymentTransactions.instant(quote.get("expires_at")).isAfter(Instant.now()))throw new IllegalArgumentException("QUOTE_EXPIRED");
    Merchant current=store.merchant(PaymentStore.s(payment,"merchant_id"));
    if(current==null||!current.enabled()||current.revision()!=((Number)quote.get("merchant_revision")).longValue())throw new IllegalArgumentException("MERCHANT_CHANGED_REQUOTE");
    if(!adapter(current).configured(current))throw new IllegalStateException("PAYMENT_CREDENTIALS_UNAVAILABLE");
    // A database claim prevents simultaneous callers from dispatching the same attempt twice.
    if(store.db.update("UPDATE t_currency_payments SET checkout_kind='dispatching' WHERE id=? AND status='creating' AND checkout_kind IS NULL",id)!=1)return view(store.payment(id));
    Merchant merchant=store.snapshot(PaymentStore.s(payment,"merchant_snapshot"));
    Checkout checkout=adapter(merchant).create(merchant,id,PaymentStore.s(payment,"payment_currency"),new BigDecimal(payment.get("payment_minor").toString()).toBigIntegerExact(),new BigDecimal(payment.get("base_minor").toString()).toBigIntegerExact(),PaymentTransactions.instant(quote.get("expires_at")));
    if(checkout==null||!Set.of("redirect","qr_text","wechat_jsapi","wechat_app","alipay_app").contains(checkout.kind()))throw new IllegalStateException("INVALID_CHECKOUT_RESPONSE");
    store.db.update("UPDATE t_currency_payments SET status='pending',checkout_kind=?,checkout_value=? WHERE id=? AND status='creating'",checkout.kind(),checkout.value(),id);
    return view(store.payment(id));
  }
  public List<Payment> history(){String user=guard.requireActiveProfile();return store.db.queryForList("SELECT * FROM t_currency_payments WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT 20",user).stream().map(this::view).toList();}
  public Payment status(String id){String user=guard.requireActiveProfile();Map<String,Object> payment=store.payment(id);if(payment==null||!user.equals(payment.get("user_id")))throw new IllegalArgumentException("PAYMENT_NOT_FOUND");if(!"paid".equals(payment.get("status")))refresh(payment);return view(store.payment(id));}
  boolean refresh(Map<String,Object> payment){
    String id=PaymentStore.s(payment,"id");
    try{Merchant merchant=store.snapshot(PaymentStore.s(payment,"merchant_snapshot"));boolean paid=transactions.settle(id,adapter(merchant).query(merchant,id));store.db.update("UPDATE t_currency_payments SET last_error_code=NULL WHERE id=?",id);return paid;}
    catch(RuntimeException failure){store.db.update("UPDATE t_currency_payments SET last_error_code='PROVIDER_QUERY_UNAVAILABLE' WHERE id=?",id);throw failure;}
    finally{store.db.update("UPDATE t_currency_payments SET last_checked_at=CURRENT_TIMESTAMP(3) WHERE id=?",id);}
  }
  public String callback(String merchantId,String id){
    Map<String,Object> payment=store.payment(id);
    if(payment==null||!merchantId.equals(payment.get("merchant_id")))throw new IllegalArgumentException("PAYMENT_NOT_FOUND");
    if("paid".equals(payment.get("status")))return store.snapshot(PaymentStore.s(payment,"merchant_snapshot")).adapter();
    // Callback content is never treated as proof. An authenticated, signature-verified
    // provider query must independently confirm reference, amount and currency first.
    return refresh(payment)?store.snapshot(PaymentStore.s(payment,"merchant_snapshot")).adapter():null;
  }
  Payment view(Map<String,Object> row){
    Merchant merchant=store.snapshot(PaymentStore.s(row,"merchant_snapshot"));String kind=PaymentStore.s(row,"checkout_kind");
    Checkout checkout=Set.of("redirect","qr_text","wechat_jsapi","wechat_app","alipay_app").contains(kind==null?"":kind)?new Checkout(kind,PaymentStore.s(row,"checkout_value")):null;
    return new Payment(PaymentStore.s(row,"id"),PaymentStore.s(row,"status"),"CNY",CurrencyAmounts.major(new BigDecimal(row.get("base_minor").toString()).toBigIntegerExact(),"CNY"),PaymentStore.s(row,"payment_currency"),new BigDecimal(new BigDecimal(row.get("payment_minor").toString()).toBigIntegerExact(),((Number)row.get("payment_minor_digits")).intValue()).toPlainString(),merchant.name(),checkout);
  }
}
