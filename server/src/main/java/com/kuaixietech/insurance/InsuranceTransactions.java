package com.kuaixietech.insurance;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.kuaixietech.insurance.InsuranceModels.*;
@Service
public class InsuranceTransactions {
 private final InsuranceStore store;private final InsuranceCatalog catalog;private final ObjectMapper json;
 public InsuranceTransactions(InsuranceStore store,InsuranceCatalog catalog,ObjectMapper json){this.store=store;this.catalog=catalog;this.json=json;}
 @Transactional public Map<String,Object> reserve(String user,ApplyInput input){
  if(!input.termsAccepted()||!input.disclosuresAccepted())throw new IllegalArgumentException("INSURANCE_CONSENT_REQUIRED");
  String request=UUID.fromString(input.requestId()).toString();
  var order=store.row("SELECT id,buyer_id,status,goods_amount,item_name,agreed_json FROM t_orders WHERE id=? FOR UPDATE",input.orderId());
  if(order==null||!user.equals(order.get("buyer_id")))throw new IllegalArgumentException("INSURANCE_ORDER_UNAVAILABLE");
  var prior=store.row("SELECT * FROM t_goods_insurance_applications WHERE user_id=? AND request_id=? FOR UPDATE",user,request);
  if(prior!=null){if(!input.orderId().equals(prior.get("order_id"))||!input.quoteId().equals(prior.get("quote_id")))throw new IllegalArgumentException("INSURANCE_REQUEST_REUSED");return prior;}
  prior=store.row("SELECT * FROM t_goods_insurance_applications WHERE order_id=? FOR UPDATE",input.orderId());
  if(prior!=null){if(!input.quoteId().equals(prior.get("quote_id")))throw new IllegalArgumentException("INSURANCE_ORDER_ALREADY_SELECTED");return prior;}
  if(!Set.of("requesting","negotiating","vendor_deposit_pending","locked").contains(order.get("status")))throw new IllegalArgumentException("INSURANCE_PURCHASE_TOO_LATE");
  var q=store.row("SELECT * FROM t_goods_insurance_quotes WHERE id=? AND user_id=? FOR UPDATE",input.quoteId(),user);
  if(q==null||!InsuranceStore.instant(q.get("expires_at")).isAfter(Instant.now()))throw new IllegalArgumentException("INSURANCE_QUOTE_EXPIRED");
  try{var agreed=json.readTree(String.valueOf(order.get("agreed_json")));boolean forOrder="order".equals(q.get("subject_type"))&&input.orderId().equals(q.get("subject_id"))&&"yes".equals(agreed.path("insurance_choice").asText());if(!forOrder&&!input.quoteId().equals(agreed.path("insurance_quote_id").asText()))throw new IllegalArgumentException("INSURANCE_ORDER_QUOTE_MISMATCH");}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("INSURANCE_INVALID_ORDER_SNAPSHOT");}
  if(new BigDecimal(q.get("declared_value").toString()).compareTo(new BigDecimal(order.get("goods_amount").toString()))!=0||!q.get("item_name").equals(order.get("item_name")))throw new IllegalArgumentException("INSURANCE_ORDER_QUOTE_MISMATCH");
  Product product=catalog.available(InsuranceStore.s(q,"product_id"));if(product.revision()!=((Number)q.get("product_revision")).longValue())throw new IllegalArgumentException("INSURANCE_PRODUCT_CHANGED_REQUOTE");
  if("order".equals(q.get("subject_type")))store.db.update("UPDATE t_orders SET agreed_json=JSON_SET(agreed_json,'$.insurance_quote_id',?) WHERE id=? AND buyer_id=?",input.quoteId(),input.orderId(),user);
  String id=UUID.nameUUIDFromBytes(("insurance:"+user+":"+request).getBytes(StandardCharsets.UTF_8)).toString();
  store.db.update("INSERT INTO t_goods_insurance_applications(id,user_id,order_id,quote_id,insurer_code,request_id,terms_accepted_at,disclosures_accepted_at) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP(3),CURRENT_TIMESTAMP(3))",id,user,input.orderId(),input.quoteId(),product.insurerCode(),request);
  Product snapshot=store.parse(q.get("snapshot_json"),Product.class);
  for(String kind:List.of("insurance_terms","insurance_data_sharing"))store.db.update("INSERT INTO t_consent_records(id,user_id,consent_type,notice_version,recipient_name,purpose,information_types,rights_method,accepted,accepted_at,source) VALUES(?,?,?,?,?,?,?,?,1,CURRENT_TIMESTAMP(3),'goods_insurance')",UUID.randomUUID().toString(),user,kind,kind.equals("insurance_terms")?q.get("terms_version"):"goods_insurance_direct_v1",snapshot.insurerName(),"投保申请 "+id+"；订单 "+input.orderId()+"；保险机构直接核保与收款","商品、数量、金额、联系人、联系电话、收货地址及行程标识",snapshot.claimsUrl());
  return store.application(id);
 }
 @Transactional public void receipt(String id,PolicyReceipt receipt){
  // Match the order lock order used by underwriting guards before locking the application.
  var initial=store.application(id);if(initial==null)throw new IllegalArgumentException("INSURANCE_APPLICATION_UNAVAILABLE");
  var order=store.row("SELECT id,status,agreed_json FROM t_orders WHERE id=? FOR UPDATE",initial.get("order_id"));
  var app=store.row("SELECT * FROM t_goods_insurance_applications WHERE id=? FOR UPDATE",id);
  var quote=store.quote(InsuranceStore.s(app,"quote_id"));Product product=store.parse(quote.get("snapshot_json"),Product.class);
  validateReceipt(app,quote,product,receipt);
  String state=receipt.state();
  if(Set.of("rejected","cancelled","expired").contains(app.get("status"))&&!app.get("status").equals(state))throw new IllegalArgumentException("INSURANCE_TERMINAL_POLICY_CHANGED");
  if("active".equals(state)){
   if(receipt.policyNumber()==null||receipt.policyNumber().isBlank()||receipt.policyNumber().length()>160||receipt.coverageRef()==null||receipt.coverageRef().isBlank()||receipt.coverageRef().length()>160||receipt.startsAt()==null||receipt.endsAt()==null||!receipt.endsAt().isAfter(receipt.startsAt()))throw new IllegalArgumentException("INSURANCE_POLICY_INCOMPLETE");
   InsuranceUrlPolicy.require(receipt.certificateUrl(),product.serviceHosts());
   if(!receipt.endsAt().isAfter(Instant.now()))state="expired";
   else if(receipt.startsAt().isAfter(Instant.now()))state="scheduled";
  }
  if(app.get("policy_number")!=null&&receipt.policyNumber()!=null&&!app.get("policy_number").equals(receipt.policyNumber()))throw new IllegalArgumentException("INSURANCE_POLICY_CHANGED");
  // Ignore a late pending response after a previously confirmed issue; preserve the audit trail.
  if(Set.of("active","scheduled","cancelled","expired").contains(app.get("status"))&&"awaiting_provider".equals(state))return;
  if(store.db.update("UPDATE t_goods_insurance_applications SET status=?,provider_application_ref=COALESCE(provider_application_ref,?),policy_number=COALESCE(?,policy_number),coverage_ref=COALESCE(?,coverage_ref),certificate_url=COALESCE(?,certificate_url),coverage_starts_at=COALESCE(?,coverage_starts_at),coverage_ends_at=COALESCE(?,coverage_ends_at),last_checked_at=CURRENT_TIMESTAMP(3),last_error_code=NULL WHERE id=?",state,receipt.applicationRef(),receipt.policyNumber(),receipt.coverageRef(),receipt.certificateUrl(),receipt.startsAt()==null?null:Timestamp.from(receipt.startsAt()),receipt.endsAt()==null?null:Timestamp.from(receipt.endsAt()),id)!=1)throw new IllegalStateException("INSURANCE_RECEIPT_NOT_PERSISTED");
  if(order==null)throw new IllegalStateException("INSURANCE_ORDER_UNAVAILABLE");
  // The legacy yes/waived enum cannot store pending; full provider state lives in this application.
  store.db.update("UPDATE t_orders SET insured=?,insurance_premium=0 WHERE id=?","active".equals(state)?"yes":"unselected",app.get("order_id"));
 }
 static void validateReceipt(Map<String,Object> app,Map<String,Object> quote,Product product,PolicyReceipt r){
  if(!Objects.equals(app.get("id"),r.applicationId())||r.applicationRef()==null||r.applicationRef().isBlank()||r.applicationRef().length()>160||app.get("provider_application_ref")!=null&&!Objects.equals(app.get("provider_application_ref"),r.applicationRef())||!Objects.equals(app.get("order_id"),r.orderId())||!product.insurerCode().equals(r.insurerCode())||!product.providerProductCode().equals(r.providerProductCode())||!quote.get("currency").equals(r.currency())||!quote.get("terms_version").equals(r.termsVersion())||r.declaredValue()==null||new BigDecimal(quote.get("declared_value").toString()).compareTo(r.declaredValue())!=0||r.premium()==null||new BigDecimal(quote.get("premium").toString()).compareTo(r.premium())!=0)throw new IllegalArgumentException("INSURANCE_RECEIPT_MISMATCH");
  if(!Set.of("active","awaiting_provider","rejected","cancelled","expired").contains(r.state()))throw new IllegalArgumentException("INSURANCE_PROVIDER_STATE_UNKNOWN");
  if("active".equals(r.state())&&!r.premiumPaidDirectly())throw new IllegalArgumentException("INSURANCE_DIRECT_PAYMENT_UNVERIFIED");
 }
}
