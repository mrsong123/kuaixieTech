package com.kuaixietech.service.operations.impl;

import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.growth.impl.LegacyScoreRpcService;
import com.kuaixietech.mapper.operations.LegacyAdminRpcMapper;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import java.math.BigDecimal;
import com.kuaixietech.security.TrustedActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Batch migration of admin RPCs. Authorized exclusively by trusted Spring Security actor context. */
@Service
public class LegacyAdminRpcService {
 private com.kuaixietech.commerce.ProductInventory inventory;
 @org.springframework.beans.factory.annotation.Autowired public void productInventory(com.kuaixietech.commerce.ProductInventory inventory){this.inventory=inventory;}
  private final LegacyAdminRpcMapper mapper;
  private final DomainAccessGuard guard;
  private final LegacyScoreRpcService scores;
  private final AtomicMoneyService money;
  public LegacyAdminRpcService(LegacyAdminRpcMapper mapper,DomainAccessGuard guard,LegacyScoreRpcService scores,AtomicMoneyService money){
    this.mapper=mapper;this.guard=guard;this.scores=scores;this.money=money;
  }
  private void audit(String actor,String action,String type,String id,String description){
    mapper.writeAdminLog(UUID.randomUUID().toString(),actor,action,type,id,description);
  }
  /** Admin guard: authorize and persist audit log in the same transaction. */
  @Transactional
  public String adminGuard(String action,String targetType,String targetId,String detail){
    String actor=guard.requireAdmin();audit(actor,action,targetType,targetId,detail);return actor;
  }
  /** Final 20261021_183941 admin_adjust_balance definition. */
  @Transactional
  public String adjustBalance(String targetId,BigDecimal delta,String note){
    String reason=note==null?"":note.trim();
    if(reason.isEmpty())return "ERR:NO_NOTE";
    if(delta==null||delta.abs().compareTo(new BigDecimal("0.01"))<0)return "ERR:BAD_AMOUNT";
    if(delta.abs().compareTo(new BigDecimal("100000"))>0)return "ERR:TOO_LARGE";
    String actor=adminGuard("balance_adjusted","profile",targetId,"调账 "+delta+" 元；理由："+reason);
    if(actor.equals(targetId))return "ERR:CANNOT_SELF";
    BigDecimal after=money.addBalance(targetId,delta,"adjust",null,"运营调账："+reason);
    mapper.notifyUser(UUID.randomUUID().toString(),targetId,"balance_adjusted","余额变动通知",
       "平台为你调整余额 "+delta+" 元，当前余额 "+(after==null?"0":after)+" 元。说明："+reason,null);
    return "OK:BALANCE:"+(after==null?"0":after);
  }
  /** Read-only commission audit: never expose rows to a non-admin principal. */
  @Transactional(readOnly=true)
  public List<Map<String,Object>> commissionRows(Integer limit){
    String actor=TrustedActor.currentUserId();
    if(mapper.adminExists(actor)==0)return List.of();
    return mapper.commissionRows(limit==null?100:Math.max(1,limit));
  }
  /** Port of admin_dissolve_pool: releases buyer frozen amount, closes seats and notifies. */
  @Transactional
  public String dissolvePool(String tripId,String reason){
    String r=reason==null?"":reason.trim();
    if(r.isEmpty())return "ERR:NO_NOTE";
    Map<String,Object> trip=mapper.lockTrip(tripId);
    if(trip==null)return "ERR:NO_TRIP";
    if(!"pool".equals(String.valueOf(trip.get("carry_mode"))))return "ERR:NOT_POOL";
    String actor=adminGuard("pool_dissolved","trip",tripId,r);
    int n=0;
    for(Map<String,Object> o:mapper.lockPoolOrders(tripId)){
      String id=String.valueOf(o.get("id")),buyer=String.valueOf(o.get("buyer_id"));
      int frozen=mapper.refundBuyerDeposit(id,r);
      if(frozen>0){
        BigDecimal goods=(BigDecimal)o.get("goods_amount"),fee=(BigDecimal)o.get("service_fee");
        money.addBalance(buyer,(goods==null? BigDecimal.ZERO:goods).add(fee==null? BigDecimal.ZERO:fee),
          "refund",id,"拼单解散退款："+r);
      }
      mapper.cancelPoolOrder(id,r);if(inventory!=null)inventory.transition(id,"released");
      mapper.notifyUser(UUID.randomUUID().toString(),buyer,"pool_dissolved","拼单已解散，款项已退回",
        "原因："+r+"。冻结款项已原路退回你的余额。",id);
      n++;
    }
    mapper.expireDissolvedTrip(tripId);
    // Sender owner is in trip row: if base lockTrip did not select owner, query must be extended.
    mapper.notifyUser(UUID.randomUUID().toString(),String.valueOf(trip.get("owner_id")),"pool_dissolved","你的拼单行程已解散",
      "原因："+r+"。该趟 "+n+" 位参与人的款项已由平台退回。",null);
    return "OK:DISSOLVED:"+n;
  }
  @Transactional
  public String broadcast(String title,String body,String scope,List<String> userIds){
    String t=title==null?"":title.trim(),b=body==null?"":body.trim();
    if(t.isEmpty()||t.length()>40)return "ERR:BAD_TITLE";
    if(b.isEmpty()||b.length()>500)return "ERR:BAD_BODY";
    if(scope==null||!Set.of("all","buyer","carrier","selected").contains(scope))return "ERR:BAD_SCOPE";
    if("selected".equals(scope)&&(userIds==null||userIds.isEmpty()))return "ERR:NO_TARGET";
    String actor=guard.requireAdmin();
    audit(actor,"broadcast_sent","notification",scope,t);
    int sent="selected".equals(scope)?mapper.broadcastSelected(t,b,userIds):mapper.broadcastByRole(t,b,scope);
    return "OK:SENT:"+sent;
  }
  @Transactional
  public String closeDemand(String demandId,String reason){
    String r=reason==null?"":reason.trim();if(r.isEmpty())return "ERR:NO_NOTE";
    Map<String,Object> demand=mapper.lockDemand(demandId);
    if(demand==null)return "ERR:NO_DEMAND";
    if(Set.of("awarded","closed","expired").contains(String.valueOf(demand.get("status"))))return "ERR:ALREADY_SETTLED";
    String actor=guard.requireAdmin();audit(actor,"demand_closed","demand",demandId,r);
    mapper.closeDemand(demandId);mapper.rejectOpenQuotes(demandId);
    mapper.notifyUser(UUID.randomUUID().toString(),(String)demand.get("publisher_id"),"demand_closed","你的求购单已被平台关闭",
      "原因："+r+"。如对处理有异议可联系平台客服复核。",null);
    return "OK:CLOSED";
  }
  @Transactional
  public String fixScore(String userId,Integer delta,String note){
    String r=note==null?"":note.trim();if(r.isEmpty())return "ERR:NO_NOTE";
    if(delta==null||delta==0||Math.abs((long)delta)>100)return "ERR:BAD_DELTA";
    String actor=guard.requireAdmin();audit(actor,"score_fixed","profile",userId,delta+" 分；理由："+r);
    scores.applyScoreDelta(userId,delta,"admin_ban",null,"运营修正："+r);
    return "OK:SCORE";
  }
  @Transactional
  public String forceFormPool(String tripId){
    Map<String,Object> trip=mapper.lockTrip(tripId);
    if(trip==null)return "ERR:NO_TRIP";
    if(!"pool".equals(trip.get("carry_mode")))return "ERR:NOT_POOL";
    if("formed".equals(trip.get("pool_status")))return "ERR:ALREADY_FORMED";
    if("expired".equals(trip.get("pool_status")))return "ERR:POOL_EXPIRED";
    int taken=mapper.takenPoolSeats(tripId);
    String actor=guard.requireAdmin();audit(actor,"pool_forced_formed","trip",tripId,"已占 "+taken+"/"+trip.get("pool_slots")+" 席");
    mapper.markPoolFormed(tripId);mapper.notifyPoolBuyers(tripId);
    return "OK:FORMED:"+taken;
  }
  @Transactional
  public String banUser(String userId, boolean ban, String reason){
    String r=reason==null?"":reason.trim();
    if(r.isEmpty())return "ERR:NO_NOTE";
    String actor=guard.requireAdmin();
    audit(actor,ban?"user_banned":"user_unbanned","profile",userId,r);
    if(actor.equals(userId))return "ERR:CANNOT_SELF";
    // Ensure user exists before sending notification; source was dependent on FK failure for missing user.
    if(mapper.lockProfile(userId)==null)throw new IllegalArgumentException("User does not exist");
    if(ban){
      mapper.setBan(userId,true);int open=mapper.countOpenOrders(userId);mapper.pauseTrips(userId);
      mapper.notifyUser(UUID.randomUUID().toString(),userId,"account_banned","账号已被平台限制",
        "原因："+(reason==null||reason.trim().isEmpty()?"违反平台规则":reason.trim())+"。你仍有 "+open+" 笔未完成订单，如需申诉请联系平台客服。",null);
      return "OK:BANNED:"+open;
    }
    mapper.setBan(userId,false);
    mapper.notifyUser(UUID.randomUUID().toString(),userId,"account_restored","账号限制已解除","你可以正常使用发布行程、下单与邀请功能。",null);
    return "OK:UNBANNED";
  }
}
