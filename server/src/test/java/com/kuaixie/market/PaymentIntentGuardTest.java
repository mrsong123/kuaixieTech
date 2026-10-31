package com.kuaixie.market;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentIntentGuardTest {
 @Test void permitsOnlyPendingOrderWithUnpaidOrPendingProvider(){
  assertTrue(PaymentApi.eligibleForIntent(Map.of("status","pending_payment","payment_status","unpaid")));
  assertTrue(PaymentApi.eligibleForIntent(Map.of("status","pending_payment","payment_status","pending_provider")));
  for(String orderState:new String[]{"completed","cancelled","agreed_unfunded","shipped","paid"}){
   assertFalse(PaymentApi.eligibleForIntent(Map.of("status",orderState,"payment_status","unpaid")));
   assertFalse(PaymentApi.eligibleForIntent(Map.of("status",orderState,"payment_status","pending_provider")));
  }
  for(String paymentState:new String[]{"paid","refunded","failed","null"})
   assertFalse(PaymentApi.eligibleForIntent(Map.of("status","pending_payment","payment_status",paymentState)));
  assertFalse(PaymentApi.eligibleForIntent(Map.of("status","pending_payment")));
  assertFalse(PaymentApi.eligibleForIntent(null));
 }
}
