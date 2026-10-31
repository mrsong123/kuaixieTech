package com.kuaixietech.service.finance.impl;
import com.kuaixietech.mapper.finance.WithdrawMigrationMapper;
import com.kuaixietech.service.finance.WithdrawMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
@Service public class WithdrawMigrationServiceImpl implements WithdrawMigrationService {
 private final WithdrawMigrationMapper db;
 public WithdrawMigrationServiceImpl(WithdrawMigrationMapper db){this.db=db;}
 private static String id(){return UUID.randomUUID().toString();}
 private boolean admin(String actor){return actor!=null && db.isAdmin(actor)>0;}
 @Override @Transactional public String requestWithdrawal(String actor,BigDecimal amount){
   if(actor==null || actor.isBlank())return "ERR:NO_LOGIN";
   if(amount==null || amount.compareTo(BigDecimal.ONE)<0 || amount.compareTo(new BigDecimal("20000"))>0)return "ERR:BAD_AMOUNT";
   // Lock profile BEFORE counting open requests: requests for this user serialize.
   BigDecimal balance=db.lockBalance(actor);
   if(balance==null||balance.compareTo(amount)<0)return "ERR:INSUFFICIENT";
   if(db.openWithdrawals(actor)>=3)return "ERR:TOO_MANY_OPEN";
   if(db.debitBalance(actor,amount)!=1)throw new IllegalStateException("concurrent balance mutation");
   db.insertLedger(id(),actor,"withdraw_freeze",amount.negate(),"提现申请冻结");
   db.insertWithdrawal(id(),actor,amount);
   return "OK";
 }
 @Override @Transactional public String reviewWithdrawal(String admin,String withdrawalId,boolean approve,String note){
   if(!admin(admin))return "ERR:NO_PERM";
   if(note==null||note.trim().length()<2)return "ERR:NO_NOTE";
   Map<String,Object> w=db.lockWithdrawal(withdrawalId);
   if(w==null)return "ERR:NO_ROW";
   if(!"pending".equals(w.get("status")))return "ERR:BAD_STATUS";
   if(db.reviewWithdrawal(withdrawalId,approve?"approved":"rejected",admin,note)!=1)throw new IllegalStateException("withdrawal state race");
   if(!approve){
      String user=(String)w.get("user_id"); BigDecimal amount=(BigDecimal)w.get("amount");
      if(db.creditBalance(user,amount)!=1)throw new IllegalStateException("missing wallet");
      db.insertLedger(id(),user,"withdraw_refund",amount,"提现驳回退回："+note);
      db.insertNotification(id(),user,"提现申请被驳回","原因："+note+"，金额已退回余额。");
   }
   db.insertAdminLog(id(),admin,approve?"withdrawal_approved":"withdrawal_rejected",withdrawalId,note);
   return approve?"OK:APPROVED":"OK:REJECTED";
 }
 @Override @Transactional public String markWithdrawalPaid(String admin,String withdrawalId,String note){
   if(!admin(admin))return "ERR:NO_PERM";
   Map<String,Object> w=db.lockWithdrawal(withdrawalId);
   if(w==null)return "ERR:NO_ROW";
   if(!"approved".equals(w.get("status")))return "ERR:BAD_STATUS";
   if(db.markPaid(withdrawalId,note)!=1)throw new IllegalStateException("withdrawal state race");
   BigDecimal amount=((BigDecimal)w.get("amount")).setScale(2);
   db.insertNotification(id(),(String)w.get("user_id"),"提现已到账","¥"+amount.toPlainString()+" 已打款至微信零钱，请查收。");
   db.insertAdminLog(id(),admin,"withdrawal_paid",withdrawalId,note);
   return "OK:PAID";
 }
}
