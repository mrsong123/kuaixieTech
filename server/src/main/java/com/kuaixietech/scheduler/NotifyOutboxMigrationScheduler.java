package com.kuaixietech.scheduler;
import com.kuaixietech.service.communication.impl.NotifyOutboxMigrationService;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
/** Original flush-notify-outbox cron every five minutes; disabled until DB/provider integration tests. */
@Component @ConditionalOnProperty(name="migration.notify-outbox.enabled",havingValue="true")
public class NotifyOutboxMigrationScheduler {
 private final NotifyOutboxMigrationService service;
 public NotifyOutboxMigrationScheduler(NotifyOutboxMigrationService service){this.service=service;}
 @Scheduled(cron="0 */5 * * * *",zone="Asia/Shanghai") public void run(){service.flushNotifyOutbox();}
}
