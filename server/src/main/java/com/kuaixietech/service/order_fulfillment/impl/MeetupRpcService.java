package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.service.finance.impl.RewardSettlementService;
import com.kuaixietech.mapper.order_fulfillment.MeetupRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import java.math.*;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Issue/confirm in-person delivery. Source confirm_meetup previously marked order completed
 * before invoking settlement, making settlement's idempotence check skip all money hooks.
 * The Java port deliberately invokes settlement while status='purchased' and then records proof. */
@Service public class MeetupRpcService {
 private final MeetupRpcMapper m;private final RewardSettlementService settlement;private final SecureRandom random=new SecureRandom();
 public MeetupRpcService(MeetupRpcMapper m,RewardSettlementService settlement){this.m=m;this.settlement=settlement;}
 private static String id(){return UUID.randomUUID().toString();}
 @Transactional public String issueMeetupCode(String orderId){
  String uid=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);
  if(o==null)return "ERR:NO_ORDER";
  if(!uid.equals(o.get("vendor_id")))return "ERR:NOT_VENDOR";
  if(!"meetup".equals(o.get("delivery_mode")))return "ERR:NOT_MEETUP";
  if(!"purchased".equals(o.get("status")))return "ERR:BAD_STATUS";
  if(o.get("meetup_code")!=null)return "OK:"+o.get("meetup_code");
  String code=String.format(Locale.ROOT,"%04d",random.nextInt(10000));
  if(m.issueCode(orderId,code)!=1)throw new IllegalStateException("Meetup code race");
  m.message(id(),orderId,uid,"带单人发起到城面交，请把 4 位收货码告知对方当面交付时核验");
  m.notice(id(),String.valueOf(o.get("buyer_id")),orderId,"meetup","当面交付待确认","带单人已在订单内下发收货码，当面交货时核对后在订单页确认收货");
  return "OK:"+code;
 }
 @Transactional public String confirmMeetup(String orderId,String code,BigDecimal lat,BigDecimal lng,String city){
  String uid=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);
  if(o==null)return "ERR:NO_ORDER";
  if(!uid.equals(o.get("buyer_id")))return "ERR:NOT_BUYER";
  if(!"meetup".equals(o.get("delivery_mode")))return "ERR:NOT_MEETUP";
  if(!"purchased".equals(o.get("status")))return "ERR:BAD_STATUS";
  if(o.get("meetup_code")==null)return "ERR:NO_CODE_ISSUED";
  if(code==null||!code.equals(o.get("meetup_code")))return "ERR:CODE_MISMATCH";
  String procurementCity=o.get("procurement_city")==null?null:String.valueOf(o.get("procurement_city"));
  boolean gap=city!=null&&procurementCity!=null&&!city.split("市",2)[0].equals(procurementCity.split("市",2)[0]);
  // Proof is saved before the complete-settlement state machine transition, under the same transaction.
  if(m.confirm(orderId,uid,lat,lng,city,gap)!=1)throw new IllegalStateException("Meetup confirmation update failed");
  if(!settlement.settleOrderCompletion(orderId))throw new IllegalStateException("Meetup settlement not allowed");
  m.message(id(),orderId,uid,"买家已核对收货码并确认当面收货，订单完成结算");
  m.notice(id(),String.valueOf(o.get("vendor_id")),orderId,"completed","面交订单已完成","买家已确认当面收货，货款将按规则结算到你的余额");
  return "OK";
 }
}
