package com.kuaixietech.aftersalesfunds;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.order_fulfillment.CancellationRpcMapper;
import com.kuaixietech.service.finance.impl.*;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import com.kuaixietech.service.order_fulfillment.impl.*;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
class AftersalesCancellationTest {
 final String buyer="123e4567-e89b-42d3-a456-426614174000",vendor="223e4567-e89b-42d3-a456-426614174000";
 final CancellationRpcMapper db=mock(CancellationRpcMapper.class);final AtomicMoneyService money=mock(AtomicMoneyService.class);final CancellationTierMigrationService tiers=mock(CancellationTierMigrationService.class);final LegacyAdminRpcService admin=mock(LegacyAdminRpcService.class);final RewardSettlementService rewards=mock(RewardSettlementService.class);
 final Map<String,Object> order=EscrowFixture.map("id","order","order_no","order","buyer_id",buyer,"vendor_id",vendor,"status","purchased","buyer_deposit",EscrowFixture.d("870"),"vendor_deposit",EscrowFixture.d("200"),"buyer_deposit_status","frozen","vendor_deposit_status","frozen");
 @BeforeEach void setup(){SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(buyer,"unit",List.of()));when(db.lockOrder("order")).thenReturn(order);when(tiers.compute("order",true)).thenReturn("free");when(money.lockBalance(any())).thenReturn(EscrowFixture.d("10"));when(db.markCancelled(any(),any(),any(),any(),anyBoolean(),any(),any(),any(),any(),any())).thenReturn(1);when(db.addNotice(any(),any(),any(),any(),any(),any())).thenReturn(1);when(db.reduceScore(any(),anyInt(),anyBoolean())).thenReturn(1);when(db.frozenDepositRows("order")).thenReturn(List.of(Map.of("id","buyer-deposit","party_role","buyer"),Map.of("id","vendor-deposit","party_role","vendor")));when(db.finalizeDepositRows(any(),any(),any(),any())).thenReturn(2);}
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 CancellationRpcService service(){return new CancellationRpcService(db,money,tiers,rewards,admin,new ObjectMapper());}
 @Test void cancellationAfterPartialRefundOnlyReturnsTheRemainingEscrow(){assertEquals("OK:free",service().requestCancelV2("order","other",false,"cancel","[]"));verify(money).addBalance(eq(buyer),eq(EscrowFixture.d("870")),eq("refund"),eq("order"),any());verify(money,never()).addBalance(eq(buyer),eq(EscrowFixture.d("1070")),any(),any(),any());verify(db).finalizeDepositRows(eq("order"),eq("refunded"),eq("refunded"),any());}
 @Test void partialForfeitUsesActualPerPartyOutcomesWithinOriginalVarcharLimit(){when(tiers.compute("order",true)).thenReturn("hour_24");assertEquals("OK:hour_24",service().requestCancelV2("order","other",false,"cancel","[]"));verify(money).addBalance(eq(buyer),eq(EscrowFixture.d("609")),eq("refund"),eq("order"),any());verify(money).addBalance(eq(vendor),eq(EscrowFixture.d("261")),eq("refund"),eq("order"),any());verify(db).finalizeDepositRows(eq("order"),eq("partial_forfeit"),eq("refunded"),any());assertTrue("partial_forfeit".length()<=16);}
 @Test void inconsistentFrozenDepositCannotBeSilentlyMarkedUnpaid(){order.put("vendor_deposit_status","unpaid");assertThrows(IllegalStateException.class,()->service().requestCancelV2("order","other",false,"cancel","[]"));verify(db,never()).finalizeDepositRows(any(),any(),any(),any());}
 @Test void concurrentDepositFinalizationFailureAbortsTransaction(){when(db.finalizeDepositRows(any(),any(),any(),any())).thenReturn(1);assertThrows(IllegalStateException.class,()->service().requestCancelV2("order","other",false,"cancel","[]"));verifyNoInteractions(rewards);}
 @Test void adminCancellationKeepsNeverPaidVendorStatusInsteadOfClaimingARefund(){order.put("vendor_deposit_status","unpaid");when(db.latestPending("order")).thenReturn(Map.of("id","cancel","applicant_id",buyer,"reason_type","other"));when(db.frozenDepositRows("order")).thenReturn(List.of(Map.of("id","buyer-deposit","party_role","buyer")));when(db.finalizeDepositRows(any(),any(),any(),any())).thenReturn(1);when(admin.adminGuard(any(),any(),any(),any())).thenReturn("admin");when(db.saveDecision(any(),anyBoolean(),any(),any())).thenReturn(1);assertEquals("OK:RESOLVED:approved",service().adminResolveCancel("order",true,"approved"));verify(db).markCancelled(eq("order"),eq("cancelled_no_fault"),isNull(),eq("other"),eq(false),eq("[]"),eq("refunded"),eq("unpaid"),eq(BigDecimal.ZERO),any());verify(money,never()).addBalance(eq(vendor),any(),any(),any(),any());}
}
