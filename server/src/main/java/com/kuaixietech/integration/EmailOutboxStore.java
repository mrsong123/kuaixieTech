package com.kuaixietech.integration;

import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailOutboxStore {
    private final JdbcTemplate db;

    public EmailOutboxStore(JdbcTemplate db) {
        this.db = db;
    }

    /** Claim and commit before network IO. An interrupted attempt never re-enters pending automatically. */
    @Transactional
    public Map<String, Object> claim() {
        db.update("UPDATE t_vc_delivery_outbox SET code='' WHERE channel='email' AND status='blocked' AND code<>'' AND created_at<=TIMESTAMPADD(MINUTE,-10,CURRENT_TIMESTAMP(3))");
        db.update("UPDATE t_vc_delivery_outbox SET status='failed',error='CODE_EXPIRED',code='' WHERE channel='email' AND status='pending' AND created_at<=TIMESTAMPADD(MINUTE,-9,CURRENT_TIMESTAMP(3))");
        var rows = db.queryForList("SELECT id,target,code,GREATEST(1,FLOOR(TIMESTAMPDIFF(SECOND,CURRENT_TIMESTAMP(3),TIMESTAMPADD(MINUTE,10,created_at))/60)) AS remaining_minutes FROM t_vc_delivery_outbox WHERE channel='email' AND status='pending' AND created_at>TIMESTAMPADD(MINUTE,-9,CURRENT_TIMESTAMP(3)) ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED");
        if (rows.isEmpty()) return null;
        var row = rows.getFirst();
        if (db.update("UPDATE t_vc_delivery_outbox SET status='blocked',error='EMAIL_DISPATCH_UNCONFIRMED' WHERE id=? AND channel='email' AND status='pending'", row.get("id")) != 1) {
            throw new IllegalStateException("EMAIL_CLAIM_CHANGED");
        }
        return row;
    }

    @Transactional
    public void result(Object id, String status, String reference) {
        if (!Set.of("sent", "failed").contains(status) || !EmailProtocols.safeReference(reference)) {
            throw new IllegalArgumentException("EMAIL_RESULT_REJECTED");
        }
        String code = (status.equals("sent") ? "PROVIDER_ACCEPTED:" : "PROVIDER_REJECTED:") + reference;
        if (db.update("UPDATE t_vc_delivery_outbox SET status=?,error=?,code='' WHERE id=? AND channel='email' AND status='blocked' AND error='EMAIL_DISPATCH_UNCONFIRMED'", status, code, id) != 1) {
            throw new IllegalStateException("EMAIL_CLAIM_CHANGED");
        }
    }

    @Transactional
    public void invalid(Object id) {
        if (db.update("UPDATE t_vc_delivery_outbox SET status='failed',error='EMAIL_INPUT_REJECTED',code='' WHERE id=? AND channel='email' AND status='blocked' AND error='EMAIL_DISPATCH_UNCONFIRMED'", id) != 1) {
            throw new IllegalStateException("EMAIL_CLAIM_CHANGED");
        }
    }
}
