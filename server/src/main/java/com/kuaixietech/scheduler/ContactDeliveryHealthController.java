package com.kuaixietech.scheduler;

import com.kuaixietech.security.DomainAccessGuard;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only diagnostics: never exposes an OTP, recipient, credential or raw SQL/exception message. */
@RestController
public class ContactDeliveryHealthController {
    private final DomainAccessGuard guard;
    private final ContactCodeDeliveryScheduler scheduler;
    private final JdbcTemplate db;
    public ContactDeliveryHealthController(DomainAccessGuard guard,ContactCodeDeliveryScheduler scheduler,JdbcTemplate db){
        this.guard=guard;this.scheduler=scheduler;this.db=db;
    }
    @GetMapping("/api/portal/admin/contact-delivery/health")
    public Map<String,Object> health(){
        guard.requireAdmin();
        var result=new LinkedHashMap<String,Object>();
        var config=scheduler.configuration();
        result.put("checkedAt",Instant.now());
        result.put("configuration",config);
        result.put("lastRun",scheduler.snapshot());
        result.put("dispatchConfigured",config.values().stream().anyMatch(c->c.state().equals("ready")));
        result.put("providerDeliveryVerified",false);
        result.put("sentMeaning","provider_accepted_not_recipient_delivery");
        result.put("blockedPolicy","never_automatically_replay; reconcile_with_provider_before_any_manual_action");
        String state=config.values().stream().anyMatch(ContactDeliveryDiagnostic::failed)?"degraded":
            config.values().stream().anyMatch(c->c.state().equals("ready"))?"ready":
            config.values().stream().anyMatch(ContactDeliveryDiagnostic::paused)?"paused":"idle";
        if(state.equals("ready")&&scheduler.snapshot().state().equals("degraded"))state="degraded";
        try {
            List<Map<String,Object>> counts=db.queryForList("SELECT channel,status,COUNT(*) AS total FROM t_vc_delivery_outbox WHERE channel IN('phone','email') GROUP BY channel,status ORDER BY channel,status");
            result.put("queue",counts);
            if(counts.stream().anyMatch(r->"blocked".equals(r.get("status"))&&r.get("total") instanceof Number n&&n.longValue()>0))state="requires_reconciliation";
        }catch(RuntimeException e){result.put("queue",List.of());result.put("queueDiagnostic",ContactDeliveryDiagnostic.failure("queue","inspection",e));state="degraded";}
        result.put("state",state);
        return result;
    }
}
