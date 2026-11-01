package com.kuaixietech.service.trading.impl;


import com.kuaixietech.service.trading.support.PkScoring;
import com.kuaixietech.service.trading.support.IntentPricing;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.trading.TradeRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Final-definition RPC ports: accept_order_atomic(v2), submit_pk_quote(v2), close_pk,
 * award_pk(v3), create_publish_deposit, refund_publish_deposit.
 * SQL statements intentionally live in mapper XML; all affected rows participate in one Spring transaction.
 * Authenticated actor comes ONLY from Spring Security; no userId request parameter is trusted.
 */
@Service
public class TradeRpcMigrationService {
 private final TradeRpcMapper mapper;
 private final AtomicMoneyService money;
 private final ObjectMapper json;
 private final com.kuaixietech.insurance.InsuranceSelections insurance;
 public TradeRpcMigrationService(TradeRpcMapper mapper,AtomicMoneyService money,ObjectMapper json,com.kuaixietech.insurance.InsuranceSelections insurance){
    this.mapper=mapper;this.money=money;this.json=json;this.insurance=insurance;
 }
 private static String id(){return UUID.randomUUID().toString();}
 private static String str(Object o){return o==null?null:String.valueOf(o);}
 private static BigDecimal dec(Object o){return o==null?null:new BigDecimal(o.toString());}
 private static String trimOrNull(String x){return x==null||x.trim().isEmpty()?null:x.trim();}
 private static void changed(int count){if(count!=1)throw new IllegalStateException("Unexpected concurrent row change; transaction rolled back");}
 private void notice(String uid,String kind,String title,String body,String orderId){
   changed(mapper.addNotice(id(),uid,kind,title,body,orderId));
 }
 @Transactional
 public String acceptOrderAtomic(String orderId,BigDecimal deposit){
   String actor=TrustedActor.currentUserId();
   Map<String,Object> o=mapper.lockOrder(orderId);
   if(o==null)return "ERR:NO_ORDER";
   if(o.get("vendor_id")!=null&&!actor.equals(str(o.get("vendor_id"))))return "ERR:NOT_VENDOR";
   if(!"requesting".equals(str(o.get("status"))))return "ERR:BAD_STATUS";
   if(mapper.isCarrier(actor)==0)return "ERR:NOT_CARRIER";
   BigDecimal expected=dec(o.get("vendor_deposit"));
   if(deposit==null||deposit.signum()<0||deposit.setScale(2,RoundingMode.HALF_UP)
       .compareTo(expected==null?BigDecimal.ZERO:expected.setScale(2,RoundingMode.HALF_UP))!=0)return "ERR:DEPOSIT_MISMATCH";
   BigDecimal bal=money.lockBalance(actor);
   if(bal==null||bal.compareTo(deposit)<0)return "ERR:INSUFFICIENT_BALANCE";
   if(money.addBalance(actor,deposit.negate(),"deposit_freeze",orderId,"接单保证金冻结")==null)
     throw new IllegalStateException("Account disappeared");
   changed(mapper.putDeposit(id(),orderId,actor,"vendor","carrier_deposit",deposit));
   changed(mapper.acceptOrder(orderId,actor,deposit));
   changed(mapper.addMessage(id(),orderId,actor,"带单人已接单并缴纳履约保证金 ¥"+deposit+"，订单进入锁定状态"));
   notice(str(o.get("buyer_id")),"order","你的订单已被接单","带单人已缴纳履约保证金，将按承诺时效采购，可在订单详情跟踪进度。",orderId);
   return "OK:"+orderId;
 }
 @Transactional
 public String submitPkQuote(String demandId,BigDecimal price,Integer days,String purchasePlace,String note){
   String actor=TrustedActor.currentUserId();
   if(mapper.isCarrier(actor)==0)return "ERR:NOT_CARRIER";
   Map<String,Object> d=mapper.lockDemand(demandId);
   if(d==null)return "ERR:NO_DEMAND";
   if(actor.equals(str(d.get("publisher_id"))))return "ERR:SELF_QUOTE";
   if(!"open".equals(str(d.get("status"))))return "ERR:CLOSED";
   if(mapper.pkWindowExpired(demandId)>0)return "ERR:WINDOW_OVER";
   if(price==null||price.signum()<=0)return "ERR:BAD_PRICE";
   if(days==null||days<1||days>60)return "ERR:BAD_DAYS";
   String tripType=str(d.get("trip_type")),direction=str(d.get("direction_pref"));
   if((tripType!=null&&!"any".equals(tripType))||(direction!=null&&!"any".equals(direction))){
      if(mapper.lineMatches(actor,tripType,direction)==0)return "ERR:LINE_MISMATCH";
   }
   if(mapper.ownPendingQuoteCount(demandId,actor)>1)
     throw new IllegalStateException("Existing duplicate pending quotes require data repair");
   Map<String,Object> old=mapper.pendingOwnQuote(demandId,actor);
   String qid=old==null?id():str(old.get("id"));
   if(old==null)changed(mapper.insertQuote(qid,demandId,actor,price,days,trimOrNull(purchasePlace),trimOrNull(note)));
   else changed(mapper.updatePendingQuote(qid,price,days,trimOrNull(purchasePlace),trimOrNull(note)));
   return "OK:"+qid;
 }
 @Transactional
 public String closePk(String demandId){
   String actor=TrustedActor.currentUserId();
   Map<String,Object> d=mapper.lockDemand(demandId);
   if(d==null)return "ERR:NO_DEMAND";
   boolean admin=mapper.isAdmin(actor)>0;
   if(!actor.equals(str(d.get("publisher_id")))&&!admin)return "ERR:NOT_OWNER";
   if(!"open".equals(str(d.get("status"))))return "ERR:BAD_STATUS";
   if(!admin&&mapper.pkWindowOpen(demandId)>0)return "ERR:WINDOW_OPEN";
   List<Map<String,Object>> all=mapper.pendingQuotes(demandId);
   if(all.isEmpty())return "ERR:NO_QUOTES";
   BigDecimal lowPrice=all.stream().map(q->dec(q.get("price"))).min(BigDecimal::compareTo).orElseThrow();
   int minDays=all.stream().mapToInt(q->((Number)q.get("promise_days")).intValue()).min().orElse(0);
   List<Map<String,Object>> ranked=new ArrayList<>();
   for(Map<String,Object> row:all){
     Map<String,Object> scored=new LinkedHashMap<>(row);
     scored.put("score",PkScoring.score(row,lowPrice,minDays));
     ranked.add(scored);
   }
   ranked.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(x->(BigDecimal)x.get("score")).reversed()
     .thenComparing(x->dec(x.get("price"))).thenComparing(x->str(x.get("id"))));
   List<Map<String,Object>> top=new ArrayList<>();
   for(int i=0;i<Math.min(3,ranked.size());i++){
      Map<String,Object> row=ranked.get(i),entry=new LinkedHashMap<>();
      entry.put("quote_id",row.get("id"));entry.put("vendor_id",row.get("vendor_id"));
      for(String key:List.of("price","promise_days","purchase_place","note","score"))entry.put(key,row.get(key));
      entry.put("rank",i+1);
      for(String key:List.of("performance_score","star_avg","deal_count","carrier_tier"))entry.put(key,row.get(key));
      top.add(entry);
   }
   try{changed(mapper.setPkResult(demandId,json.writeValueAsString(top)));}
   catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("Cannot serialize PK snapshot",e);}
   notice(str(d.get("publisher_id")),"demand","报价 PK 已截止","求购单「"+d.get("title")+"」共收到 "+all.size()+" 份报价，系统已按价格+履约分+时效+完成率综合评分，请在 Top3 中终选。",null);
   return "OK:"+all.size();
 }
 @Transactional
 public String awardPk(String demandId,String quoteId,String insuranceChoice,BigDecimal expectedBuyerEscrow,String insuranceQuoteId){
   String actor=TrustedActor.currentUserId();
   if(mapper.isVerifiedBuyer(actor)==0)return "ERR:BUYER_VERIFICATION_REQUIRED";
   Map<String,Object> d=mapper.lockDemand(demandId);
   if(d==null)return "ERR:NO_DEMAND";
   if(!actor.equals(str(d.get("publisher_id"))))return "ERR:NOT_OWNER";
   if("awarded".equals(str(d.get("status")))&&quoteId!=null&&quoteId.equals(str(d.get("awarded_quote_id")))){
     String existing=mapper.awardedOrder(demandId);
     if(existing!=null)return "OK:"+existing;
   }
   if(!Set.of("open","awarded_pending").contains(str(d.get("status"))))return "ERR:BAD_STATUS";
   Map<String,Object> q=mapper.quote(quoteId,demandId);
   if(q==null)return "ERR:NO_QUOTE";
   if(d.get("pk_result")!=null){
     try{
       JsonNode arr=json.readTree(str(d.get("pk_result")));
       if(!arr.isArray())throw new IllegalStateException("PK result not array");
       boolean found=false;
       for(JsonNode x:arr)if(quoteId.equals(x.path("quote_id").asText()))found=true;
       if(!found)return "ERR:NOT_TOP3";
     }catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("Invalid PK score snapshot",e);}
   }
   if(mapper.countOpenOrders(demandId)>0)return "ERR:ORDER_EXISTS";
   BigDecimal goods=PkScoring.money(q.get("price"));
   String insurance=insuranceChoice==null?str(d.get("insurance_pref")):insuranceChoice;
   String insured="yes".equals(insurance)?"yes":"waived".equals(insurance)?"waived":"unselected";
   if("unselected".equals(insured))return "ERR:INSURANCE_CHOICE_REQUIRED";
   String insuredItem=com.kuaixietech.insurance.InsuranceService.itemName(d);
   try{if("yes".equals(insured))this.insurance.validate(actor,insuranceQuoteId,"pk_quote",quoteId,goods,insuredItem);else if(insuranceQuoteId!=null)return "ERR:INSURANCE_SELECTION_CONFLICT";}catch(IllegalArgumentException e){return "ERR:"+e.getMessage();}
   IntentPricing.Quote pricing=IntentPricing.externalQuote(goods,insured);
   if(expectedBuyerEscrow==null||pricing.buyerEscrow().compareTo(expectedBuyerEscrow)!=0)return "ERR:QUOTE_CONFIRMATION_REQUIRED";
   BigDecimal fee=pricing.serviceFee();
   BigDecimal buyerDep=pricing.buyerEscrow(),vendorDep=pricing.vendorDeposit();
   String buyer=str(d.get("publisher_id")),vendor=str(q.get("vendor_id"));
   if(buyer.equals(vendor))return "ERR:SELF_TRADE";
   // Lock in stable order to avoid a buyer/vendor reverse-order deadlock across concurrent PK awards.
   for(String uid:new TreeSet<>(Arrays.asList(buyer,vendor))){if(money.lockBalance(uid)==null)return uid.equals(buyer)?"ERR:BUYER_BALANCE":"ERR:CARRIER_BALANCE";}
   BigDecimal buyerBal=money.lockBalance(buyer),vendorBal=money.lockBalance(vendor);
   if(buyer.equals(vendor)){
      if(buyerBal.compareTo(buyerDep.add(vendorDep))<0)return "ERR:BUYER_BALANCE";
   }else{
      if(buyerBal.compareTo(buyerDep)<0)return "ERR:BUYER_BALANCE";
      if(vendorBal.compareTo(vendorDep)<0)return "ERR:CARRIER_BALANCE";
   }
   String orderId=id();
   String orderNo="DD"+ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        +String.format("%04d",java.util.concurrent.ThreadLocalRandom.current().nextInt(10000));
   String name=insuredItem;
   BigDecimal premium=pricing.insurancePremium();
   BigDecimal declared="yes".equals(insured)?goods:null;
   boolean carrierPays="carrier_pays".equals(str(d.get("shipping_bearer")));
   changed(mapper.insertPkOrder(orderId,orderNo,buyer,vendor,demandId,name,goods,fee,buyerDep,vendorDep,
      "yes".equals(insured)?"unselected":"waived",premium,declared,carrierPays?BigDecimal.ZERO:null,carrierPays?"agreed":"none"));
   changed(mapper.setInsuranceSelection(orderId,insured,insuranceQuoteId));
   if(money.addBalance(buyer,buyerDep.negate(),"deposit_freeze",orderId,"中标自动建单·货款与5%服务费托管；保险由承保机构另行收款")==null)
     throw new IllegalStateException("Missing buyer account");
   if(money.addBalance(vendor,vendorDep.negate(),"deposit_freeze",orderId,"中标自动建单·带货保证金冻结（货值20%）")==null)
     throw new IllegalStateException("Missing vendor account");
   changed(mapper.putDeposit(id(),orderId,buyer,"buyer","buyer_escrow",buyerDep));
   changed(mapper.putDeposit(id(),orderId,vendor,"vendor","carrier_deposit",vendorDep));
   changed(mapper.quoteWon(quoteId));
   mapper.remainingQuotesLost(demandId,quoteId);
   changed(mapper.awardDemand(demandId,quoteId));
   changed(mapper.addMessage(id(),orderId,buyer,"求购单中标自动生成订单：报价 ¥"+goods+"，代购服务费 ¥"+fee+
     "；买家托管 ¥"+buyerDep+"、带单人履约保证金 ¥"+vendorDep+" 均已冻结"+
     ("yes".equals(insured)?"；已选择外部保险报价，尚未承保":"")+
     (carrierPays?"；邮费由带单人承担（已议定 ¥0）":"")));
   notice(vendor,"demand","你的报价已中标，订单已自动生成","求购单「"+d.get("title")+"」报价被采纳。带货保证金 ¥"+vendorDep+" 已从余额冻结，请尽快按时采购并在订单中确认。",orderId);
   notice(buyer,"order","中标订单已生成并完成资金冻结","已按报价自动锁定订单 "+orderNo+"，商品款+服务费 ¥"+buyerDep+" 已冻结托管。",orderId);
   return "OK:"+orderId;
 }
 @Transactional
 public Object quotePkAward(String demandId,String quoteId,String insuranceChoice,String insuranceQuoteId){
   String actor=TrustedActor.currentUserId();
   if(mapper.isVerifiedBuyer(actor)==0)return Map.of("err","BUYER_VERIFICATION_REQUIRED");
   Map<String,Object> demand=mapper.lockDemand(demandId);
   if(demand==null||!actor.equals(str(demand.get("publisher_id"))))return Map.of("err","NOT_OWNER");
   if(!Set.of("open","awarded_pending").contains(str(demand.get("status"))))return Map.of("err","BAD_STATUS");
   Map<String,Object> quote=mapper.quote(quoteId,demandId);
   if(quote==null)return Map.of("err","NO_QUOTE");
   try{String choice=insuranceChoice==null?str(demand.get("insurance_pref")):insuranceChoice;BigDecimal goods=PkScoring.money(quote.get("price"));if("yes".equals(choice))this.insurance.validate(actor,insuranceQuoteId,"pk_quote",quoteId,goods,com.kuaixietech.insurance.InsuranceService.itemName(demand));else if(insuranceQuoteId!=null)throw new IllegalArgumentException("INSURANCE_SELECTION_CONFLICT");return IntentPricing.externalQuote(goods,choice);}
   catch(IllegalArgumentException e){return Map.of("err",e.getMessage());}
 }
 @Transactional
 public String createPublishDeposit(BigDecimal amount,String source,String orderId){
   String actor=TrustedActor.currentUserId();
   if(amount==null||amount.signum()<=0)return "ERR:BAD_AMOUNT";
   BigDecimal bal=money.lockBalance(actor);
   if(bal==null||bal.compareTo(amount)<0)return "ERR:INSUFFICIENT_BALANCE";
   if(money.addBalance(actor,amount.negate(),"deposit_freeze",orderId,"发布保证金冻结")==null)
       throw new IllegalStateException("Missing profile");
   changed(mapper.createPublishDeposit(id(),actor,amount,trimOrNull(source)==null?"demand":source,orderId));
   return "OK";
 }
 @Transactional
 public String refundPublishDeposit(){
   String actor=TrustedActor.currentUserId();
   // Serialize per user before checking frozen rows and incomplete orders.
   if(money.lockBalance(actor)==null)return "ERR:NO_FROZEN";
   int unfinished=mapper.unfinishedOrders(actor);
   if(unfinished>0)return "ERR:HAS_UNFINISHED:"+unfinished;
   BigDecimal total=mapper.sumFrozenPublish(actor);
   if(total==null||total.signum()<=0)return "ERR:NO_FROZEN";
   changed(mapper.refundPublish(actor)>0?1:0);
   if(money.addBalance(actor,total,"deposit_refund",null,"全部订单已完结，申请退回")==null)
      throw new IllegalStateException("Missing profile");
   return "OK:"+total.setScale(2,RoundingMode.HALF_UP);
 }
}
