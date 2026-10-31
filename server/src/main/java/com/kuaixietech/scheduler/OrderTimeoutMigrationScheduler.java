package com.kuaixietech.scheduler;
import com.kuaixietech.service.order_fulfillment.impl.TimeoutAndPaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
/** Disabled until verified MySQL deployment; schedule matches the original pg_cron every 10 minutes. */
@Component @ConditionalOnProperty(name="migration.order-timeouts.enabled",havingValue="true")
public class OrderTimeoutMigrationScheduler {
 private final TimeoutAndPaymentService service;
 public OrderTimeoutMigrationScheduler(TimeoutAndPaymentService service){this.service=service;}
 @Scheduled(cron="0 */10 * * * *",zone="Asia/Shanghai") public void run(){service.handleOrderTimeouts();}
}
