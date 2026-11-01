package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.mapper.finance.WithdrawMigrationMapper;
import com.kuaixietech.service.finance.impl.WithdrawMigrationServiceImpl;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;

class UnverifiedPayoutGuardTest {
 private final String admin="operator",user="buyer",id="withdrawal";
 private Map<String,Object> row(String status){return new HashMap<>(Map.of("id",id,"user_id",user,"amount",new BigDecimal("12.34"),"status",status));}
 private WithdrawMigrationMapper authorized(String status){var db=mock(WithdrawMigrationMapper.class);when(db.isAdmin(admin)).thenReturn(1);when(db.lockWithdrawal(id)).thenReturn(row(status));return db;}

 @Test void unsupportedPayoutNeverCreatesOrFreezesANewRequest(){
  var db=mock(WithdrawMigrationMapper.class);var service=new WithdrawMigrationServiceImpl(db);
  for(int i=0;i<2;i++)assertEquals("ERR:WITHDRAWAL_PAYOUT_UNAVAILABLE",service.requestWithdrawal(user,new BigDecimal("12.34")));
  verifyNoInteractions(db);
 }
 @Test void invalidAmountsAndUnauthenticatedRequestsDoNotTouchWallets(){
  var db=mock(WithdrawMigrationMapper.class);var service=new WithdrawMigrationServiceImpl(db);
  assertEquals("ERR:NO_LOGIN",service.requestWithdrawal(null,BigDecimal.TEN));
  for(BigDecimal value:List.of(new BigDecimal("0.99"),new BigDecimal("20000.01"),new BigDecimal("1.001")))assertEquals("ERR:BAD_AMOUNT",service.requestWithdrawal(user,value));
  assertEquals("ERR:BAD_AMOUNT",service.requestWithdrawal(user,null));verifyNoInteractions(db);
 }
 @Test void operatorNoteOrRepeatedPaidClickCannotManufactureAReceipt(){
  var db=authorized("approved");var service=new WithdrawMigrationServiceImpl(db);
  for(String note:List.of("","bank paid","receipt 123456"))assertEquals("ERR:WITHDRAWAL_PAYOUT_UNAVAILABLE",service.markWithdrawalPaid(admin,id,note));
  verify(db,times(3)).isAdmin(admin);verify(db,times(3)).lockWithdrawal(id);verifyNoMoreInteractions(db);
 }
 @Test void historicalRejectedAmountCannotCreditUnprovenSourceFunds(){
  var db=authorized("pending");var service=new WithdrawMigrationServiceImpl(db);
  for(int i=0;i<2;i++)assertEquals("ERR:WITHDRAWAL_SOURCE_RECONCILIATION_REQUIRED",service.reviewWithdrawal(admin,id,false,"资料需核对"));
  verify(db,times(2)).isAdmin(admin);verify(db,times(2)).lockWithdrawal(id);verifyNoMoreInteractions(db);
 }
 @Test void reviewApprovalRecordsOnlyReviewAndCannotBeAppliedTwice(){
  var db=mock(WithdrawMigrationMapper.class);var record=row("pending");when(db.isAdmin(admin)).thenReturn(1);when(db.lockWithdrawal(id)).thenReturn(record);
  when(db.reviewWithdrawal(id,"approved",admin,"资料审核通过")).thenAnswer(call->{record.put("status","approved");return 1;});
  var service=new WithdrawMigrationServiceImpl(db);
  assertEquals("OK:APPROVED",service.reviewWithdrawal(admin,id,true,"资料审核通过"));
  assertEquals("ERR:BAD_STATUS",service.reviewWithdrawal(admin,id,true,"资料审核通过"));
  verify(db,times(2)).isAdmin(admin);verify(db,times(2)).lockWithdrawal(id);verify(db).reviewWithdrawal(id,"approved",admin,"资料审核通过");
  verify(db).insertAdminLog(anyString(),eq(admin),eq("withdrawal_approved"),eq(id),eq("资料审核通过"));verifyNoMoreInteractions(db);
 }
 @Test void unauthorizedOperatorCannotReadWithdrawalOrClaimPayment(){
  var db=mock(WithdrawMigrationMapper.class);var service=new WithdrawMigrationServiceImpl(db);
  assertEquals("ERR:NO_PERM",service.markWithdrawalPaid(user,id,"paid"));verify(db).isAdmin(user);verifyNoMoreInteractions(db);
 }
 @Test void terminalOrMissingRequestsAreNotRelabelledPaid(){
  var db=authorized("paid");var service=new WithdrawMigrationServiceImpl(db);
  assertEquals("ERR:BAD_STATUS",service.markWithdrawalPaid(admin,id,"again"));
  when(db.lockWithdrawal(id)).thenReturn(null);assertEquals("ERR:NO_ROW",service.markWithdrawalPaid(admin,id,"again"));
  verify(db,times(2)).isAdmin(admin);verify(db,times(2)).lockWithdrawal(id);verifyNoMoreInteractions(db);
 }
}
