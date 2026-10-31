package com.kuaixie.market;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PaymentReceiptGuardTest {
 private Map<String,Object> order(){var m=new HashMap<String,Object>();m.put("status","pending_payment");m.put("payment_status","pending_provider");m.put("store_id","enterprise-store");m.put("order_no","ORDER-17");m.put("total_amount","10.50");return m;}
 @Test void requiresExactOrderAmountCurrencyAndState(){
  var o=order();assertTrue(PaymentReceiptGuard.matches(o,"ORDER-17",1050,"CNY"));
  assertFalse(PaymentReceiptGuard.matches(o,"ORDER-OTHER",1050,"CNY"));
  assertFalse(PaymentReceiptGuard.matches(o,"ORDER-17",1051,"CNY"));
  assertFalse(PaymentReceiptGuard.matches(o,"ORDER-17",1050,"USD"));
  o.put("payment_status","paid");assertFalse(PaymentReceiptGuard.matches(o,"ORDER-17",1050,"CNY"));
  o.put("payment_status","pending_provider");o.put("demand_id","matching");assertFalse(PaymentReceiptGuard.matches(o,"ORDER-17",1050,"CNY"));
  o.remove("demand_id");o.put("total_amount","10.505");assertFalse(PaymentReceiptGuard.matches(o,"ORDER-17",1050,"CNY"));
 }
}
