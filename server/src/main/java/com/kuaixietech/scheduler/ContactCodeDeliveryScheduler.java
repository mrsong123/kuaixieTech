package com.kuaixietech.scheduler;

import com.kuaixietech.integration.EmailOutboxDispatcher;
import com.kuaixietech.integration.LegacyCloudFunction;
import com.kuaixietech.integration.LegacyCloudFunctionBridge;
import com.kuaixietech.integration.SmsOutboxDispatcher;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Independent native senders; unhealthy or paused native ownership never enables a legacy double send. */
@Component
public class ContactCodeDeliveryScheduler {
    private static final Logger LOG=LoggerFactory.getLogger(ContactCodeDeliveryScheduler.class);
    private final LegacyCloudFunctionBridge gateway;
    private final SmsOutboxDispatcher sms;
    private final EmailOutboxDispatcher email;
    private final boolean legacyEnabled;
    private volatile Snapshot last = new Snapshot(null,"not_checked",unchecked("sms"),unchecked("email"),unchecked("native_ownership"),unchecked("legacy"));
    private long runs;
    public record Snapshot(Instant checkedAt,String state,ContactDeliveryDiagnostic sms,ContactDeliveryDiagnostic email,
                           ContactDeliveryDiagnostic nativeOwnership,ContactDeliveryDiagnostic legacy) {}

    @Autowired
    public ContactCodeDeliveryScheduler(LegacyCloudFunctionBridge gateway, SmsOutboxDispatcher sms,
        EmailOutboxDispatcher email,
        @Value("${suixinggou.jobs.deliver-contact-codes-enabled:false}") boolean legacyEnabled) {
        this.gateway=gateway;this.sms=sms;this.email=email;this.legacyEnabled=legacyEnabled;
    }
    public ContactCodeDeliveryScheduler(LegacyCloudFunctionBridge gateway,SmsOutboxDispatcher sms,boolean legacyEnabled) {
        this(gateway,sms,null,legacyEnabled);
    }
    private static ContactDeliveryDiagnostic unchecked(String channel){return ContactDeliveryDiagnostic.state(channel,"not_checked","configuration","CONTACT_NOT_CHECKED");}
    public Snapshot snapshot(){return last;}

    @Scheduled(cron="0 * * * * *",zone="Asia/Shanghai")
    public synchronized void run() {
        boolean managed=false;
        var ownership=ContactDeliveryDiagnostic.state("native_ownership","idle","configuration","CONTACT_NO_NATIVE_OWNERSHIP");
        try {managed=email!=null&&email.nativeManaged();ownership=ContactDeliveryDiagnostic.state("native_ownership",managed?"ready":"idle","configuration",managed?"CONTACT_NATIVE_OWNS_QUEUE":"CONTACT_NO_NATIVE_OWNERSHIP");}
        catch(RuntimeException e){ownership=ContactDeliveryDiagnostic.failure("native_ownership","configuration",e);}
        var smsState=probe("sms",sms::configured,sms::configurationStatus);
        var emailState=email==null?ContactDeliveryDiagnostic.state("email","disabled","configuration","EMAIL_COMPONENT_NOT_PRESENT"):
            probe("email",email::configured,email::configurationStatus);
        if(smsState.state().equals("ready"))smsState=dispatch("sms",sms::run);
        if(emailState.state().equals("ready"))emailState=dispatch("email",email::run);
        var legacy=ContactDeliveryDiagnostic.state("legacy","disabled","dispatch",legacyEnabled?"CONTACT_LEGACY_NOT_SELECTED":"CONTACT_LEGACY_DISABLED");
        // A read failure or deliberate SANDBOX/feature pause is not permission to use an old whole-queue sender.
        boolean nativeFailure=ownership.failed()||smsState.failed()||emailState.failed();
        if(legacyEnabled&&!managed&&!nativeFailure&&!smsState.paused()&&!emailState.paused()
            &&smsState.state().equals("idle")&&(emailState.state().equals("idle")||emailState.state().equals("disabled"))) {
            legacy=dispatch("legacy",()->gateway.invoke(LegacyCloudFunction.SEND_CONTACT_CODE,Map.of("source","cron")));
        }
        String state=nativeFailure||legacy.failed()?"degraded":smsState.state().equals("ready")||emailState.state().equals("ready")||legacy.state().equals("ready")?"ready":smsState.paused()||emailState.paused()?"paused":"idle";
        var next=new Snapshot(Instant.now(),state,smsState,emailState,ownership,legacy);
        runs++;report(last.sms(),smsState);report(last.email(),emailState);report(last.nativeOwnership(),ownership);report(last.legacy(),legacy);last=next;
    }
    private ContactDeliveryDiagnostic probe(String channel,BooleanSupplier configured,Supplier<ContactDeliveryDiagnostic> detail) {
        try {
            boolean ready=configured.getAsBoolean();
            var status=detail.get();
            // Direct-construction test and compatibility dispatchers may not expose a stored probe.
            return status!=null&&!status.state().equals("not_checked")?status:
                ContactDeliveryDiagnostic.state(channel,ready?"ready":"idle","configuration",ready?"CONTACT_PROVIDER_CONFIGURED":"CONTACT_PROVIDER_NOT_CONFIGURED");
        }catch(RuntimeException e){return ContactDeliveryDiagnostic.failure(channel,"configuration",e);}
    }
    private ContactDeliveryDiagnostic dispatch(String channel,Runnable work) {
        try {work.run();return ContactDeliveryDiagnostic.state(channel,"ready","dispatch","CONTACT_POLL_COMPLETED");}
        catch(RuntimeException e){return ContactDeliveryDiagnostic.failure(channel,"dispatch",e);}
    }
    private void report(ContactDeliveryDiagnostic previous,ContactDeliveryDiagnostic status) {
        boolean changed=!previous.state().equals(status.state())||!previous.reasonCode().equals(status.reasonCode())||!Objects.equals(previous.missingTable(),status.missingTable())||!Objects.equals(previous.sqlState(),status.sqlState())||!Objects.equals(previous.databaseError(),status.databaseError());
        if(!changed&&(!status.failed()||runs%10!=0))return;
        if(status.failed())LOG.error("contact_delivery channel={} state={} stage={} code={} type={} sqlState={} databaseError={} missingTable={}; consult /portal/admin/contact-delivery/health; no automatic replay of blocked attempts",
            status.channel(),status.state(),status.stage(),status.reasonCode(),status.exceptionType(),status.sqlState(),status.databaseError(),status.missingTable());
        else LOG.info("contact_delivery channel={} state={} stage={} code={}",status.channel(),status.state(),status.stage(),status.reasonCode());
    }
    /** Probe configuration only. This admin read must never claim or dispatch a verification code. */
    public Map<String,ContactDeliveryDiagnostic> configuration(){
        return Map.of("sms",probe("sms",sms::configured,sms::configurationStatus),"email",email==null?
            ContactDeliveryDiagnostic.state("email","disabled","configuration","EMAIL_COMPONENT_NOT_PRESENT"):
            probe("email",email::configured,email::configurationStatus));
    }
}
