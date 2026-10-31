package com.kuaixietech.service.assist.impl;

import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.mapper.assist.Batch12AssistMapper;
import com.kuaixietech.security.TrustedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Converts final 20261024 assist RPCs, always using the verified actor. */
@Service public class Batch12AssistRpcService {
 private final Batch12AssistMapper m; private final AtomicMoneyService money;
 private final ObjectMapper json;
 public Batch12AssistRpcService(Batch12AssistMapper m,AtomicMoneyService money,ObjectMapper json){this.m=m;this.money=money;this.json=json;}
 private static String s(Object o){return o==null?null:o.toString();}
 private static BigDecimal money(Object o){return o==null?BigDecimal.ZERO:new BigDecimal(o.toString());}
 private boolean hasPhoto(String urls){try{return urls!=null&&json.readTree(urls).isArray()&&json.readTree(urls).size()>0;}catch(Exception e){return false;}}
 private void notify(String user,String title,String body){if(user!=null&&m.notifyUser(UUID.randomUUID().toString(),user,title,body)!=1)throw new IllegalStateException("notification failed");}
 @Transactional public String createAssistRequest(String order,BigDecimal rate,String note){
  String actor=TrustedActor.currentUserId();if(rate==null||rate.compareTo(new BigDecimal("20"))<0||rate.compareTo(new BigDecimal("60"))>0)return "ERR:BAD_RATE";
  Map<String,Object> o=m.lockOrder(order);if(o==null)return "ERR:NO_ORDER";
  if(!actor.equals(s(o.get("vendor_id"))))return "ERR:NOT_VENDOR";
  if(!Set.of("locked","purchased").contains(s(o.get("status"))))return "ERR:BAD_STATUS";
  if(m.activeCount(order)>0)return "ERR:EXISTS";
  if(m.create(UUID.randomUUID().toString(),order,actor,rate,note)!=1)throw new IllegalStateException("assist create failed");
  m.notifyCarriers(actor,rate.toPlainString());return "OK";
 }
 @Transactional public String acceptAssist(String id){
  String actor=TrustedActor.currentUserId();if(m.verifiedCarrier(actor)==0)return "ERR:NOT_CARRIER";
  Map<String,Object> a=m.lockAssist(id);if(a==null)return "ERR:NO_ROW";
  if(!"pending".equals(s(a.get("status"))))return "ERR:TAKEN";
  if(actor.equals(s(a.get("requester_id"))))return "ERR:SELF";
  if(m.accept(id,actor)!=1)return "ERR:TAKEN";
  notify(s(a.get("requester_id")),"协带已有人承接","对方将与你联系交接事宜。");return "OK";
 }
 @Transactional public String cancelAssist(String id){
  String actor=TrustedActor.currentUserId();Map<String,Object> a=m.lockAssist(id);if(a==null)return "ERR:NO_ROW";
  if(!actor.equals(s(a.get("requester_id"))))return "ERR:NOT_OWNER";
  if(!Set.of("pending","accepted").contains(s(a.get("status"))))return "ERR:BAD_STATUS";
  if(m.cancel(id)!=1)return "ERR:BAD_STATUS";
  if(a.get("assistant_id")!=null)notify(s(a.get("assistant_id")),"协带单已取消","发起人取消了该协带。");return "OK";
 }
 @Transactional public String submitAssistHandoverA(String id,String urls,String city){
  String actor=TrustedActor.currentUserId();Map<String,Object> a=m.lockAssist(id);if(a==null)return "ERR:NO_ROW";
  if(!actor.equals(s(a.get("requester_id"))))return "ERR:NOT_OWNER";
  if(!"accepted".equals(s(a.get("status"))))return "ERR:BAD_STATUS";
  if(!hasPhoto(urls))return "ERR:NO_PHOTO";
  if(m.handoverA(id,urls,city)!=1)return "ERR:BAD_STATUS";
  notify(s(a.get("assistant_id")),"已收到交接商品","请携带商品完成配送并在交付买家时上传交接凭证。");return "OK";
 }
 @Transactional public String finishAssist(String id,String urls,String city){
  String actor=TrustedActor.currentUserId();Map<String,Object> a=m.lockAssist(id);if(a==null)return "ERR:NO_ROW";
  if(!actor.equals(s(a.get("assistant_id"))))return "ERR:NOT_ASSISTANT";
  if(!"carrying".equals(s(a.get("status"))))return "ERR:BAD_STATUS";
  if(!hasPhoto(urls))return "ERR:NO_PHOTO";
  Map<String,Object> o=m.lockOrder(s(a.get("order_id")));if(o==null)throw new IllegalStateException("assist order missing");
  BigDecimal share=money(o.get("goods_amount")).add(money(o.get("shipping_fee"))).multiply(money(a.get("share_rate"))).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
  // Financial-safety deviation: reject insufficient payer balance rather than create a negative balance.
  BigDecimal payerBalance=money.lockBalance(s(a.get("requester_id")));
  if(payerBalance==null||payerBalance.compareTo(share)<0)return "ERR:INSUFFICIENT_BALANCE";
  money.addBalance(s(a.get("requester_id")),share.negate(),"assist_share_out",s(a.get("order_id")),"协带分润支出");
  money.addBalance(actor,share,"assist_share",s(a.get("order_id")),"协带分润");
  if(m.finish(id,urls,city,share)!=1)throw new IllegalStateException("assist state transition rejected");
  notify(s(a.get("requester_id")),"协带已完成","商品已交付买家，分润 ¥"+share+" 已从余额划转给协带人。");
  return "OK:"+share.toPlainString();
 }
}
