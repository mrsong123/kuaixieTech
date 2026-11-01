package com.kuaixietech.payments;

import static org.junit.jupiter.api.Assertions.*;
import java.math.*;
import java.security.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class PaymentDomainTest {
  @Test void currencyCatalogIsNotAHandwrittenShortlist(){
    assertTrue(CurrencyAmounts.catalog().size()>100);
    assertEquals(BigInteger.valueOf(123),CurrencyAmounts.minor("123","JPY"));
    assertEquals(BigInteger.valueOf(1234),CurrencyAmounts.minor("1.234","BHD"));
    assertEquals("1.234",CurrencyAmounts.major(BigInteger.valueOf(1234),"BHD"));
    assertEquals(BigInteger.valueOf(123),CurrencyAmounts.minor("123","KRW"));
    assertThrows(ArithmeticException.class,()->CurrencyAmounts.minor("1.01","JPY"));
    assertThrows(IllegalArgumentException.class,()->CurrencyAmounts.currency("XXX"));
  }
  @Test void conversionUsesExactMinorUnitsAndExplicitSpread(){
    assertEquals(BigInteger.valueOf(1400),CurrencyAmounts.convert(BigInteger.valueOf(10000),"CNY","USD",new BigDecimal("0.14"),0));
    assertEquals(BigInteger.valueOf(1414),CurrencyAmounts.convert(BigInteger.valueOf(10000),"CNY","USD",new BigDecimal("0.14"),100));
    assertEquals(BigInteger.valueOf(2100),CurrencyAmounts.convert(BigInteger.valueOf(10000),"CNY","JPY",new BigDecimal("21"),0));
    assertEquals(BigInteger.valueOf(5250),CurrencyAmounts.convert(BigInteger.valueOf(10000),"CNY","BHD",new BigDecimal("0.0525"),0));
    assertThrows(IllegalArgumentException.class,()->CurrencyAmounts.convert(BigInteger.TEN,"CNY","USD",BigDecimal.ZERO,0));
  }
  @Test void nativeProviderSignaturesRejectAnyChangedReceipt() throws Exception {
    var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);var pair=generator.generateKeyPair();
    String raw="{\"amount\":100,\"currency\":\"CNY\"}";
    String signature=PaymentSecrets.sign(pair.getPrivate(),raw);
    assertTrue(PaymentSecrets.verify(pair.getPublic(),raw,signature));
    assertFalse(PaymentSecrets.verify(pair.getPublic(),raw.replace("100","101"),signature));
    assertFalse(PaymentSecrets.verify(pair.getPublic(),raw,"invalid"));
  }
  @Test void alipayVerificationPreservesTheExactSignedJsonObject(){
    String raw="{ \"code\":\"10000\", \"text\":\"brace } and \\\" quote\",\"child\":{\"n\":1}}";
    String response="{\"alipay_trade_query_response\":"+raw+",\"sign\":\"example\"}";
    assertEquals(raw,AlipayPageAdapter.responseObject(response,"alipay_trade_query_response"));
    assertThrows(IllegalArgumentException.class,()->AlipayPageAdapter.responseObject("{}","missing"));
  }
}
