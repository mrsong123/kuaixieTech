package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import com.kuaixietech.mapper.order_fulfillment.EvidencePrivacyRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Ports get_order_evidence (admin only), log_privacy_call (current order participant). */
@Service public class EvidencePrivacyRpcService {
 private final EvidencePrivacyRpcMapper m;private final LegacyAdminRpcService admin;
 public EvidencePrivacyRpcService(EvidencePrivacyRpcMapper m,LegacyAdminRpcService admin){this.m=m;this.admin=admin;}
 @Transactional public Map<String,Object> getOrderEvidence(String orderId){
  String actor=admin.adminGuard("evidence_read","order",orderId,"运营查看完整防伪证据链");
  Map<String,Object> order=m.findOrderEvidence(orderId);
  if(order==null)return Map.of("err","NO_ORDER");
  String tripId=order.get("trip_id")==null?null:String.valueOf(order.remove("trip_id"));
  Map<String,Object> result=new LinkedHashMap<>();result.put("order",order);
  result.put("trip",tripId==null?null:m.findTripEvidence(tripId));
  result.put("faces",m.findFaces(orderId));
  return result;
 }
 @Transactional public Map<String,Object> logPrivacyCall(String orderId,String result){
  String uid=TrustedActor.currentUserId();
  if(result==null||!Set.of("dialed","connected","unreachable").contains(result))return Map.of("err","BAD_RESULT");
  Map<String,Object> binding=m.activeBinding(orderId);
  if(binding==null)return Map.of("err","NO_BINDING");
  Map<String,Object> party=m.orderParties(orderId);
  if(party==null)return Map.of("err","NOT_PARTY");
  String role,peer;
  if(uid.equals(party.get("buyer_id"))){role="buyer";peer="vendor";}
  else if(uid.equals(party.get("vendor_id"))){role="vendor";peer="buyer";}
  else return Map.of("err","NOT_PARTY");
  String lid=UUID.randomUUID().toString();
  m.callLog(lid,orderId,String.valueOf(binding.get("id")),uid,role,peer,String.valueOf(binding.get("virtual_number")),result);
  return Map.of("ok",true,"log_id",lid);
 }
}
