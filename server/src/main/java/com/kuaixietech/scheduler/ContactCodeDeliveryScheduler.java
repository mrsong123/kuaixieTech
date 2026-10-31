package com.kuaixietech.scheduler;

import com.kuaixietech.integration.LegacyCloudFunction;
import com.kuaixietech.integration.LegacyCloudFunctionBridge;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Port of pg_cron deliver-contact-codes: every minute POST
 * {"source":"cron"} to the configurable send-contact-code cloud adapter.
 * Disabled until a real delivery endpoint and credentials are configured.
 */
@Component
@ConditionalOnProperty(prefix="suixinggou.jobs", name="deliver-contact-codes-enabled", havingValue="true")
public class ContactCodeDeliveryScheduler {
    private final LegacyCloudFunctionBridge gateway;
    public ContactCodeDeliveryScheduler(LegacyCloudFunctionBridge gateway) {
        this.gateway = gateway;
    }
    @Scheduled(cron="0 * * * * *", zone="Asia/Shanghai")
    public void run() {
        gateway.invoke(LegacyCloudFunction.SEND_CONTACT_CODE, Map.of("source", "cron"));
    }
}
