package com.kuaixietech.service.order_fulfillment.impl;
import com.kuaixietech.mapper.order_fulfillment.ShippingNegotiationMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import java.math.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Final RPC source 20261021_140355: propose_shipping_fee / respond_shipping_fee. */
@Service public class ShippingNegotiationService {
 private final ShippingNegotiationMapper m;
 public ShippingNegotiationService(ShippingNegotiationMapper m){this.m=m;}
 private static String id(){return UUID.randomUUID().toString();}
 private static BigDecimal money(BigDecimal n){return n.setScale(2,RoundingMode.HALF_UP);}
 @Transactional public String proposeShippingFee(String orderId,BigDecimal fee,String note){
  String uid=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);
  if(o==null)return "ERR:NO_ORDER";
  if(!uid.equals(o.get("vendor_id")))return "ERR:NOT_VENDOR";
  if(!Set.of("purchased","shipped").contains(String.valueOf(o.get("status"))))return "ERR:BAD_STATUS";
  if("agreed".equals(o.get("shipping_fee_status")))return "ERR:ALREADY_AGREED";
  if(fee==null||fee.signum()<0||fee.compareTo(new BigDecimal("2000"))>0)return "ERR:BAD_FEE";
  BigDecimal n=money(fee);
  m.proposeFee(orderId,n,note==null||note.trim().isEmpty()?null:note.trim());
  m.message(id(),orderId,uid,"带单人提出邮费 ¥"+n+(note==null?"":"（"+note+"）")+"，24 小时内确认，超时视为同意");
  m.notice(id(),String.valueOf(o.get("buyer_id")),orderId,"带单人提出邮费 ¥"+n+"，请在 24 小时内确认或反提","待确认邮费");
  return "OK";
 }
 @Transactional public String respondShippingFee(String orderId,boolean accept,BigDecimal counter){
  String uid=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);
  if(o==null)return "ERR:NO_ORDER";
  if(!uid.equals(o.get("buyer_id")))return "ERR:NOT_BUYER";
  if(!Set.of("proposed","countered").contains(String.valueOf(o.get("shipping_fee_status"))))return "ERR:BAD_STATUS";
  if(accept){m.acceptFee(orderId);m.message(id(),orderId,uid,"买家已同意邮费 ¥"+o.get("shipping_fee"));return "OK";}
  if(counter==null||counter.signum()<0||counter.compareTo(new BigDecimal("2000"))>0)return "ERR:BAD_FEE";
  BigDecimal n=money(counter);m.counterFee(orderId,n);
  m.notice(id(),String.valueOf(o.get("vendor_id")),orderId,"买家反提邮费 ¥"+n+"，24 小时内不处理视为接受","买家反提邮费");
  return "OK";
 }
}
