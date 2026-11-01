package com.kuaixietech.payments;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Disabled until merchant connectivity and live reconciliation are verified. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.payments.reconcile-enabled",havingValue="true")
public class PaymentReconciliation {
  private final PaymentStore store;private final PaymentService payments;
  public PaymentReconciliation(PaymentStore store,PaymentService payments){this.store=store;this.payments=payments;}
  @Scheduled(fixedDelayString="${app.payments.reconcile-delay-ms:60000}")
  public void reconcile(){
    for(var row:store.db.queryForList("SELECT * FROM t_currency_payments WHERE status IN('creating','pending') ORDER BY COALESCE(last_checked_at,created_at),id LIMIT 100"))
      try{payments.refresh(row);}catch(RuntimeException unavailable){/* Leave unpaid/unverified attempts pending for retry, never credit speculatively. */}
  }
}
