package com.kuaixietech.service.operations.impl;

import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.mapper.operations.OperationsRpcMapper;
import com.kuaixietech.security.TrustedActor;
import com.kuaixietech.security.DomainAccessGuard;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Migrated admin_grant_points, record_arrival_proof, expire_unformed_pools. */
@Service public class OperationsRpcService {
 private final OperationsRpcMapper m;private final LegacyAdminRpcService admin;private final AtomicMoneyService money;
 public OperationsRpcService(OperationsRpcMapper m,LegacyAdminRpcService admin,AtomicMoneyService money){this.m=m;this.admin=admin;this.money=money;}
 @Transactional public String adminGrantPoints(String uid,Integer points,String note){
  String reason=note==null?"":note.trim();if(reason.isEmpty())return "ERR:NO_NOTE";
  if(points==null||points==0||Math.abs((long)points)>100000)return "ERR:BAD_POINTS";
  admin.adminGuard("points_granted","profile",uid,points+" 分；理由："+reason);
  if(m.grantPoints(uid,points)!=1)throw new IllegalArgumentException("User not found");
  m.rewardLedger(UUID.randomUUID().toString(),uid,points,"运营发放："+reason);
  m.notice(UUID.randomUUID().toString(),uid,"invite_reward",points>0?"积分到账":"积分扣减","平台调整 "+Math.abs((long)points)+" 积分（"+reason+"），可在钱包兑换代金券。",null);
  return "OK:POINTS";
 }
 @Transactional public String recordArrivalProof(String tripId,String snapshotUrl,BigDecimal lat,BigDecimal lng,String city){
  String actor=TrustedActor.currentUserId();Map<String,Object> trip=m.lockTrip(tripId);
  if(trip==null)return "ERR:NO_TRIP";
  if(!actor.equals(trip.get("owner_id")))return "ERR:NOT_OWNER";
  if(snapshotUrl==null||snapshotUrl.trim().length()<5)return "ERR:NO_SNAPSHOT";
  if(lat==null||lng==null||lat.abs().compareTo(new BigDecimal("90"))>0||lng.abs().compareTo(new BigDecimal("180"))>0)return "ERR:NO_LOCATION";
  if(trip.get("arrival_at")!=null)return "ERR:ALREADY_DONE";
  String c=city==null?"":city.trim();
  if(m.saveArrival(tripId,snapshotUrl,c.isEmpty()?null:c)!=1)throw new IllegalStateException("Arrival proof race");
  m.notifyArrival(tripId,c);return "OK";
 }
 /** Internal scheduler only; never export to client endpoints. */
 @Transactional public String expireUnformedPools(){
  int trips=0,orders=0;
  for(Map<String,Object> trip:m.expireTrips()){
   String tripId=String.valueOf(trip.get("id"));
   for(Map<String,Object> row:m.expireOrders(tripId)){
    String orderId=String.valueOf(row.get("id")),buyer=String.valueOf(row.get("buyer_id"));
    String vendor=row.get("vendor_id")==null?null:String.valueOf(row.get("vendor_id"));
    if("frozen".equals(row.get("buyer_deposit_status"))&&amount(row.get("buyer_deposit")).signum()>0)
     money.addBalance(buyer,amount(row.get("buyer_deposit")).add(amount(row.get("service_fee"))),"refund",orderId,"拼单未成团自动退款");
    if(vendor!=null&&"frozen".equals(row.get("vendor_deposit_status"))&&amount(row.get("vendor_deposit")).signum()>0)
     money.addBalance(vendor,amount(row.get("vendor_deposit")),"refund",orderId,"拼单未成团保证金退回");
    if(m.cancelExpiredOrder(orderId)!=1)throw new IllegalStateException("Pool order expired concurrently");
    m.notice(UUID.randomUUID().toString(),buyer,"order","拼单未成行","你参与的顺带拼单到期未凑满，本单已无责取消，保证金已全额退回余额。",null);
    if(vendor!=null)m.notice(UUID.randomUUID().toString(),vendor,"order","拼单未成行","该趟拼单到期未凑满，关联订单已无责取消，你的保证金已退回。",null);
    orders++;
   }
   if(m.expirePool(tripId)!=1)throw new IllegalStateException("Pool expired concurrently");trips++;
  }
  return "OK:trips="+trips+",orders="+orders;
 }
 private static BigDecimal amount(Object o){return o==null?BigDecimal.ZERO:new BigDecimal(String.valueOf(o));}
}
