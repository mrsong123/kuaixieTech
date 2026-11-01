package com.kuaixietech.integration;

import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.kuaixietech.scheduler.ContactDeliveryDiagnostic;
import com.kuaixietech.scheduler.ContactDeliveryAttempt;

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

    private volatile ContactDeliveryDiagnostic configurationStatus=ContactDeliveryDiagnostic.state("email","not_checked","configuration","CONTACT_NOT_CHECKED");
    public ContactDeliveryDiagnostic configurationStatus(){return configurationStatus;}
    public boolean configured() {
        try {
            if(couplerMode!=null)couplerMode.requireLiveNewCall("email_dispatch");
            var fields=configuration.runtime("aliyun","directmail");
            if(fields!=null)protocols.validateConfiguration(fields);
            boolean managed=fields!=null||configuration.exists("aliyun","directmail");
            configurationStatus=ContactDeliveryDiagnostic.state("email",fields!=null?"ready":managed?"paused":"idle","configuration",fields!=null?"CONTACT_PROVIDER_CONFIGURED":managed?"CONTACT_CONFIGURATION_DISABLED_OR_UNSELECTED":"EMAIL_PROVIDER_NOT_CONFIGURED");
            return fields!=null;
        }catch(RuntimeException e){configurationStatus=ContactDeliveryDiagnostic.failure("email","configuration",e);if(configurationStatus.paused())return false;throw e;}
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
                if(!receipt.accepted())throw new ContactDeliveryAttempt("EMAIL_PROVIDER_REJECTED",null);
            } catch (ContactDeliveryAttempt e) { throw e;
            } catch (IllegalArgumentException e) {
                store.invalid(row.get("id"));
                throw new ContactDeliveryAttempt("EMAIL_INPUT_REJECTED",e);
            } catch (RuntimeException e) {
                // Keep the original blocked attempt, and expose uncertainty without leaking its target or OTP.
                throw new ContactDeliveryAttempt("EMAIL_DISPATCH_UNCONFIRMED",e);
            }
        }
    }
}
