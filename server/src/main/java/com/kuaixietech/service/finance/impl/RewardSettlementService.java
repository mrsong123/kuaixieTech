package com.kuaixietech.service.finance.impl;

import com.kuaixietech.service.growth.InviteTierMigrationService;
import com.kuaixietech.service.growth.impl.LegacyScoreRpcService;
import com.kuaixietech.mapper.finance.RewardSettlementMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Ports grant_first_order_rewards, pay_one_commission, grant_team_commission,
 * revoke_order_rewards, settle_order_completion. Called from trusted server transitions only. */
@Service public class RewardSettlementService {
 private com.kuaixietech.aftersalesfunds.AftersalesEscrowFunds escrowRefunds;
 @org.springframework.beans.factory.annotation.Autowired public void escrowRefunds(com.kuaixietech.aftersalesfunds.AftersalesEscrowFunds escrowRefunds){this.escrowRefunds=escrowRefunds;}
 private com.kuaixietech.commerce.ProductInventory inventory;
 @org.springframework.beans.factory.annotation.Autowired public void productInventory(com.kuaixietech.commerce.ProductInventory inventory){this.inventory=inventory;}
 private final RewardSettlementMapper m;private final AtomicMoneyService money;private final LegacyScoreRpcService scores;private final InviteTierMigrationService tiers;
 private final ObjectMapper json;
 public RewardSettlementService(RewardSettlementMapper m,AtomicMoneyService money,LegacyScoreRpcService scores,InviteTierMigrationService tiers,ObjectMapper json){this.m=m;this.money=money;this.scores=scores;this.tiers=tiers;this.json=json;}
 private static String id(){return UUID.randomUUID().toString();}
 private static String str(Object x){return x==null?null:String.valueOf(x);}
 private static BigDecimal decimal(Object x){return x==null?BigDecimal.ZERO:new BigDecimal(String.valueOf(x));}
 private static boolean defined(String x){return x!=null&&!x.isBlank();}
 private static String s(String x){return x==null?"":x;}
 private void notifyUser(String uid,String kind,String title,String body){m.notice(id(),uid,kind,title,body);}
 /** First-order source awards 5% (L1) and 2% (L2), converting CNY to points at ¥0.1/point. */
 @Transactional public void grantFirstOrderRewards(String invitee,String orderId,BigDecimal goods){
  if(!defined(invitee))return;
  m.lockProfile(invitee); // consistent serialization when two first orders settle concurrently
  if(m.hasFirstOrder(invitee)>0)return;
  String l1=m.getInviter(invitee);if(!defined(l1))return;
  String l2=m.getInviter(l1);
  rewardPoints(l1,invitee,orderId,1,goods,new BigDecimal("0.05"));
  if(defined(l2))rewardPoints(l2,invitee,orderId,2,goods,new BigDecimal("0.02"));
 }
 private void rewardPoints(String inviter,String invitee,String orderId,int level,BigDecimal goods,BigDecimal rate){
  int points=goods.multiply(rate).divide(new BigDecimal("0.1"),0,RoundingMode.HALF_UP).intValueExact();
  if(points<=0)return;
  m.addReward(id(),inviter,invitee,orderId,level,"first_order","points",points,null,null,"points",false,level==1?"受邀人完成首单奖励":"间推下级完成首单奖励");
  m.grantPoints(inviter,points);
  notifyUser(inviter,"invite_reward",level==1?"邀请奖励到账":"间推奖励到账",level==1?"你的受邀伙伴完成首单，获得 "+points+" 积分（约 ¥"+new BigDecimal(points).multiply(new BigDecimal("0.1"))+"），可在钱包兑换代金券":"你的下级邀请的伙伴完成首单，获得 "+points+" 积分");
 }
 /** The inviter profile row is locked before checking duplicate reward and monthly cap. */
 @Transactional public BigDecimal payOneCommission(String inviter,String invitee,String orderId,int level,BigDecimal serviceFee){
  if(!defined(inviter)||!defined(invitee)||serviceFee==null||serviceFee.signum()<=0)return BigDecimal.ZERO;
  Map<String,Object> p=m.lockProfile(inviter);if(p==null)return BigDecimal.ZERO;
  String tier=str(p.get("invite_tier"));
  BigDecimal rate=switch(s(tier)){
   case "l1"->level==1?new BigDecimal("0.08"):BigDecimal.ZERO;
   case "l2"->level==1?new BigDecimal("0.15"):BigDecimal.ZERO;
   case "l3"->level==1?new BigDecimal("0.22"):new BigDecimal("0.10");
   case "l4"->level==1?new BigDecimal("0.30"):new BigDecimal("0.15");
   default->BigDecimal.ZERO;
  };
  if(rate.signum()<=0||m.alreadyCommission(orderId,inviter,level)>0)return BigDecimal.ZERO;
  String month=YearMonth.now(ZoneId.of("Asia/Shanghai")).toString();
  BigDecimal mtd=decimal(p.get("commission_mtd"));
  if(!month.equals(str(p.get("commission_mtd_month")))){m.setCommissionMonth(inviter,month);mtd=BigDecimal.ZERO;}
  BigDecimal amt=serviceFee.multiply(rate).setScale(2,RoundingMode.HALF_UP).min(new BigDecimal("2000.00").subtract(mtd).max(BigDecimal.ZERO));
  if(amt.signum()<=0)return BigDecimal.ZERO;
  m.addReward(id(),inviter,invitee,orderId,level,"team_commission","cash",amt.movePointRight(2).intValueExact(),rate,serviceFee,"cny",true,"下线完成订单提成（服务费的 "+rate.movePointRight(2).stripTrailingZeros().toPlainString()+"%）");
  money.addBalance(inviter,amt,"commission",orderId,"团队订单提成");
  m.addCommissionMonth(inviter,amt);
  notifyUser(inviter,"invite_reward","团队提成到账","你的下线完成一笔订单，提成 ¥"+amt+" 已入余额（本月累计 ¥"+mtd.add(amt)+"）");
  return amt;
 }
 @Transactional public void grantTeamCommission(String invitee,String orderId,BigDecimal serviceFee){
  if(!defined(invitee)||!defined(orderId)||serviceFee==null||serviceFee.signum()<=0)return;
  String up=m.getInviter(invitee);if(!defined(up)||up.equals(invitee))return;
  payOneCommission(up,invitee,orderId,1,serviceFee);
  String top=m.getInviter(up);if(defined(top)&&!top.equals(up)&&!top.equals(invitee))payOneCommission(top,invitee,orderId,2,serviceFee);
 }
 @Transactional public String revokeOrderRewards(String orderId,String reason){
  if(!defined(orderId))return "ERR:NO_ORDER";
  String why=defined(reason)?reason:"订单违约，提成追回";int count=0;
  for(Map<String,Object> row:m.lockCommissionRewards(orderId)){
   String inviter=str(row.get("inviter_id"));
   if(Boolean.TRUE.equals(row.get("settled_to_balance"))||"1".equals(str(row.get("settled_to_balance")))){
    if("cny".equals(str(row.get("currency")))){
     BigDecimal amount=decimal(row.get("amount")).movePointLeft(2).setScale(2,RoundingMode.HALF_UP);
     money.addBalance(inviter,amount.negate(),"commission_rev",orderId,why);
     m.reduceCommission(inviter,amount);
     notifyUser(inviter,"invite_reward_revoked","提成已追回","一笔已计佣订单被判违约/退款，¥"+amount+" 提成已从余额扣回。"+s(reason));
    }
   }
   if(m.revokeReward(str(row.get("id")),why)!=1)throw new IllegalStateException("reward revoke conflict");count++;
  }
  return "OK:REVOKED:"+count;
 }
 @Transactional public boolean settleOrderCompletion(String orderId){
  Map<String,Object> o=m.lockOrder(orderId);
  if(o==null)throw new IllegalArgumentException("订单不存在");
  String status=str(o.get("status"));if("completed".equals(status))return true;
  if(!Set.of("shipped","purchased").contains(status))return false;
  if(!m.activeCases(orderId).isEmpty()||!m.activeAfterSales(orderId).isEmpty())throw new com.kuaixietech.cases.CaseOpenException();
  boolean prepaidInsurance=false;
  try{prepaidInsurance=com.kuaixietech.service.trading.support.IntentPricing.isEscrowVersion(json.readTree(str(o.get("agreed_json"))).path("pricing_version").asText());}
  catch(Exception ignored){/* Legacy orders retain their original accounting. */}
  // A historical fee/choice is not evidence of an issued policy. Do not collect or
  // silently release an unresolved legacy premium as part of trade settlement.
  if(decimal(o.get("insurance_premium")).signum()!=0)throw new com.kuaixietech.insurance.InsurancePremiumReconciliationRequired();
  String buyer=str(o.get("buyer_id")),vendor=str(o.get("vendor_id"));
  BigDecimal goods=decimal(o.get("goods_amount")),fee=decimal(o.get("service_fee")),ship=BigDecimal.ZERO,insurance=BigDecimal.ZERO,tax=BigDecimal.ZERO;
  BigDecimal goodsToSettle=escrowRefunds==null?goods:escrowRefunds.netGoods(orderId,goods);
  boolean hasGoodsRefund=goodsToSettle.compareTo(goods)<0;
  boolean onTime=true;
  if(o.get("promised_arrive_date")!=null){
   LocalDate promised=LocalDate.parse(str(o.get("promised_arrive_date")).substring(0,10));
   // The source checks a timestamp against promised DATE + INTERVAL '1 day' at 00:00.
   LocalDateTime cutoff=promised.plusDays(1).atStartOfDay();
   onTime=!asLocalTime(o.get("shipped_at")).isAfter(cutoff);
  }
  boolean noProof=o.get("delivery_at")==null;
  if("agreed".equals(o.get("shipping_fee_status"))&&arrayLength(o.get("freight_label_urls"))>=1)ship=decimal(o.get("shipping_fee"));
  BigDecimal shippingHeld=BigDecimal.ZERO;
  if(prepaidInsurance){
   try{shippingHeld=decimal(json.readTree(str(o.get("agreed_json"))).path("shipping_escrow").asText("0"));}
   catch(Exception e){throw new IllegalStateException("Invalid shipping escrow snapshot",e);}
   if(ship.compareTo(shippingHeld)>0)throw new IllegalStateException("Shipping fee has not been funded by the buyer");
  }
  if("yes".equals(o.get("insured")))insurance=decimal(o.get("insurance_premium"));
  boolean degraded=false;
  if("refund_to_buyer".equals(o.get("tax_refund_plan"))){
   if(nestedArrayLength(o.get("purchase_evidence"),"tax_refund_receipt")>=1){
    tax=decimal(o.get("tax_refund_amount"));
    if(tax.signum()>0&&defined(buyer)&&defined(vendor)){
     money.addBalance(vendor,tax.negate(),"tax_refund",orderId,"退税金额原路返还买家（方案A）");
     money.addBalance(buyer,tax,"tax_refund",orderId,"收到代购退税返还（方案A）");
     m.setTaxRefundAmount(orderId,tax);
    }
   } else {
    degraded=true;fee=fee.subtract(goods.multiply(new BigDecimal("0.05")).setScale(2,RoundingMode.HALF_UP)).max(BigDecimal.ZERO);
    m.markTaxDegraded(orderId,fee);
    if(defined(buyer))money.addBalance(buyer,goods.multiply(new BigDecimal("0.05")).setScale(2,RoundingMode.HALF_UP),"tax_degrade_comp",orderId,"承诺退税未兑现，代购费差价补偿（方案A→B降级）");
   }
  }
  if(noProof)m.markNoProof(orderId);
  if(m.markOrderSettled(orderId)!=1)throw new IllegalStateException("order settlement concurrent conflict");
  if(inventory!=null)inventory.transition(orderId,"consumed");
  m.releaseDeposits(orderId);
  if(defined(vendor)){
   // New escrow already transfers the tax refund separately above; never deduct it twice.
   // Preserve historical accounting pending reconciliation of pre-versioned orders.
   money.addBalance(vendor,goodsToSettle.subtract(prepaidInsurance?BigDecimal.ZERO:tax).add(ship),"goods_settlement",orderId,"订单完成货款及邮费结算");
   m.updateVendorStats(vendor,onTime);
   scores.applyScoreDelta(vendor,onTime?6:-6,onTime?"ontime_completed":"late_delivery",orderId,onTime?"按时送达并完成交割":"超出承诺时效送达");
  }
  if(prepaidInsurance&&defined(vendor)&&"frozen".equals(o.get("vendor_deposit_status")))
    money.addBalance(vendor,decimal(o.get("vendor_deposit")),"deposit_release",orderId,"订单完成退回带货人履约保证金");
  if(prepaidInsurance&&defined(buyer)&&shippingHeld.compareTo(ship)>0)
    money.addBalance(buyer,shippingHeld.subtract(ship),"shipping_refund",orderId,"未使用的托管邮费退回");
  if(!hasGoodsRefund&&defined(buyer)){if(m.hasFirstOrder(buyer)==0)grantFirstOrderRewards(buyer,orderId,goods);if(fee.signum()>0)grantTeamCommission(buyer,orderId,fee);tiers.refreshInviteTier(buyer);}
  if(!hasGoodsRefund&&defined(vendor)){if(m.hasFirstOrder(vendor)==0)grantFirstOrderRewards(vendor,orderId,goods);if(fee.signum()>0)grantTeamCommission(vendor,orderId,fee);tiers.refreshInviteTier(vendor);}
  m.message(id(),orderId,buyer,"订单已完成结算，货款与保证金按规则释放"+(hasGoodsRefund?"；已扣除退入买家钱包的售后货款，奖励与提成待运营核对":"")+(ship.signum()>0?"；邮费 ¥"+ship+" 已随货款支付带单人":"")+(insurance.signum()>0?"；历史保价费用记录 ¥"+insurance+" 已记录，承保结果须另行核验":"")+(tax.signum()>0?"；退税 ¥"+tax+" 已原路返还买家":"")+(degraded?"；因未提供退税凭证，本单已降级为自留方案并补偿买家代购费差价":"")+(noProof?"；注意：本单未经买家收货刷脸核验即结算，已标记待人工复核":""));
  return true;
 }
 private static LocalDateTime asLocalTime(Object value){
  if(value==null)return LocalDateTime.now();
  if(value instanceof java.sql.Timestamp ts)return ts.toLocalDateTime();
  if(value instanceof LocalDateTime dt)return dt;
  if(value instanceof Date d)return LocalDateTime.ofInstant(d.toInstant(),ZoneId.systemDefault());
  return java.sql.Timestamp.valueOf(String.valueOf(value).replace('T',' ')).toLocalDateTime();
 }
 private int arrayLength(Object value){try{JsonNode n=json.readTree(str(value));return n!=null&&n.isArray()?n.size():0;}catch(Exception e){return 0;}}
 private int nestedArrayLength(Object value,String field){try{JsonNode n=json.readTree(str(value));JsonNode arr=n.path(field);return arr.isArray()?arr.size():0;}catch(Exception e){return 0;}}
}
