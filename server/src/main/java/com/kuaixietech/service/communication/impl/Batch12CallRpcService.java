package com.kuaixietech.service.communication.impl;
import com.kuaixietech.mapper.communication.Batch12CallMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;import java.time.*;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
/** Order RTC signaling RPC. Media/recording provider remains a separate configured integration. */
@Service public class Batch12CallRpcService {
 private final Batch12CallMapper m;public Batch12CallRpcService(Batch12CallMapper m){this.m=m;}
 private static String s(Object o){return o==null?null:String.valueOf(o);}
 private static Map<String,Object> err(String e){return Map.of("err",e);}
 private void notifyUser(String user,String order,String kind,String title,String content){if(m.notifyPeer(UUID.randomUUID().toString(),user,order,kind,title,content)!=1)throw new IllegalStateException("notification failed");}
 @Transactional public Map<String,Object> startOrderCall(String order){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(order);if(o==null)return err("NO_ORDER");
  String peer=actor.equals(s(o.get("buyer_id")))?s(o.get("vendor_id")):actor.equals(s(o.get("vendor_id")))?s(o.get("buyer_id")):null;
  if(peer==null)return actor.equals(s(o.get("buyer_id")))||actor.equals(s(o.get("vendor_id")))?err("NO_PEER"):err("NOT_PARTY");
  if(!Set.of("locked","purchased","shipped").contains(s(o.get("status"))))return err("BAD_STATUS");
  m.expireRinging(order);if(m.activeRinging(order)>0)return err("BUSY");
  String id=UUID.randomUUID().toString(),room="call-"+UUID.randomUUID().toString().replace("-","");
  if(m.createCall(id,order,actor,peer,room)!=1)throw new IllegalStateException("call creation failed");
  notifyUser(peer,order,"incoming_call","收到网络通话邀请","对方请求与你在平台内语音沟通，录音必须由供应商确认并取得知情同意。");
  return Map.of("call_id",id,"room_id",room,"peer",peer);
 }
 @Transactional public Map<String,Object> acceptOrderCall(String id){
  String actor=TrustedActor.currentUserId();Map<String,Object> c=m.lockCall(id);if(c==null)return err("NOT_FOUND");
  if(!actor.equals(s(c.get("callee_id"))))return err("NOT_CALLEE");if(!"ringing".equals(s(c.get("status"))))return err("STALE");
  if(m.accept(id)!=1)return err("STALE");notifyUser(s(c.get("caller_id")),s(c.get("order_id")),"call","对方已接听","网络通话已接通。");
  return Map.of("ok",true,"room_id",s(c.get("room_id")));
 }
 @Transactional public String rejectOrderCall(String id,boolean callee){
  String actor=TrustedActor.currentUserId();Map<String,Object> c=m.lockCall(id);if(c==null)return "ERR:NOT_FOUND";
  if(!actor.equals(s(c.get(callee?"callee_id":"caller_id")))||!"ringing".equals(s(c.get("status"))))return "ERR:FORBIDDEN";
  if(m.reject(id,callee?"rejected":"cancelled")!=1)return "ERR:FORBIDDEN";
  if(callee)notifyUser(s(c.get("caller_id")),s(c.get("order_id")),"call","通话未接通","对方拒绝了你的通话邀请，可尝试文字沟通。");return "OK";
 }
 @Transactional public Map<String,Object> hangupOrderCall(String id){
  String actor=TrustedActor.currentUserId();Map<String,Object> c=m.lockCall(id);if(c==null)return err("NOT_FOUND");
  if(!actor.equals(s(c.get("caller_id")))&&!actor.equals(s(c.get("callee_id"))))return err("FORBIDDEN");
  if(!Set.of("ringing","accepted").contains(s(c.get("status"))))return Map.of("ok",true,"duration_sec",c.get("duration_sec")==null?0:c.get("duration_sec"));
  long secs=0;Object start=c.get("started_at");if(start instanceof LocalDateTime t)secs=Math.max(0,Duration.between(t,LocalDateTime.now()).getSeconds());
  if(start instanceof java.sql.Timestamp t)secs=Math.max(0,Duration.between(t.toLocalDateTime(),LocalDateTime.now()).getSeconds());
  if(m.hangup(id,secs)!=1)throw new IllegalStateException("hangup failed");
  m.systemMessage(UUID.randomUUID().toString(),s(c.get("order_id")),actor,secs);
  // IMPORTANT recording_url must only come from verified RTC provider callback, never an untrusted client parameter.
  return Map.of("ok",true,"duration_sec",secs);
 }
}
