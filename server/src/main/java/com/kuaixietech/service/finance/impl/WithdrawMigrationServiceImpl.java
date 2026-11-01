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
 private boolean admin(String actor){return actor!=null && !actor.isBlank() && Integer.valueOf(1).equals(db.isAdmin(actor));}
 @Override @Transactional public String requestWithdrawal(String actor,BigDecimal amount){
   if(actor==null || actor.isBlank())return "ERR:NO_LOGIN";
   if(amount==null || amount.compareTo(BigDecimal.ONE)<0 || amount.compareTo(new BigDecimal("20000"))>0 || amount.stripTrailingZeros().scale()>2)return "ERR:BAD_AMOUNT";
   // No payout adapter or verified payout receipt exists yet. Do not create an
   // unpayable request or freeze new funds merely because this migration stub exists.
   return "ERR:WITHDRAWAL_PAYOUT_UNAVAILABLE";
 }
 @Override @Transactional public String reviewWithdrawal(String admin,String withdrawalId,boolean approve,String note){
   if(!admin(admin))return "ERR:NO_PERM";
   if(note==null||note.trim().length()<2)return "ERR:NO_NOTE";
   Map<String,Object> w=db.lockWithdrawal(withdrawalId);
   if(w==null)return "ERR:NO_ROW";
   if(!"pending".equals(w.get("status")))return "ERR:BAD_STATUS";
   if(!approve){
      // Legacy rows do not link a request to a unique original freeze ledger.
      // The amount alone cannot prove that crediting this wallet is a refund.
      return "ERR:WITHDRAWAL_SOURCE_RECONCILIATION_REQUIRED";
   }
   if(note.trim().length()>2000)return "ERR:BAD_NOTE";
   if(db.reviewWithdrawal(withdrawalId,"approved",admin,note)!=1)throw new IllegalStateException("withdrawal state race");
   db.insertAdminLog(id(),admin,"withdrawal_approved",withdrawalId,note);
   // Review is distinct from dispatch and does not imply that money was sent.
   return "OK:APPROVED";
 }
 @Override @Transactional public String markWithdrawalPaid(String admin,String withdrawalId,String note){
   if(!admin(admin))return "ERR:NO_PERM";
   Map<String,Object> w=db.lockWithdrawal(withdrawalId);
   if(w==null)return "ERR:NO_ROW";
   if(!"approved".equals(w.get("status")))return "ERR:BAD_STATUS";
   // An operator note cannot stand in for an authenticated provider receipt.
   return "ERR:WITHDRAWAL_PAYOUT_UNAVAILABLE";
 }
}
