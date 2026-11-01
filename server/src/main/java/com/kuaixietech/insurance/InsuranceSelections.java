package com.kuaixietech.insurance;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import static com.kuaixietech.insurance.InsuranceModels.*;
@Service
public class InsuranceSelections {
 private final InsuranceStore store;private final InsuranceCatalog catalog;
 public InsuranceSelections(InsuranceStore store,InsuranceCatalog catalog){this.store=store;this.catalog=catalog;}
 public Map<String,Object> validate(String buyer,String id,String type,String subject,BigDecimal goods,String item){
  if(id==null||id.isBlank())throw new IllegalArgumentException("INSURANCE_SELECTION_REQUIRED");
  Map<String,Object> q=store.quote(id);
  if(q==null||!buyer.equals(q.get("user_id"))||!type.equals(q.get("subject_type"))||!subject.equals(q.get("subject_id"))||new BigDecimal(q.get("declared_value").toString()).compareTo(goods)!=0||!item.trim().equals(q.get("item_name")))throw new IllegalArgumentException("INSURANCE_QUOTE_SUBJECT_MISMATCH");
  if(!InsuranceStore.instant(q.get("expires_at")).isAfter(Instant.now()))throw new IllegalArgumentException("INSURANCE_QUOTE_EXPIRED");
  Product p=catalog.available(InsuranceStore.s(q,"product_id"));if(p.revision()!=((Number)q.get("product_revision")).longValue())throw new IllegalArgumentException("INSURANCE_PRODUCT_CHANGED_REQUOTE");
  return q;
 }
}
