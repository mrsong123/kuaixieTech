package com.kuaixietech.aftersalesfunds;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.finance.RewardSettlementMapper;
import com.kuaixietech.service.finance.impl.*;
import com.kuaixietech.service.growth.InviteTierMigrationService;
import com.kuaixietech.service.growth.impl.LegacyScoreRpcService;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class AftersalesSettlementTest {
 @Test void actualSettlementOnlyPaysNetGoodsAndWithholdsGrossBasedRewards(){var db=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);var tiers=mock(InviteTierMigrationService.class);var service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),tiers,new ObjectMapper());var refunds=mock(AftersalesEscrowFunds.class);service.escrowRefunds(refunds);var order=EscrowFixture.map("id","order","status","purchased","buyer_id","buyer","vendor_id","vendor","goods_amount",EscrowFixture.d("1000"),"service_fee",EscrowFixture.d("50"),"agreed_json","{\"pricing_version\":\"escrow_v2_external_insurance\"}","insurance_premium",EscrowFixture.d("0"));when(db.lockOrder("order")).thenReturn(order);when(db.markOrderSettled("order")).thenReturn(1);when(refunds.netGoods("order",EscrowFixture.d("1000"))).thenReturn(EscrowFixture.d("800"));assertTrue(service.settleOrderCompletion("order"));verify(money).addBalance(eq("vendor"),eq(EscrowFixture.d("800")),eq("goods_settlement"),eq("order"),any());verify(money,never()).addBalance(eq("vendor"),eq(EscrowFixture.d("1000")),eq("goods_settlement"),any(),any());verify(db,never()).hasFirstOrder(any());verifyNoInteractions(tiers);assertEquals(EscrowFixture.d("1000"),order.get("goods_amount"));}
 @Test void unprovenNetBudgetFailsBeforeAnyWalletOrOrderSettlement(){var db=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);var service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),new ObjectMapper());var refunds=mock(AftersalesEscrowFunds.class);service.escrowRefunds(refunds);when(db.lockOrder("order")).thenReturn(EscrowFixture.map("status","purchased","buyer_id","buyer","vendor_id","vendor","goods_amount",EscrowFixture.d("1000"),"service_fee",EscrowFixture.d("50"),"agreed_json","{\"pricing_version\":\"escrow_v2_external_insurance\"}","insurance_premium",EscrowFixture.d("0")));when(refunds.netGoods(any(),any())).thenThrow(new EscrowFundsProof.Blocked("AS_ESCROW_ACCOUNTING_REQUIRED"));assertThrows(EscrowFundsProof.Blocked.class,()->service.settleOrderCompletion("order"));verifyNoInteractions(money);verify(db,never()).markOrderSettled(any());}
}
