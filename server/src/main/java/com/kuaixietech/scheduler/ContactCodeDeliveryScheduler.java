package com.kuaixietech.scheduler;

import com.kuaixietech.integration.EmailOutboxDispatcher;
import com.kuaixietech.integration.LegacyCloudFunction;
import com.kuaixietech.integration.LegacyCloudFunctionBridge;
import com.kuaixietech.integration.SmsOutboxDispatcher;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Native channels are independent; the old whole-queue bridge must never race a native dispatcher. */
@Component
public class ContactCodeDeliveryScheduler {
    private final LegacyCloudFunctionBridge gateway;
    private final SmsOutboxDispatcher sms;
    private final EmailOutboxDispatcher email;
    private final boolean legacyEnabled;

    @Autowired
    public ContactCodeDeliveryScheduler(LegacyCloudFunctionBridge gateway, SmsOutboxDispatcher sms,
        EmailOutboxDispatcher email,
        @Value("${suixinggou.jobs.deliver-contact-codes-enabled:false}") boolean legacyEnabled) {
        this.gateway = gateway;
        this.sms = sms;
        this.email = email;
        this.legacyEnabled = legacyEnabled;
    }

    /** Retain existing direct-construction callers; production injection always supplies both channels. */
    public ContactCodeDeliveryScheduler(LegacyCloudFunctionBridge gateway, SmsOutboxDispatcher sms, boolean legacyEnabled) {
        this(gateway, sms, null, legacyEnabled);
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Shanghai")
    public void run() {
        boolean smsReady = false, emailReady = false, managed = false, uncertain = false;
        try { managed = email != null && email.nativeManaged(); } catch (RuntimeException e) { uncertain = true; }
        try { smsReady = sms.configured(); } catch (RuntimeException e) { uncertain = true; }
        try { emailReady = email != null && email.configured(); } catch (RuntimeException e) { uncertain = true; }
        if (smsReady) {
            try { sms.run(); } catch (RuntimeException e) { uncertain = true; }
        }
        if (emailReady) {
            try { email.run(); } catch (RuntimeException e) { uncertain = true; }
        }
        // An unreadable native configuration is not permission to fall back to another sender.
        if (!smsReady && !emailReady && !managed && !uncertain && legacyEnabled) {
            gateway.invoke(LegacyCloudFunction.SEND_CONTACT_CODE, Map.of("source", "cron"));
        }
        if (uncertain) throw new IllegalStateException("CONTACT_DELIVERY_PARTIALLY_UNAVAILABLE");
    }
}
