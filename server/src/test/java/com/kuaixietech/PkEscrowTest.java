package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.trading.TradeRpcMapper;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.trading.impl.TradeRpcMigrationService;
import com.kuaixietech.service.trading.support.IntentPricing;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class PkEscrowTest {
  String buyer="0187c6ba-1788-41a6-8c6e-3aa56343f5b5",vendor="822de014-ae8d-485f-bd06-cd972ac00970";
  TradeRpcMapper db;AtomicMoneyService money;TradeRpcMigrationService service;
  Map<String,Object> demand;
  @BeforeEach void init(){
    SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(buyer,"test",List.of()));
    db=mock(TradeRpcMapper.class);money=mock(AtomicMoneyService.class);service=new TradeRpcMigrationService(db,money,new ObjectMapper(),mock(com.kuaixietech.insurance.InsuranceSelections.class));
    demand=new HashMap<>(Map.of("id","demand","publisher_id",buyer,"status","open","insurance_pref","waived","title","Camera"));
    when(db.isVerifiedBuyer(buyer)).thenReturn(1);when(db.lockDemand("demand")).thenReturn(demand);
    when(db.quote("quote","demand")).thenReturn(Map.of("price",new BigDecimal("1000.00"),"vendor_id",vendor));
  }
  @AfterEach void clear(){SecurityContextHolder.clearContext();}
  @Test void previewAndMissingConfirmationNeverFreezeFunds(){
    Object preview=service.quotePkAward("demand","quote","waived",null);
    assertInstanceOf(IntentPricing.Quote.class,preview);
    assertEquals(new BigDecimal("1050.00"),((IntentPricing.Quote)preview).buyerEscrow());
    assertEquals("ERR:QUOTE_CONFIRMATION_REQUIRED",service.awardPk("demand","quote","waived",null,null));
    verifyNoInteractions(money);
  }
  @Test void pkAwardUsesSameEscrowAndRetryReturnsTheOriginalOrder(){
    when(money.lockBalance(any())).thenReturn(new BigDecimal("10000"));
    when(money.addBalance(any(),any(),any(),any(),any())).thenReturn(BigDecimal.ONE);
    AtomicReference<String> orderId=new AtomicReference<>();
    when(db.insertPkOrder(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any())).thenAnswer(call->{orderId.set(call.getArgument(0));return 1;});
    when(db.putDeposit(any(),any(),any(),any(),any(),any())).thenReturn(1);
    when(db.setInsuranceSelection(any(),any(),isNull())).thenReturn(1);
    when(db.quoteWon("quote")).thenReturn(1);
    when(db.awardDemand("demand","quote")).thenAnswer(call->{demand.put("status","awarded");demand.put("awarded_quote_id","quote");return 1;});
    when(db.awardedOrder("demand")).thenAnswer(call->orderId.get());
    when(db.addMessage(any(),any(),any(),any())).thenReturn(1);
    when(db.addNotice(any(),any(),any(),any(),any(),any())).thenReturn(1);
    String first=service.awardPk("demand","quote","waived",new BigDecimal("1050"),null);
    assertTrue(first.startsWith("OK:"));
    assertEquals(first,service.awardPk("demand","quote","waived",new BigDecimal("1050"),null));
    verify(money,times(1)).addBalance(eq(buyer),eq(new BigDecimal("-1050.00")),eq("deposit_freeze"),any(),any());
    verify(money,times(1)).addBalance(eq(vendor),eq(new BigDecimal("-200.00")),eq("deposit_freeze"),any(),any());
  }
}
