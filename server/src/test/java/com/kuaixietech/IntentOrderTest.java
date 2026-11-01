package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kuaixietech.entity.portal.IntentInput;
import com.kuaixietech.mapper.trading.IntentOrderMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.trading.impl.IntentOrderService;
import com.kuaixietech.service.trading.support.IntentPricing;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class IntentOrderTest {
  IntentOrderMapper db;DomainAccessGuard guard;AtomicMoneyService money;IntentOrderService service;
  Map<String,Map<String,Object>> saved;
  String trip="b080aa30-2565-4dde-9801-24bcf8943a7b",key="b6d4beac-e1b8-471f-bc82-04e4a15441b4";
  IntentInput input(String name,BigDecimal confirmed){
    return new IntentInput(trip,name,null,1,"piece",null,new BigDecimal("1000.00"),null,"Buyer","13800138000","Address",null,"waived",key,confirmed);
  }
  @BeforeEach void init(){
    db=mock(IntentOrderMapper.class);guard=mock(DomainAccessGuard.class);money=mock(AtomicMoneyService.class);
    service=new IntentOrderService(db,guard,money,24,mock(com.kuaixietech.insurance.InsuranceSelections.class));saved=new HashMap<>();
    when(guard.requireVerifiedBuyer()).thenReturn("buyer");
    Map<String,Object> available=Map.of("id",trip,"owner_id","carrier","capacity_qty",2,"remaining_slots",2);
    when(db.trip(trip)).thenReturn(available);when(db.lockTrip(trip)).thenReturn(available);
    when(db.reservedQuantityTotal(trip)).thenReturn(0);
    when(money.lockBalance("buyer")).thenReturn(new BigDecimal("2000"));
    when(money.addBalance(any(),any(),any(),any(),any())).thenReturn(new BigDecimal("940"));
    when(db.lockExisting(anyString())).thenAnswer(call->saved.get(call.getArgument(0)));
    when(db.insertOrder(any(),any(),any(),any(),any(),any(),any(),anyInt())).thenAnswer(call->{
      String id=call.getArgument(0);saved.put(id,Map.of("id",id,"buyer_id",call.getArgument(2),"status","requesting","agreed_json",call.getArgument(6)));return 1;
    });
    when(db.insertDeposit(any(),any(),any(),any())).thenReturn(1);
  }
  @Test void newQuoteDoesNotCollectPlatformInsurancePremium(){
    var quote=service.quote(input("Camera",null));
    assertEquals(new BigDecimal("1050.00"),quote.buyerEscrow());
    assertEquals(new BigDecimal("0.00"),quote.insurancePremium());
    verifyNoInteractions(money);assertTrue(saved.isEmpty());
  }
  @Test void sameRequestIsChargedOnlyOnce(){
    var request=input("Camera",new BigDecimal("1050.00"));
    var first=service.create(request);var retry=service.create(request);
    assertEquals(first,retry);assertEquals(1,saved.size());
    verify(money,times(1)).addBalance(eq("buyer"),eq(new BigDecimal("-1050.00")),eq("deposit_freeze"),eq(first.get("id")),any());
    verify(db,times(1)).insertDeposit(any(),eq(first.get("id")),eq("buyer"),eq(new BigDecimal("1050.00")));
  }
  @Test void reusedKeyWithDifferentInputCannotChargeAgain(){
    service.create(input("Camera",new BigDecimal("1050")));
    var error=assertThrows(ResponseStatusException.class,()->service.create(input("Other item",new BigDecimal("1050"))));
    assertEquals(409,error.getStatusCode().value());assertEquals(1,saved.size());
    verify(money,times(1)).addBalance(any(),any(),any(),any(),any());
  }
  @Test void insufficientBalanceStaleQuoteAndFullTripDoNotCreateEscrow(){
    when(money.lockBalance("buyer")).thenReturn(new BigDecimal("1049.99"));
    assertThrows(ResponseStatusException.class,()->service.create(input("Camera",new BigDecimal("1050"))));
    when(money.lockBalance("buyer")).thenReturn(new BigDecimal("2000"));
    assertThrows(ResponseStatusException.class,()->service.create(input("Camera",new BigDecimal("1060"))));
    when(db.reservedQuantityTotal(trip)).thenReturn(2);
    assertThrows(ResponseStatusException.class,()->service.create(input("Camera",new BigDecimal("1050"))));
    assertTrue(saved.isEmpty());verify(money,never()).addBalance(any(),any(),any(),any(),any());
  }
  @Test void explicitConfirmationIsRequired(){
    assertThrows(ResponseStatusException.class,()->service.create(input("Camera",null)));
    verifyNoInteractions(money);assertTrue(saved.isEmpty());
  }
  @Test void normalIntentLocksProfilesBeforeBalanceAndRechecksIdentity(){
    var locks=mock(com.kuaixietech.commerce.OrderProfileLocks.class);service.orderProfileLocks(locks);service.create(input("Camera",new BigDecimal("1050.00")));
    var sequence=inOrder(guard,locks,money,db);sequence.verify(guard).requireVerifiedBuyer();sequence.verify(locks).forTrip("buyer",trip);sequence.verify(money).lockBalance("buyer");sequence.verify(guard).requireVerifiedBuyer();sequence.verify(db).lockExisting(any());sequence.verify(db).lockTrip(trip);sequence.verify(db).reservedQuantityTotal(trip);
  }
  @Test void revokedBuyerAfterLockCannotCreateNormalIntent(){
    when(guard.requireVerifiedBuyer()).thenReturn("buyer").thenThrow(new org.springframework.security.access.AccessDeniedException("revoked"));
    assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.create(input("Camera",new BigDecimal("1050.00"))));assertTrue(saved.isEmpty());verify(money,never()).addBalance(any(),any(),any(),any(),any());
  }
  @Test void historicalPricingRetainsOriginalFiveYuanMinimum(){
    var insured=IntentPricing.quote(new BigDecimal("100"),"yes");
    assertEquals(new BigDecimal("5.00"),insured.insurancePremium());
    assertEquals(new BigDecimal("110.00"),insured.buyerEscrow());
    assertEquals(new BigDecimal("105.00"),IntentPricing.quote(new BigDecimal("100"),"waived").buyerEscrow());
    assertEquals(new BigDecimal("5.01"),IntentPricing.quote(new BigDecimal("0.01"),"yes").buyerEscrow());
    assertThrows(IllegalArgumentException.class,()->IntentPricing.quote(BigDecimal.ZERO,"yes"));
    assertThrows(IllegalArgumentException.class,()->IntentPricing.quote(BigDecimal.TEN,"unselected"));
  }
}
