package com.kuaixietech.scheduler;
import com.kuaixietech.service.order_fulfillment.impl.LegacyShippingRpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/** Replaces notify-stalled-shipments (hourly). Disabled by default; only one scheduler replica should own the job. */
@Component
@ConditionalOnProperty(name="suixinggou.jobs.notify-stalled-shipments-enabled",havingValue="true")
public class StalledShipmentScheduler {
  private final LegacyShippingRpcService service;
  public StalledShipmentScheduler(LegacyShippingRpcService service){this.service=service;}
  @Scheduled(cron="0 0 * * * *",zone="UTC")
  public void execute(){service.notifyStalledShipments();}
}
