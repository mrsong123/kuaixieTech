package com.kuaixie.market;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentReceiptIdentityTest {
 @Test void requiresStableNonemptyIdentity() {
  assertTrue(PaymentReceiptIdentity.valid("wechat", "merchant", "tx-1", "order-1"));
  assertFalse(PaymentReceiptIdentity.valid("wechat", "merchant", " ", "order-1"));
  assertFalse(PaymentReceiptIdentity.valid("wechat", "merchant", "tx-1", null));
  assertFalse(PaymentReceiptIdentity.valid("x".repeat(33), "merchant", "tx-1", "order-1"));
 }
 @Test void replayMustHaveIdenticalBinding() {
  var row=Map.<String,Object>of("provider","wechat", "merchant_id","m1",
    "provider_transaction_id","tx1", "merchant_order_no","o1");
  assertTrue(PaymentReceiptIdentity.sameBinding(row,"wechat","m1","tx1","o1"));
  assertFalse(PaymentReceiptIdentity.sameBinding(row,"wechat","m2","tx1","o1"));
  assertFalse(PaymentReceiptIdentity.sameBinding(row,"wechat","m1","tx1","o2"));
 }
}
