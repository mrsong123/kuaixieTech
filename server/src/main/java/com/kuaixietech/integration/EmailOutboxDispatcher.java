package com.kuaixietech.integration;

import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class EmailOutboxDispatcher {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

    private final IntegrationConfiguration configuration;
    private final EmailOutboxStore store;
    private final EmailProtocols protocols;

    public EmailOutboxDispatcher(IntegrationConfiguration configuration, EmailOutboxStore store, EmailProtocols protocols) {
        this.configuration = configuration;
        this.store = store;
        this.protocols = protocols;
    }

    /** Stored native settings are an explicit migration boundary, including drafts and disabled records. */
    public boolean nativeManaged() {
        return configuration.exists("aliyun", "sms") || configuration.exists("tencent", "sms")
            || configuration.exists("aliyun", "directmail");
    }

    public boolean configured() {
        return configuration.runtime("aliyun", "directmail") != null;
    }

    public void run() {
  if(couplerMode!=null)couplerMode.requireLiveNewCall("email_dispatch");
        Map<String, String> fields = configuration.runtime("aliyun", "directmail");
        if (fields == null) throw new IllegalStateException("EMAIL_PROVIDER_NOT_CONFIGURED");
        protocols.validateConfiguration(fields);
        for (int i = 0; i < 20; i++) {
            var row = store.claim();
            if (row == null) return;
            try {
                var receipt = protocols.send(fields, Objects.toString(row.get("target"), ""),
                    Objects.toString(row.get("code"), ""), row.get("remaining_minutes") instanceof Number n ? n.intValue() : 0);
                if (!EmailProtocols.safeReference(receipt.providerReference())) {
                    throw new IllegalStateException("EMAIL_RECEIPT_UNCONFIRMED");
                }
                store.result(row.get("id"), receipt.accepted() ? "sent" : "failed", receipt.providerReference());
            } catch (IllegalArgumentException e) {
                store.invalid(row.get("id"));
            } catch (RuntimeException e) {
                // Keep the original blocked attempt on timeout, malformed acknowledgement or DB uncertainty.
                return;
            }
        }
    }
}
