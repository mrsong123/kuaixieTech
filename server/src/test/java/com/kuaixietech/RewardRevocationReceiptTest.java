package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.finance.RewardSettlementMapper;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.finance.impl.RewardSettlementService;
import com.kuaixietech.service.growth.InviteTierMigrationService;
import com.kuaixietech.service.growth.impl.LegacyScoreRpcService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RewardRevocationReceiptTest {
 private final RewardSettlementMapper db=mock(RewardSettlementMapper.class);
 private final AtomicMoneyService money=mock(AtomicMoneyService.class);
 private final RewardSettlementService service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),new ObjectMapper());
 private void reward(){when(db.lockCommissionRewards("order")).thenReturn(List.of(Map.of("id","reward","inviter_id","inviter","amount",1234,"currency","cny","settled_to_balance",true)));}

 @Test void missingWalletCannotClaimThatCommissionWasRecovered(){
  reward();when(money.addBalance("inviter",new BigDecimal("-12.34"),"commission_rev","order","退款核对")).thenReturn(null);
  var error=assertThrows(IllegalStateException.class,()->service.revokeOrderRewards("order","退款核对"));
  assertEquals("REWARD_REVOCATION_WALLET_UNAVAILABLE",error.getMessage());
  verify(db).lockCommissionRewards("order");verifyNoMoreInteractions(db);
  verify(money).addBalance("inviter",new BigDecimal("-12.34"),"commission_rev","order","退款核对");verifyNoMoreInteractions(money);
 }
 @Test void actualDebitReceiptPrecedesRevocationEvenWhenBalanceBecomesNegative(){
  reward();when(money.addBalance("inviter",new BigDecimal("-12.34"),"commission_rev","order","退款核对")).thenReturn(new BigDecimal("-5.01"));when(db.revokeReward("reward","退款核对")).thenReturn(1);
  assertEquals("OK:REVOKED:1",service.revokeOrderRewards("order","退款核对"));
  var sequence=inOrder(db,money);sequence.verify(db).lockCommissionRewards("order");
  sequence.verify(money).addBalance("inviter",new BigDecimal("-12.34"),"commission_rev","order","退款核对");
  sequence.verify(db).reduceCommission("inviter",new BigDecimal("12.34"));
  sequence.verify(db).notice(anyString(),eq("inviter"),eq("invite_reward_revoked"),eq("提成已追回"),contains("12.34"));
  sequence.verify(db).revokeReward("reward","退款核对");sequence.verifyNoMoreInteractions();
 }
}
