package com.kuaixietech.payments;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import static com.kuaixietech.payments.PaymentModels.*;

class PaymentReceiptTest {
  JdbcTemplate db;AtomicMoneyService money;PaymentTransactions transactions;Map<String,Object> payment;
  @BeforeEach void init(){
    db=mock(JdbcTemplate.class);money=mock(AtomicMoneyService.class);transactions=new PaymentTransactions(new PaymentStore(db),money);
    payment=new HashMap<>(Map.of("id","payment","user_id","user","merchant_id","merchant","base_currency","CNY","base_minor",new BigDecimal("10000"),"payment_currency","USD","payment_minor",new BigDecimal("1400"),"status","pending"));
    payment.put("payment_minor_digits",2);
    when(db.queryForList(anyString(),any(Object[].class))).thenAnswer(call->List.of(payment));
    when(db.update(startsWith("UPDATE t_currency_payments SET status='paid'"),any(Object[].class))).thenAnswer(call->{payment.put("status","paid");payment.put("provider_transaction_id","provider-transaction");return 1;});
    when(db.update(startsWith("INSERT INTO t_currency_payment_receipts"),any(Object[].class))).thenReturn(1);
    when(money.addBalance(any(),any(),any(),any(),any())).thenReturn(new BigDecimal("100.00"));
  }
  @Test void verifiedForeignPaymentCreditsOnlyTheLockedCnyAmountOnce(){
    var receipt=new ProviderPayment("payment","provider-transaction","USD",BigInteger.valueOf(1400),true);
    assertTrue(transactions.settle("payment",receipt));assertTrue(transactions.settle("payment",receipt));
    verify(money,times(1)).addBalance(eq("user"),eq(new BigDecimal("100.00")),eq("fx_recharge"),isNull(),any());
  }
  @Test void verifiedReceiptPersistsOriginalMinorUnitPrecision(){transactions.settle("payment",new ProviderPayment("payment","provider-transaction","USD",BigInteger.valueOf(1400),true));verify(db).update(contains("payment_minor,payment_minor_digits,base_minor"),any(),eq("payment"),eq("merchant"),eq("provider-transaction"),eq("USD"),eq(BigInteger.valueOf(1400)),eq(2),eq(new BigDecimal("10000")));}
  @Test void wrongCurrencyWrongReferenceAndWrongAmountCannotCreditTheWallet(){
    assertThrows(IllegalArgumentException.class,()->transactions.settle("payment",new ProviderPayment("payment","transaction","CNY",BigInteger.valueOf(1400),true)));
    assertThrows(IllegalArgumentException.class,()->transactions.settle("payment",new ProviderPayment("other","transaction","USD",BigInteger.valueOf(1400),true)));
    assertThrows(IllegalArgumentException.class,()->transactions.settle("payment",new ProviderPayment("payment","transaction","USD",BigInteger.valueOf(1399),true)));
    verifyNoInteractions(money);
  }
  @Test void unconfirmedProviderStatusIsNotReceipt(){
    assertFalse(transactions.settle("payment",new ProviderPayment("payment",null,"USD",BigInteger.valueOf(1400),false)));
    verifyNoInteractions(money);verify(db,never()).update(startsWith("INSERT INTO t_currency_payment_receipts"),any(Object[].class));
  }
  @Test void nonCnyLedgerAmountCannotBeCreditedAsCny(){
    payment.put("base_currency","USD");
    assertThrows(IllegalArgumentException.class,()->transactions.settle("payment",new ProviderPayment("payment","transaction","USD",BigInteger.valueOf(1400),true)));verifyNoInteractions(money);
  }
}
