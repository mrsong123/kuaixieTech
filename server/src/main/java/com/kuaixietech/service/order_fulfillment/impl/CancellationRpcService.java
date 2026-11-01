package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.finance.impl.RewardSettlementService;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import com.kuaixietech.mapper.order_fulfillment.CancellationRpcMapper;
import com.kuaixietech.security.TrustedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Final source request_cancel_v2 (20261021_180730) and admin_resolve_cancel (20261021_182909).
 * Source's force majeure direct closure is retained, NOT represented as an administrator-approved review.
 */
@Service public class CancellationRpcService {
 private com.kuaixietech.commerce.ProductInventory inventory;
 @org.springframework.beans.factory.annotation.Autowired public void productInventory(com.kuaixietech.commerce.ProductInventory inventory){this.inventory=inventory;}
 private final CancellationRpcMapper m;private final AtomicMoneyService money;private final CancellationTierMigrationService tiers;
 private final RewardSettlementService rewards;private final LegacyAdminRpcService admin;private final ObjectMapper json;
 public CancellationRpcService(CancellationRpcMapper m,AtomicMoneyService money,CancellationTierMigrationService tiers,RewardSettlementService rewards,LegacyAdminRpcService admin,ObjectMapper json){this.m=m;this.money=money;this.tiers=tiers;this.rewards=rewards;this.admin=admin;this.json=json;}
 private static String str(Object x){return x==null?null:String.valueOf(x);}
 private static BigDecimal amount(Object x){return x==null?BigDecimal.ZERO:new BigDecimal(str(x));}
 private static BigDecimal money(BigDecimal x){return x.setScale(2,RoundingMode.HALF_UP);}
 private static String status(Map<String,Object> o,String k){return str(o.get(k));}
 private static void changed(int n){if(n!=1)throw new IllegalStateException("Concurrent cancellation decision");}
 private void notice(String uid,String kind,String title,String body,String orderId){if(uid!=null)changed(m.addNotice(UUID.randomUUID().toString(),uid,kind,title,body,orderId));}
 private void lockParticipants(String buyer,String vendor){for(String uid:new TreeSet<>(Arrays.asList(buyer,vendor==null?buyer:vendor)))if(money.lockBalance(uid)==null)throw new IllegalStateException("Missing account");}
 private void refund(String uid,BigDecimal value,String orderId,String note){if(uid!=null&&value.signum()>0)money.addBalance(uid,value,"refund",orderId,note);}
 @Transactional public String requestCancelV2(String orderId,String reason,boolean force,String note,String proofJson){
  String actor=TrustedActor.currentUserId();Map<String,Object> o=m.lockOrder(orderId);if(o==null)return "ERR:NO_ORDER";
  String buyer=str(o.get("buyer_id")),vendor=str(o.get("vendor_id"));boolean isBuyer=actor.equals(buyer);
  if(!isBuyer&&!actor.equals(vendor))return "ERR:NOT_PARTY";
  if(!m.activeCases(orderId).isEmpty()||!m.activeAfterSales(orderId).isEmpty())return "ERR:DISPUTE_OPEN";
  if(!Set.of("requesting","negotiating","vendor_deposit_pending","locked","purchased").contains(status(o,"status")))return "ERR:BAD_STATUS";
  String tier=force?"free":tiers.compute(orderId,isBuyer);
  if(tier.startsWith("ERR:"))return tier;
  String proof=proofJson==null?"[]":proofJson;try{JsonNode p=json.readTree(proof);if(!p.isArray())return "ERR:BAD_PROOF";}catch(Exception e){return "ERR:BAD_PROOF";}
  BigDecimal bd=amount(o.get("buyer_deposit")),vd=amount(o.get("vendor_deposit"));boolean bf="frozen".equals(status(o,"buyer_deposit_status")),vf="frozen".equals(status(o,"vendor_deposit_status"));
  // Do not mint funds when source is called on an order whose deposit is already released.
  if(!bf&&isBuyer&&!Set.of("cooldown","free").contains(tier))return "ERR:BUYER_NOT_FROZEN";
  if(!vf&&!isBuyer&&"breach".equals(tier))return "ERR:VENDOR_NOT_FROZEN";
  lockParticipants(buyer,vendor);BigDecimal penalty=BigDecimal.ZERO;
  String buyerStatus=status(o,"buyer_deposit_status"),vendorStatus=status(o,"vendor_deposit_status");
  String finalStatus;
  if(Set.of("cooldown","free").contains(tier)){
   if(bf){refund(buyer,bd,orderId,"取消无责退还买家冻结金额");buyerStatus="refunded";}
   if(vf){refund(vendor,vd,orderId,"取消无责退还带单人保证金");vendorStatus="refunded";}
   finalStatus="cancelled_no_fault";
  }else if("hour_24".equals(tier)){
   penalty=money(bd.multiply(new BigDecimal("0.30")));
   if(bf){refund(buyer,bd.subtract(penalty),orderId,"24 小时档：退还 70% 冻结金额");buyerStatus="forfeited_partial";}
   refund(vendor,penalty,orderId,"买家 24 小时取消补偿");
   if(vf){refund(vendor,vd,orderId,"带单人保证金退回");vendorStatus="refunded";}
   changed(m.reduceScore(buyer,5,false));finalStatus="cancelled_breach";
  }else{
   finalStatus="cancelled_breach";
   if(isBuyer){penalty=money(bd.multiply(new BigDecimal("0.70")));
    refund(vendor,penalty,orderId,"买家违约赔付 70%，剩余 30% 为平台风险池（待独立台账核验）");
    buyerStatus="forfeited";if(vf){refund(vendor,vd,orderId,"带单人保证金退回");vendorStatus="refunded";}
    changed(m.reduceScore(buyer,15,true));
   }else{penalty=money(vd.multiply(new BigDecimal("0.70")));
    refund(buyer,penalty,orderId,"带单人违约赔付 70%，剩余 30% 为平台风险池（待独立台账核验）");
    vendorStatus="forfeited";if(bf){refund(buyer,bd,orderId,"买家保证金退回");buyerStatus="refunded";}
    changed(m.reduceScore(vendor,15,true));
   }
  }
  changed(m.markCancelled(orderId,finalStatus,tier,reason==null?"other":reason,force,proof,buyerStatus,vendorStatus,penalty,null));
  if(inventory!=null)inventory.transition(orderId,"released");
  // Source has a terminal reward recall hook; preserve it within the same transaction.
  rewards.revokeOrderRewards(orderId,"订单取消/违约结案，提成已按规则追回");
  notice(buyer,"order","订单取消处理完成","订单 "+o.get("order_no")+" 已按「"+tier+"」规则处理"+(note==null||note.isBlank()?"":"；说明："+note),orderId);
  notice(vendor,"order","订单取消处理完成","订单 "+o.get("order_no")+" 已按「"+tier+"」规则结算。",orderId);
  return "OK:"+tier;
 }
 @Transactional public String adminResolveCancel(String orderId,boolean approve,String reason){
  String why=reason==null?"":reason.trim();if(why.isEmpty())return "ERR:NO_NOTE";
  Map<String,Object> o=m.lockOrder(orderId);if(o==null)return "ERR:NO_ORDER";
  if(!m.activeCases(orderId).isEmpty()||!m.activeAfterSales(orderId).isEmpty())return "ERR:DISPUTE_OPEN";
  Map<String,Object> cr=m.latestPending(orderId);if(cr==null)return "ERR:NO_PENDING";
  String operator=admin.adminGuard(approve?"cancel_approved":"cancel_rejected","order",orderId,why);
  String tier="-";
  if(approve){
   String buyer=str(o.get("buyer_id")),vendor=str(o.get("vendor_id"));lockParticipants(buyer,vendor);
   if("frozen".equals(status(o,"buyer_deposit_status")))refund(buyer,amount(o.get("buyer_deposit")),orderId,"取消核准：保证金退回");
   if("frozen".equals(status(o,"vendor_deposit_status")))refund(vendor,amount(o.get("vendor_deposit")),orderId,"取消核准：保证金退回");
   changed(m.markCancelled(orderId,"cancelled_no_fault",null,str(cr.get("reason_type")),false,"[]","refunded","refunded",BigDecimal.ZERO,"运营核准取消："+why));
   if(inventory!=null)inventory.transition(orderId,"released");
   m.markDepositRows(orderId,"运营核准取消");rewards.revokeOrderRewards(orderId,"取消申请获运营核准，订单无责结束");
  }else{
   tier=tiers.compute(orderId,"buyer".equals(str(cr.get("applicant_role"))));m.restoreLocked(orderId);
  }
  changed(m.saveDecision(str(cr.get("id")),approve,why,operator));
  notice(str(cr.get("applicant_id")),"cancel_result",approve?"取消申请已获核准":"取消申请被驳回",(approve?"双方保证金已退回，订单结束。":"请继续按约定履约（预演档位："+tier+"）。")+" 说明："+why,orderId);
  return "OK:RESOLVED:"+(approve?"approved":"rejected");
 }
}
