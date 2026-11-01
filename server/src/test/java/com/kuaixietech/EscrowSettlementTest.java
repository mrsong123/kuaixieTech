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
import java.time.LocalDateTime;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class EscrowSettlementTest {
  @Test void shippingCannotBePaidFromUnfundedEscrow(){
    var db=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);
    var service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),new ObjectMapper());
    var order=new HashMap<String,Object>();
    order.put("status","shipped");order.put("buyer_id","buyer");order.put("vendor_id","carrier");
    order.put("shipping_fee_status","agreed");order.put("shipping_fee",new BigDecimal("20"));
    order.put("freight_label_urls","[\"label\"]");order.put("agreed_json","{\"pricing_version\":\"escrow_v1\"}");
    when(db.lockOrder("order")).thenReturn(order);
    assertThrows(IllegalStateException.class,()->service.settleOrderCompletion("order"));
    verifyNoInteractions(money);verify(db,never()).markOrderSettled(any());verify(db,never()).markNoProof(any());
  }
  @Test void taxAlreadyTransferredIsNotDeductedFromGoodsAgain(){
    var db=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);
    var service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),new ObjectMapper());
    var order=new HashMap<String,Object>();
    order.put("status","shipped");order.put("buyer_id","buyer");order.put("vendor_id","carrier");
    order.put("goods_amount",new BigDecimal("1000.00"));order.put("tax_refund_plan","refund_to_buyer");
    order.put("tax_refund_amount",new BigDecimal("100.00"));order.put("purchase_evidence","{\"tax_refund_receipt\":[\"receipt\"]}");
    order.put("agreed_json","{\"pricing_version\":\"escrow_v1\"}");
    when(db.lockOrder("order")).thenReturn(order);when(db.markOrderSettled("order")).thenReturn(1);
    when(db.hasFirstOrder(any())).thenReturn(1);
    assertTrue(service.settleOrderCompletion("order"));
    verify(money).addBalance(eq("carrier"),eq(new BigDecimal("-100.00")),eq("tax_refund"),eq("order"),any());
    verify(money).addBalance(eq("carrier"),eq(new BigDecimal("1000.00")),eq("goods_settlement"),eq("order"),any());
  }
  @Test void externalInsuranceIsNotDebitedAndCarrierDepositIsReturnedOnce(){
    var db=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);
    var service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),new ObjectMapper());
    var order=new HashMap<String,Object>();
    order.put("id","order");order.put("status","shipped");order.put("buyer_id","buyer");order.put("vendor_id","carrier");
    order.put("goods_amount",new BigDecimal("1000.00"));order.put("service_fee",new BigDecimal("50.00"));
    order.put("insurance_premium",new BigDecimal("0.00"));order.put("insured","yes");order.put("delivery_at",LocalDateTime.now());
    order.put("vendor_deposit",new BigDecimal("200.00"));order.put("vendor_deposit_status","frozen");
    order.put("agreed_json","{\"pricing_version\":\"escrow_v2_external_insurance\",\"insurance_escrow\":0}");
    when(db.lockOrder("order")).thenReturn(order);
    when(db.markOrderSettled("order")).thenAnswer(call->{order.put("status","completed");return 1;});
    when(db.hasFirstOrder(any())).thenReturn(1);
    assertTrue(service.settleOrderCompletion("order"));
    assertTrue(service.settleOrderCompletion("order"));
    verify(money,never()).addBalance(eq("buyer"),any(),eq("insurance_fee"),any(),any());
    verify(money,times(1)).addBalance(eq("carrier"),eq(new BigDecimal("200.00")),eq("deposit_release"),eq("order"),any());
    verify(db,times(1)).markOrderSettled("order");
  }
  @Test void unresolvedLegacyPremiumBlocksSettlementBeforeAnyFinancialMutation(){
    var db=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);
    var service=new RewardSettlementService(db,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),new ObjectMapper());
    when(db.lockOrder("order")).thenReturn(java.util.Map.of("status","shipped","buyer_id","buyer","vendor_id","carrier","insurance_premium",new BigDecimal("10.00"),"agreed_json","{\"pricing_version\":\"escrow_v1\"}"));
    assertThrows(IllegalStateException.class,()->service.settleOrderCompletion("order"));verifyNoInteractions(money);verify(db,never()).markOrderSettled(any());
  }

}
