package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.operations.impl.OperationsRpcService;
import com.kuaixietech.service.trading.support.PkScoring;
import com.kuaixietech.mapper.order_fulfillment.TimeoutAndPaymentMapper;
import com.kuaixietech.mapper.trading.TradeRpcMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Legacy handle_order_timeouts, settle_recharge, and cron wrapper run_expire_unformed_pools.
 * settleRechargeVerified MUST be called only after external provider callback authenticity verification.
 */
@Service public class TimeoutAndPaymentService {
 private final TimeoutAndPaymentMapper m;private final AtomicMoneyService money;private final TradeRpcMapper trade;
 private final OperationsRpcService operations;private final ObjectMapper json;
 public TimeoutAndPaymentService(TimeoutAndPaymentMapper m,AtomicMoneyService money,TradeRpcMapper trade,OperationsRpcService operations,ObjectMapper json){this.m=m;this.money=money;this.trade=trade;this.operations=operations;this.json=json;}
 private static String str(Object x){return x==null?null:String.valueOf(x);}
 private static BigDecimal amount(Object x){return x==null?BigDecimal.ZERO:new BigDecimal(str(x));}
 private static void changed(int n){if(n!=1)throw new IllegalStateException("Concurrent/duplicate transition");}
 private void notifyUser(String uid,String title,String body){if(uid!=null)m.notifyUser(UUID.randomUUID().toString(),uid,"order",title,body,null);}
 /** The original SQL only set deposit_status='refunded', failing to actually credit the wallet.
  * This implementation credits the frozen amount and writes a ledger record in the same transaction. */
 @Transactional public Map<String,Integer> handleOrderTimeouts(){
  int expiredIntent=0,expiredQuote=0,pk=0,closedDemands=0;final int limit=200;
  for(Map<String,Object> o:m.expiredIntentOrders(limit)){
   String id=str(o.get("id")),buyer=str(o.get("buyer_id"));
   if("frozen".equals(str(o.get("buyer_deposit_status"))))money.addBalance(buyer,amount(o.get("buyer_deposit")),"refund",id,"意向超时退还冻结保证金");
   changed(m.closeOrder(id,"requesting","intent_expired"));m.releaseOrderDeposits(id);expiredIntent++;
  }
  for(Map<String,Object> o:m.expiredQuotes(limit)){
   String id=str(o.get("id")),buyer=str(o.get("buyer_id"));
   if("frozen".equals(str(o.get("buyer_deposit_status"))))money.addBalance(buyer,amount(o.get("buyer_deposit")),"refund",id,"议价超时退还冻结保证金");
   changed(m.closeOrder(id,"negotiating","quote_expired"));m.releaseOrderDeposits(id);expiredQuote++;
  }
  int fees=m.expireShippingFees();
  // close_pk requires a user principal in the former implementation; run identical scoring
  // internally from already locked due demand, rather than silently swallowing exceptions.
  for(Map<String,Object> d:m.overduePkDemands(limit)){
   String demandId=str(d.get("id"));List<Map<String,Object>> qs=trade.pendingQuotes(demandId);
   if(qs.isEmpty())continue; // source close_pk rejects NO_QUOTES; do not fabricate a top3
   BigDecimal low=qs.stream().map(q->amount(q.get("price"))).min(BigDecimal::compareTo).orElse(BigDecimal.ONE);
   int days=qs.stream().mapToInt(q->((Number)q.get("promise_days")).intValue()).min().orElse(1);
   List<Map<String,Object>> ranked=new ArrayList<>();for(Map<String,Object> q:qs){Map<String,Object> row=new HashMap<>(q);row.put("score",PkScoring.score(q,low,days));ranked.add(row);}
   ranked.sort(Comparator.<Map<String,Object>,BigDecimal>comparing(q->(BigDecimal)q.get("score")).reversed().thenComparing(q->amount(q.get("price"))));
   List<Map<String,Object>> top=new ArrayList<>();for(int i=0;i<Math.min(3,ranked.size());i++){
    Map<String,Object> q=ranked.get(i);Map<String,Object> item=new LinkedHashMap<>();
    for(String key:List.of("id","vendor_id","price","promise_days","purchase_place","note","score","performance_score","star_avg","deal_count","carrier_tier"))if(q.containsKey(key))item.put("id".equals(key)?"quote_id":key,q.get(key));
    item.put("rank",i+1);top.add(item);
   }
   try{changed(m.savePkResult(demandId,json.writeValueAsString(top)));}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("Cannot serialize PK",e);}
   notifyUser(str(d.get("publisher_id")),"报价 PK 已截止","求购单「"+d.get("title")+"」已生成 Top3，等待终选。 ");pk++;
  }
  for(Map<String,Object> d:m.closeOverdueDemands(limit)){changed(m.closeDemand(str(d.get("id"))));closedDemands++;}
  return Map.of("intentExpired",expiredIntent,"quoteExpired",expiredQuote,"shippingFeeAgreed",fees,"pkClosed",pk,"demandsClosed",closedDemands);
 }
 /** Not HTTP-exposed. Call only after verified WeChat Pay v3 signature, decrypted transaction and amount/order matching. */
 @Transactional public String settleRechargeVerified(String tradeNo,String transactionId){
  if(tradeNo==null||tradeNo.isBlank()||transactionId==null||transactionId.isBlank())return "ERR:BAD_REFERENCE";
  Map<String,Object> p=m.lockRecharge(tradeNo);if(p==null)return "ERR:NO_ORDER";
  if("paid".equals(str(p.get("status"))))return "ALREADY";
  if(!"pending".equals(str(p.get("status"))))return "ERR:BAD_STATUS";
  int cents=((Number)p.get("amount_cents")).intValue();if(cents<=0)throw new IllegalStateException("Invalid recharge order amount");
  changed(m.markRecharge(str(p.get("id")),transactionId));
  String uid=str(p.get("user_id"));BigDecimal funds=BigDecimal.valueOf(cents,2);
  money.addBalance(uid,funds,"recharge",null,"微信支付充值 "+tradeNo);
  m.notifyUser(UUID.randomUUID().toString(),uid,"wallet","充值到账","微信支付 ¥"+funds+" 已入余额。",null);
  return "OK";
 }
 /** run_expire_unformed_pools source delegates to previously ported expiry implementation. */
 @Transactional public String runExpireUnformedPools(){return operations.expireUnformedPools();}
}
