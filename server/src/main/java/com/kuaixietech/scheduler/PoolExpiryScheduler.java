package com.kuaixietech.scheduler;
import com.kuaixietech.service.operations.impl.OperationsRpcService;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
/** Legacy run_expire_unformed_pools cron: every ten minutes, disabled until deployment verification. */
@Component @ConditionalOnProperty(name="suixinggou.jobs.expire-unformed-pools-enabled",havingValue="true")
public class PoolExpiryScheduler {
 private final OperationsRpcService service;
 public PoolExpiryScheduler(OperationsRpcService service){this.service=service;}
 @Scheduled(cron="0 */10 * * * *",zone="Asia/Shanghai") public void run(){service.expireUnformedPools();}
}
