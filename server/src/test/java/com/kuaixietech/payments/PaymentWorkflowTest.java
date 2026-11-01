package com.kuaixietech.payments;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import static com.kuaixietech.payments.PaymentModels.*;

class PaymentWorkflowTest {
  JdbcTemplate jdbc;PaymentStore store;Adapter adapter;DomainAccessGuard guard;FxService fx;PaymentService service;Merchant merchant;
  @BeforeEach void init(){
    jdbc=mock(JdbcTemplate.class);store=spy(new PaymentStore(jdbc));adapter=mock(Adapter.class);guard=mock(DomainAccessGuard.class);fx=mock(FxService.class);
    merchant=new Merchant("merchant","Test fixture","test_fx","credit_card","web",List.of("USD","CNY"),null,100,300,true,1,Map.of());
    doReturn(merchant).when(store).merchant("merchant");when(adapter.id()).thenReturn("test_fx");when(adapter.configured(any())).thenReturn(true);when(adapter.availableCurrencies(any())).thenReturn(Set.of("USD","CNY"));
    when(guard.requireActiveProfile()).thenReturn("user");
    service=new PaymentService(store,fx,guard,mock(PaymentTransactions.class),List.of(adapter));
  }
  @Test void quotePersistsAnExactImmutableCnyToPaymentSnapshot(){
    Instant now=Instant.now();when(fx.rate("USD",true)).thenReturn(new Rate("CNY","USD",new BigDecimal("0.14"),"test source",now.minusSeconds(30),now.plusSeconds(600)));
    Quote quote=service.quote(new QuoteInput("merchant","USD","100.00"));
    assertEquals("100.00",quote.baseAmount());assertEquals("14.14",quote.paymentAmount());assertEquals("CNY",quote.settlementCurrency());
    var args=ArgumentCaptor.forClass(Object[].class);verify(jdbc).update(startsWith("INSERT INTO t_payment_quotes"),args.capture());
    assertEquals(BigInteger.valueOf(10000),args.getValue()[5]);assertEquals(BigInteger.valueOf(1414),args.getValue()[7]);assertEquals(2,args.getValue()[8]);
    assertEquals(merchant,store.snapshot(args.getValue()[4].toString()));
  }
  @Test void sameCurrencyDoesNotInventAnFxMarkup(){
    Instant now=Instant.now();when(fx.rate("CNY",true)).thenReturn(new Rate("CNY","CNY",BigDecimal.ONE,"identity",now,now.plusSeconds(900)));
    Quote quote=service.quote(new QuoteInput("merchant","CNY","100"));assertEquals("100.00",quote.paymentAmount());assertEquals(0,quote.spreadBps());
  }
  @Test void unsupportedCurrencyIsRejectedBeforeRateLookup(){
    assertThrows(IllegalStateException.class,()->service.quote(new QuoteInput("merchant","JPY","100")));verifyNoInteractions(fx);
  }
  @Test void expiredAndReconfiguredQuotesCannotReservePayments(){
    var money=mock(AtomicMoneyService.class);var transactions=new PaymentTransactions(store,money);
    var row=new HashMap<String,Object>(Map.of("id","quote","user_id","user","merchant_id","merchant","merchant_revision",1,"expires_at",Timestamp.from(Instant.now().minusSeconds(1))));
    doAnswer(call->call.getArgument(0,String.class).contains("t_payment_quotes")?row:null).when(store).row(anyString(),any(Object[].class));
    var input=new ConfirmInput("quote","b6d4beac-e1b8-471f-bc82-04e4a15441b4");
    assertThrows(IllegalArgumentException.class,()->transactions.reserve("user",input));
    row.put("expires_at",Timestamp.from(Instant.now().plusSeconds(300)));row.put("merchant_revision",0);
    assertThrows(IllegalArgumentException.class,()->transactions.reserve("user",input));
    verifyNoInteractions(money);verify(jdbc,never()).update(startsWith("INSERT INTO t_currency_payments"),any(Object[].class));
  }
}
