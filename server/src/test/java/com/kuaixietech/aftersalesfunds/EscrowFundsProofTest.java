package com.kuaixietech.aftersalesfunds;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class EscrowFundsProofTest {
 void blocked(EscrowFixture f,String expected){var e=assertThrows(EscrowFundsProof.Blocked.class,()->f.proof.prove("order",f.order,true));assertEquals(expected,e.getMessage());verifyNoInteractions(f.money);assertTrue(f.writes.isEmpty());}
 @Test void coherentUnspentOriginalProductEscrowIsProvable(){var f=new EscrowFixture();var p=f.proof.prove("order",f.order,true);assertEquals(EscrowFixture.d("1000"),p.remainingGoods());assertEquals(EscrowFixture.d("1070"),p.held());}
 @Test void settledOrdersCannotDrawAgain(){var f=new EscrowFixture();f.order.put("settled_at","done");blocked(f,"AS_ESCROW_ORDER_NOT_UNSETTLED");}
 @Test void completedOrderIsNotAnUnspentEscrowSource(){var f=new EscrowFixture();f.order.put("status","completed");blocked(f,"AS_ESCROW_ORDER_NOT_UNSETTLED");}
 @Test void legacyOrdersDoNotInheritProductBudget(){var f=new EscrowFixture();f.order.put("source","legacy");blocked(f,"AS_ESCROW_ORDER_NOT_UNSETTLED");}
 @Test void historicalInsuranceChargeRequiresReconciliation(){var f=new EscrowFixture();f.order.put("insurance_premium",EscrowFixture.d("5"));blocked(f,"AS_ESCROW_HISTORICAL_INSURANCE_REQUIRES_RECONCILIATION");}
 @Test void anyTaxRefundPlanRequiresReviewIncludingMissingProofCompensation(){var f=new EscrowFixture();f.order.put("tax_refund_plan","refund_to_buyer");blocked(f,"AS_ESCROW_TAX_REQUIRES_RECONCILIATION");}
 @Test void inventoryMustAlreadyBeConsumedAndIsNotRestocked(){var f=new EscrowFixture();f.reservation.put("stock_state","held");blocked(f,"AS_ESCROW_PRODUCT_ATTRIBUTION_UNPROVEN");}
 @Test void frozenColumnAloneCannotProveRealDebit(){var f=new EscrowFixture();f.ledgers.removeFirst();blocked(f,"AS_ESCROW_FUNDING_LEDGER_UNPROVEN");}
 @Test void wrongOriginalDebitCannotMintRefund(){var f=new EscrowFixture();f.ledgers.getFirst().put("amount",EscrowFixture.d("-1000"));blocked(f,"AS_ESCROW_INITIAL_DEBIT_MISMATCH");}
 @Test void anyPriorOtherPartyPayoutBlocksAutomaticRefund(){var f=new EscrowFixture();f.ledgers.add(EscrowFixture.map("id","other","user_id","vendor","kind","deposit_release","amount",EscrowFixture.d("200")));blocked(f,"AS_ESCROW_OTHER_PARTY_ALREADY_PAID");}
 @Test void historicalGoodsSettlementCannotBeIgnored(){var f=new EscrowFixture();f.ledgers.add(EscrowFixture.map("id","payout","user_id","vendor","kind","goods_settlement","amount",EscrowFixture.d("1000")));blocked(f,"AS_ESCROW_GOODS_ALREADY_PAID");}
 @Test void anyUnexplainedBuyerRefundBlocksAutomaticRefund(){var f=new EscrowFixture();f.ledgers.add(EscrowFixture.map("id","legacy","user_id","buyer","kind","refund","amount",EscrowFixture.d("100")));blocked(f,"AS_ESCROW_OTHER_WALLET_MOVEMENT");}
 @Test void depositAndOrderMustAgreeExactly(){var f=new EscrowFixture();f.deposit.put("amount",EscrowFixture.d("1050"));blocked(f,"AS_ESCROW_FROZEN_BALANCE_MISMATCH");}
 @Test void alreadyAwardedOrderRequiresAccounting(){var f=new EscrowFixture();f.rewards.add(EscrowFixture.map("id","reward"));blocked(f,"AS_ESCROW_REWARDS_REQUIRE_RECONCILIATION");}
 @Test void driftedRefundSourceDepositCannotBeReused(){var f=new EscrowFixture();f.service.execute("case",f.input());f.executions.getFirst().put("source_deposit_id","other-deposit");var e=assertThrows(EscrowFundsProof.Blocked.class,()->f.proof.prove("order",f.order,true));assertEquals("AS_ESCROW_REFUND_CHAIN_INVALID",e.getMessage());}
 @Test void driftedBeforeAfterChainFailsClosed(){var f=new EscrowFixture();f.service.execute("case",f.input());f.executions.getFirst().put("buyer_escrow_before",EscrowFixture.d("1071"));assertThrows(EscrowFundsProof.Blocked.class,()->f.proof.prove("order",f.order,true));}
 @Test void proofDoesNotRangeLockImmutableLedgersBeforeWalletLock(){var f=new EscrowFixture();f.proof.prove("order",f.order,true);assertTrue(f.queries.stream().filter(q->q.contains("FROM t_balance_ledger")||q.contains("FROM t_reward_ledger")||q.startsWith("SELECT * FROM t_order_escrow_refunds")).noneMatch(q->q.contains("FOR UPDATE")));}
}
