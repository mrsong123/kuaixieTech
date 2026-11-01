package com.kuaixietech.insurance;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.payments.CurrencyAmounts;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import static com.kuaixietech.insurance.InsuranceModels.*;
@Service
public class InsuranceService {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

 private final InsuranceStore store;private final InsuranceCatalog catalog;private final InsuranceTransactions transactions;private final DomainAccessGuard guard;
 public InsuranceService(InsuranceStore store,InsuranceCatalog catalog,InsuranceTransactions transactions,DomainAccessGuard guard){this.store=store;this.catalog=catalog;this.transactions=transactions;this.guard=guard;}
 public Quote quote(QuoteInput input){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("insurance_quote");
  String user=guard.requireVerifiedBuyer();Product p=catalog.available(input.productId());
  String platform=input.platform()==null?"web":input.platform();if(!catalog.adapter(p).supportedPlatforms(p).contains(platform))throw new IllegalArgumentException("INSURANCE_PLATFORM_UNSUPPORTED");
  BigDecimal declared=new BigDecimal(input.declaredValue());if(declared.signum()<=0||declared.compareTo(new BigDecimal("1000000"))>0)throw new IllegalArgumentException("INSURANCE_VALUE_INVALID");
  if("trip".equals(input.subjectType())){
   var trip=store.row("SELECT t.id FROM t_trips t JOIN t_profiles s ON s.id=t.owner_id WHERE t.id=? AND t.owner_id<>? AND t.status='active' AND t.visibility='public' AND t.verify_status='approved' AND t.return_date>=CURRENT_DATE() AND s.carrier_verify_status='approved' AND s.deleted_at IS NULL AND (s.banned_until IS NULL OR s.banned_until<=CURRENT_TIMESTAMP(3))",input.subjectId(),user);
   if(trip==null)throw new IllegalArgumentException("INSURANCE_SUBJECT_UNAVAILABLE");
  }else if("pk_quote".equals(input.subjectType())){
   var offer=store.row("SELECT q.price,d.title,d.item_desc FROM t_quotes q JOIN t_demands d ON d.id=q.demand_id WHERE q.id=? AND d.publisher_id=? AND d.status IN('open','awarded_pending') AND q.status='pending'",input.subjectId(),user);
   if(offer==null||new BigDecimal(offer.get("price").toString()).compareTo(declared)!=0||!itemName(offer).equals(input.itemName().trim()))throw new IllegalArgumentException("INSURANCE_SUBJECT_UNAVAILABLE");
  }else if("order".equals(input.subjectType())){
   var order=store.row("SELECT id,item_name,goods_amount,agreed_json FROM t_orders WHERE id=? AND buyer_id=? AND status IN('requesting','negotiating','vendor_deposit_pending','locked')",input.subjectId(),user);
   if(order==null||new BigDecimal(order.get("goods_amount").toString()).compareTo(declared)!=0||!input.itemName().trim().equals(order.get("item_name")))throw new IllegalArgumentException("INSURANCE_SUBJECT_UNAVAILABLE");
   try{if(!"yes".equals(new com.fasterxml.jackson.databind.ObjectMapper().readTree(order.get("agreed_json").toString()).path("insurance_choice").asText()))throw new IllegalArgumentException("INSURANCE_SUBJECT_UNAVAILABLE");}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("INSURANCE_INVALID_ORDER_SNAPSHOT");}
  }else throw new IllegalArgumentException("INSURANCE_SUBJECT_UNAVAILABLE");
  String id=UUID.randomUUID().toString();ProviderQuote q=catalog.adapter(p).quote(p,input,id);
  if(q==null||q.reference()==null||q.reference().isBlank()||q.reference().length()>160||q.declaredValue()==null||q.declaredValue().compareTo(declared)!=0||q.premium()==null||q.premium().signum()<0||q.termsVersion()==null||q.termsVersion().isBlank()||q.termsVersion().length()>160||q.expiresAt()==null||!q.expiresAt().isAfter(Instant.now())||q.expiresAt().isAfter(Instant.now().plusSeconds(900)))throw new IllegalStateException("INSURER_QUOTE_INVALID");
  CurrencyAmounts.minor(q.premium().toPlainString(),q.currency());
  if(q.premium().scale()>6||q.premium().precision()>24)throw new IllegalStateException("INSURER_PREMIUM_PRECISION_UNSUPPORTED");
  store.db.update("INSERT INTO t_goods_insurance_quotes(id,user_id,subject_type,platform,subject_id,item_name,product_id,product_revision,declared_value,premium,currency,provider_ref,terms_version,snapshot_json,expires_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",id,user,input.subjectType(),platform,input.subjectId(),input.itemName().trim(),p.id(),p.revision(),declared,q.premium(),q.currency(),q.reference(),q.termsVersion(),store.json(p),Timestamp.from(q.expiresAt()));
  return new Quote(id,p.id(),p.insurerName(),p.name(),p.kind(),declared.toPlainString(),amount(q.premium().toPlainString(),q.currency()),q.currency(),p.termsUrl(),p.exclusionsUrl(),p.claimsUrl(),q.termsVersion(),q.expiresAt(),"insurer_direct","not_issued");
 }
 private static String amount(String value,String currency){return CurrencyAmounts.major(CurrencyAmounts.minor(value,currency),currency);}
 public static String itemName(Map<String,Object> demand){String name=InsuranceStore.s(demand,"item_desc");if(name==null||name.isBlank())name=InsuranceStore.s(demand,"title");name=name.trim();return name.length()>120?name.substring(0,120):name;}
 public Map<String,Object> pkSubject(String demand,String quote){String user=guard.requireVerifiedBuyer();var row=store.row("SELECT q.id,q.price,d.title,d.item_desc FROM t_quotes q JOIN t_demands d ON d.id=q.demand_id WHERE q.id=? AND d.id=? AND d.publisher_id=? AND q.status='pending' AND d.status IN('open','awarded_pending')",quote,demand,user);if(row==null)throw new IllegalArgumentException("INSURANCE_SUBJECT_UNAVAILABLE");return Map.of("subjectId",quote,"subjectType","pk_quote","itemName",itemName(row),"declaredValue",row.get("price").toString());}
 public Quote ownQuote(String id){String user=guard.requireActiveProfile();var q=store.quote(id);if(q==null||!user.equals(q.get("user_id")))throw new IllegalArgumentException("INSURANCE_QUOTE_UNAVAILABLE");Product p=store.parse(q.get("snapshot_json"),Product.class);return new Quote(id,p.id(),p.insurerName(),p.name(),p.kind(),q.get("declared_value").toString(),amount(q.get("premium").toString(),q.get("currency").toString()),q.get("currency").toString(),p.termsUrl(),p.exclusionsUrl(),p.claimsUrl(),q.get("terms_version").toString(),InsuranceStore.instant(q.get("expires_at")),"insurer_direct","not_issued");}
 public Application apply(ApplyInput input){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("insurance_apply");
  String user=guard.requireVerifiedBuyer();Map<String,Object> app;
  try{app=transactions.reserve(user,input);}catch(DuplicateKeyException e){app=store.row("SELECT * FROM t_goods_insurance_applications WHERE order_id=? AND user_id=?",input.orderId(),user);if(app==null||!input.quoteId().equals(app.get("quote_id")))throw new IllegalArgumentException("INSURANCE_REQUEST_CONFLICT");}
  String id=InsuranceStore.s(app,"id");
  if("reserved".equals(app.get("status"))){
   var q=store.quote(InsuranceStore.s(app,"quote_id"));Product p=store.parse(q.get("snapshot_json"),Product.class);
   Product current=catalog.available(p.id());if(current.revision()!=((Number)q.get("product_revision")).longValue()||!InsuranceStore.instant(q.get("expires_at")).isAfter(Instant.now()))throw new IllegalArgumentException("INSURANCE_QUOTE_EXPIRED");
   if(store.db.update("UPDATE t_goods_insurance_applications SET status='dispatching' WHERE id=? AND status='reserved'",id)==1){
    try{var order=store.row("SELECT id,item_name,item_spec,quantity,unit,goods_amount,contact_name,contact_phone,address,trip_id,status FROM t_orders WHERE id=? AND buyer_id=?",input.orderId(),user);
     if(order==null||!Set.of("requesting","negotiating","vendor_deposit_pending","locked").contains(order.get("status")))throw new IllegalStateException("INSURANCE_PURCHASE_TOO_LATE");
     OrderRisk risk=new OrderRisk(input.orderId(),InsuranceStore.s(order,"item_name"),InsuranceStore.s(order,"item_spec"),((Number)order.get("quantity")).intValue(),InsuranceStore.s(order,"unit"),order.get("goods_amount").toString(),"CNY",InsuranceStore.s(order,"contact_name"),InsuranceStore.s(order,"contact_phone"),InsuranceStore.s(order,"address"),InsuranceStore.s(order,"trip_id"),InsuranceStore.s(q,"platform"));
     Checkout checkout=catalog.adapter(p).apply(p,id,risk,new ProviderQuote(InsuranceStore.s(q,"provider_ref"),new BigDecimal(q.get("declared_value").toString()),new BigDecimal(q.get("premium").toString()),InsuranceStore.s(q,"currency"),InsuranceStore.s(q,"terms_version"),InsuranceStore.instant(q.get("expires_at"))));
     if(checkout.applicationRef()==null||checkout.applicationRef().isBlank()||checkout.applicationRef().length()>160)throw new IllegalStateException("INSURER_CHECKOUT_INVALID");
     InsuranceUrlPolicy.require(checkout.url(),p.checkoutHosts());
     store.db.update("UPDATE t_goods_insurance_applications SET status='awaiting_provider',provider_application_ref=?,checkout_url=? WHERE id=? AND status='dispatching'",checkout.applicationRef(),checkout.url(),id);
    }catch(Exception e){store.db.update("UPDATE t_goods_insurance_applications SET last_error_code='INSURER_RESULT_UNCONFIRMED' WHERE id=?",id);throw new IllegalStateException("INSURER_RESULT_UNCONFIRMED");}
   }
  }
  return view(store.application(id));
 }
 public Application record(String id){String user=guard.requireActiveProfile();var app=store.row("SELECT * FROM t_goods_insurance_applications WHERE id=? AND user_id=?",id,user);if(app==null)throw new IllegalArgumentException("INSURANCE_APPLICATION_UNAVAILABLE");return view(app);}
 public Application status(String id){String user=guard.requireActiveProfile();var app=store.row("SELECT * FROM t_goods_insurance_applications WHERE id=? AND user_id=?",id,user);if(app==null)throw new IllegalArgumentException("INSURANCE_APPLICATION_UNAVAILABLE");if(!"reserved".equals(app.get("status")))refresh(app);return view(store.application(id));}
 public Map<String,Object> forOrder(String orderId){String user=guard.requireActiveProfile();if(store.row("SELECT id FROM t_orders WHERE id=? AND buyer_id=?",orderId,user)==null)throw new IllegalArgumentException("INSURANCE_ORDER_UNAVAILABLE");var app=store.row("SELECT * FROM t_goods_insurance_applications WHERE order_id=? AND user_id=?",orderId,user);Map<String,Object> result=new HashMap<>();result.put("application",app==null?null:view(app));return result;}
 public List<Application> mine(){String user=guard.requireActiveProfile();return store.db.queryForList("SELECT * FROM t_goods_insurance_applications WHERE user_id=? ORDER BY created_at DESC,id DESC LIMIT 50",user).stream().map(this::view).toList();}
 void refresh(Map<String,Object> app){
  var quote=store.quote(InsuranceStore.s(app,"quote_id"));Product p=store.parse(quote.get("snapshot_json"),Product.class);
  try{var receipt=catalog.adapter(p).query(p,InsuranceStore.s(app,"id"),InsuranceStore.s(app,"provider_application_ref"));transactions.receipt(InsuranceStore.s(app,"id"),receipt);}catch(Exception e){store.db.update("UPDATE t_goods_insurance_applications SET last_checked_at=CURRENT_TIMESTAMP(3),last_error_code='INSURER_QUERY_UNCONFIRMED' WHERE id=?",app.get("id"));throw new IllegalStateException("INSURER_QUERY_UNCONFIRMED");}
 }
 private Application view(Map<String,Object> app){
  var q=store.quote(InsuranceStore.s(app,"quote_id"));Product p=store.parse(q.get("snapshot_json"),Product.class);String status=InsuranceStore.s(app,"status");
  Instant starts=app.get("coverage_starts_at")==null?null:InsuranceStore.instant(app.get("coverage_starts_at")),ends=app.get("coverage_ends_at")==null?null:InsuranceStore.instant(app.get("coverage_ends_at"));
  boolean active="active".equals(status)&&starts!=null&&!starts.isAfter(Instant.now())&&ends!=null&&ends.isAfter(Instant.now())&&app.get("last_error_code")==null&&app.get("last_checked_at")!=null&&InsuranceStore.instant(app.get("last_checked_at")).isAfter(Instant.now().minusSeconds(300));
  return new Application(InsuranceStore.s(app,"id"),InsuranceStore.s(app,"order_id"),status,p.insurerName(),p.name(),amount(q.get("premium").toString(),q.get("currency").toString()),q.get("currency").toString(),InsuranceStore.s(app,"checkout_url"),InsuranceStore.s(app,"policy_number"),InsuranceStore.s(app,"certificate_url"),p.claimsUrl(),InsuranceStore.s(app,"coverage_ref"),InsuranceStore.s(q,"terms_version"),starts,ends,InsuranceStore.s(q,"platform"),"insurer_direct",active?"provider_confirmed":"not_confirmed");
 }
}
