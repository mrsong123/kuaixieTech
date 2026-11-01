package com.kuaixietech.service.order_fulfillment.impl;
import com.kuaixietech.mapper.order_fulfillment.ShippingNegotiationMapper;
import com.kuaixietech.security.TrustedActor;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import java.util.*;
import java.math.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Final RPC source 20261021_140355: propose_shipping_fee / respond_shipping_fee. */
@Service public class ShippingNegotiationService {
 private final ShippingNegotiationMapper m;
 private final AtomicMoneyService accounting;
 public ShippingNegotiationService(ShippingNegotiationMapper m,AtomicMoneyService accounting){this.m=m;this.accounting=accounting;}
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
  m.message(id(),orderId,uid,"带单人提出邮费 ¥"+n+(note==null?"":"（"+note+"）")+(com.kuaixietech.service.trading.support.IntentPricing.isEscrowVersion(o.get("pricing_version"))?"，需买家确认并托管后才可支付":"，24 小时内确认，超时视为同意"));
  m.notice(id(),String.valueOf(o.get("buyer_id")),orderId,"带单人提出邮费 ¥"+n+"，请在 24 小时内确认或反提","待确认邮费");
  return "OK";
 }
 @Transactional public String respondShippingFee(String orderId,boolean accept,BigDecimal counter,BigDecimal expectedFee){
  String uid=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);
  if(o==null)return "ERR:NO_ORDER";
  if(!uid.equals(o.get("buyer_id")))return "ERR:NOT_BUYER";
  if(accept&&"agreed".equals(o.get("shipping_fee_status")))return "OK";
  if(!Set.of("purchased","shipped").contains(String.valueOf(o.get("status"))))return "ERR:BAD_STATUS";
  if(!Set.of("proposed","countered").contains(String.valueOf(o.get("shipping_fee_status"))))return "ERR:BAD_STATUS";
  if(accept){
   if(com.kuaixietech.service.trading.support.IntentPricing.isEscrowVersion(o.get("pricing_version"))){
    if(!"frozen".equals(o.get("buyer_deposit_status")))return "ERR:ESCROW_NOT_FROZEN";
    BigDecimal fee=money(new BigDecimal(String.valueOf(o.get("shipping_fee"))));
    if(expectedFee==null||expectedFee.compareTo(fee)!=0)return "ERR:SHIPPING_FEE_CHANGED_RECONFIRM";
    if(fee.signum()<0||fee.compareTo(new BigDecimal("2000"))>0)return "ERR:BAD_FEE";
    BigDecimal balance=accounting.lockBalance(uid);
    if(balance==null||balance.compareTo(fee)<0)return "ERR:INSUFFICIENT_BALANCE";
    if(fee.signum()>0){
     if(accounting.addBalance(uid,fee.negate(),"shipping_freeze",orderId,"买家确认邮费并托管")==null)throw new IllegalStateException("Missing buyer wallet");
     if(m.holdFee(orderId,fee)!=1||m.growBuyerDeposit(orderId,uid,fee)!=1)throw new IllegalStateException("Shipping escrow mismatch");
    }
   }
   if(m.acceptFee(orderId)!=1)throw new IllegalStateException("Shipping confirmation conflict");
   m.message(id(),orderId,uid,"买家已同意邮费 ¥"+o.get("shipping_fee"));return "OK";
  }
  if(counter==null||counter.signum()<0||counter.compareTo(new BigDecimal("2000"))>0)return "ERR:BAD_FEE";
  BigDecimal n=money(counter);m.counterFee(orderId,n);
  m.notice(id(),String.valueOf(o.get("vendor_id")),orderId,"买家反提邮费 ¥"+n+"，24 小时内不处理视为接受","买家反提邮费");
  return "OK";
 }
}
