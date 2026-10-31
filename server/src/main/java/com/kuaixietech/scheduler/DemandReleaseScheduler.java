package com.kuaixietech.scheduler;
import com.kuaixietech.service.trading.impl.DemandReleaseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(prefix="suixinggou.jobs", name="reveal-due-demands-enabled", havingValue="true")
public class DemandReleaseScheduler {
    private final DemandReleaseService service;
    public DemandReleaseScheduler(DemandReleaseService service) { this.service=service; }
    @Scheduled(cron="0 */5 * * * *")
    public void execute() { service.revealDueDemands(); }
}
