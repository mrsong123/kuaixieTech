package com.kuaixietech.service.order_fulfillment.impl;
import com.kuaixietech.mapper.order_fulfillment.PeerRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Source open_peer_thread and get_peer_profile; only explicitly projected profile fields leave the server. */
@Service public class PeerRpcService {
 private final PeerRpcMapper m;public PeerRpcService(PeerRpcMapper m){this.m=m;}
 private static String s(Object x){return x==null?null:String.valueOf(x);}
 @Transactional public Map<String,Object> openPeerThread(String orderId){
  String actor=TrustedActor.currentUserId();Map<String,Object> order=m.lockOrder(orderId);if(order==null)return Map.of("err","NO_ORDER");
  String buyer=s(order.get("buyer_id")),vendor=s(order.get("vendor_id"));
  String peer;if(actor.equals(buyer))peer=vendor;else if(actor.equals(vendor))peer=buyer;else return Map.of("err","NOT_PARTY");
  if(peer==null)return Map.of("err","NO_PEER");Map<String,Object> profile=m.peer(peer);
  String name=profile==null||s(profile.get("nickname"))==null?"对方":s(profile.get("nickname"));
  Map<String,Object> found=m.existingThread(orderId,actor,peer);boolean reused=found!=null;
  String id;if(reused){id=s(found.get("id"));if("closed".equals(s(found.get("status"))))m.reopenThread(id);}
  else{id=UUID.randomUUID().toString();if(m.createThread(id,orderId,actor,peer)!=1)throw new IllegalStateException("Cannot create peer thread");}
  return Map.of("thread_id",id,"peer_id",peer,"peer_nickname",name,"reused",reused);
 }
 @Transactional(readOnly=true) public Map<String,Object> getPeerProfile(String target){
  String actor=TrustedActor.currentUserId();
  if(target==null)return Map.of("err","NOT_FOUND");
  if(!actor.equals(target)&&m.related(actor,target)==0)return Map.of("err","NOT_RELATED");
  Map<String,Object> p=m.peer(target);if(p==null)return Map.of("err","NOT_FOUND");
  Map<String,Object> out=new LinkedHashMap<>();
  for(String name:List.of("id","nickname","avatar_url","city","performance_score","star_avg","star_count","deal_count","carrier_verify_status","buyer_verify_status","carrier_tier","invite_tier","user_role"))out.put(name,p.get(name));
  String created=s(p.get("created_at"));out.put("created_at",created==null?null:created.substring(0,Math.min(10,created.length())));
  String phone=s(p.get("phone"));out.put("phone_masked",phone==null||phone.length()<7?null:actor.equals(target)?phone:phone.substring(0,3)+"****"+phone.substring(phone.length()-4));
  return out;
 }
}
