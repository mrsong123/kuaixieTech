package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kuaixietech.mapper.order_fulfillment.ShippingNegotiationMapper;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.order_fulfillment.impl.ShippingNegotiationService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ShippingEscrowTest {
  String buyer="0187c6ba-1788-41a6-8c6e-3aa56343f5b5";
  ShippingNegotiationMapper db;AtomicMoneyService money;ShippingNegotiationService service;Map<String,Object> order;
  @BeforeEach void init(){
    SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(buyer,"test",List.of()));
    db=mock(ShippingNegotiationMapper.class);money=mock(AtomicMoneyService.class);service=new ShippingNegotiationService(db,money);
    order=new HashMap<>(Map.of("buyer_id",buyer,"status","purchased","shipping_fee_status","proposed","shipping_fee",new BigDecimal("20.00"),"buyer_deposit_status","frozen","pricing_version","escrow_v1"));
    when(db.lockOrder("order")).thenReturn(order);
  }
  @AfterEach void clear(){SecurityContextHolder.clearContext();}
  @Test void feeNeedsExactConfirmationBeforeAnyWalletAccess(){
    assertEquals("ERR:SHIPPING_FEE_CHANGED_RECONFIRM",service.respondShippingFee("order",true,null,new BigDecimal("10.00")));
    verifyNoInteractions(money);verify(db,never()).acceptFee(any());
  }
  @Test void acceptedFeeIsHeldOnceAndRepeatedConfirmationIsIdempotent(){
    when(money.lockBalance(buyer)).thenReturn(new BigDecimal("100"));
    when(money.addBalance(any(),any(),any(),any(),any())).thenReturn(new BigDecimal("80"));
    when(db.holdFee(any(),any())).thenReturn(1);when(db.growBuyerDeposit(any(),any(),any())).thenReturn(1);
    when(db.acceptFee("order")).thenAnswer(call->{order.put("shipping_fee_status","agreed");return 1;});
    assertEquals("OK",service.respondShippingFee("order",true,null,new BigDecimal("20")));
    assertEquals("OK",service.respondShippingFee("order",true,null,new BigDecimal("20")));
    verify(money,times(1)).addBalance(eq(buyer),eq(new BigDecimal("-20.00")),eq("shipping_freeze"),eq("order"),any());
    verify(db,times(1)).growBuyerDeposit("order",buyer,new BigDecimal("20.00"));
  }
}
