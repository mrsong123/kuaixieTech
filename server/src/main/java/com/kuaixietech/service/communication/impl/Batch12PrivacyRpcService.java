package com.kuaixietech.service.communication.impl;
import com.kuaixietech.mapper.communication.Batch12PrivacyMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
/** Local-pool binding port. Only real non-demo provider-provisioned numbers are eligible. */
@Service public class Batch12PrivacyRpcService {
 private final Batch12PrivacyMapper m;public Batch12PrivacyRpcService(Batch12PrivacyMapper m){this.m=m;}
 private static String s(Object o){return o==null?null:o.toString();}
 private static String masked(String s){if(s==null||s.length()<7)return "****";return s.substring(0,3)+"****"+s.substring(s.length()-4);}
 private static Map<String,Object> err(String code){return Map.of("err",code);}
 private static Map<String,Object> binding(String id,String number,Object expiry,boolean reused){Map<String,Object> r=new LinkedHashMap<>();r.put("binding_id",id);r.put("virtual_number",number);r.put("expire_at",expiry);r.put("reused",reused);r.put("mode","local_pool");return r;}
 @Transactional public Map<String,Object> getOrBindPrivacyNumber(String order){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(order);if(o==null)return err("NO_ORDER");
  String buyer=s(o.get("buyer_id")),vendor=s(o.get("vendor_id"));if(!actor.equals(buyer)&&!actor.equals(vendor))return err("NOT_PARTY");
  String peer=actor.equals(buyer)?vendor:buyer;if(peer==null)return err("NO_PEER");
  if(!Set.of("locked","purchased","shipped").contains(s(o.get("status"))))return err("BAD_STATUS");
  String my=m.phone(actor),theirs=m.phone(peer);if(my==null||my.isBlank())return err("NO_MY_PHONE");if(theirs==null||theirs.isBlank())return err("NO_PEER_PHONE");
  m.releaseExpiredNumbers(order);m.releaseExpiredBindings(order);
  Map<String,Object> reused=m.activeBinding(order);if(reused!=null)return binding(s(reused.get("id")),s(reused.get("virtual_number")),reused.get("expire_at"),true);
  String number=m.availableNumber();if(number==null)return err("NO_NUMBER");
  if(m.occupyNumber(number)!=1)throw new IllegalStateException("privacy number raced");
  String id=UUID.randomUUID().toString();
  if(m.createBinding(id,order,number,masked(m.phone(buyer)),masked(m.phone(vendor)))!=1)throw new IllegalStateException("binding create failed");
  m.notifyUser(UUID.randomUUID().toString(),peer,order);
  // Telephony provider must be configured and number active, otherwise cannot assume calls will be routed.
  Map<String,Object> saved=m.activeBinding(order);if(saved==null)throw new IllegalStateException("new privacy binding not readable");return binding(id,number,saved.get("expire_at"),false);
 }
}
