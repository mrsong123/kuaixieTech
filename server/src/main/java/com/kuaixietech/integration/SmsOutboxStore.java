package com.kuaixietech.integration;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SmsOutboxStore {
 private final JdbcTemplate db;
 public SmsOutboxStore(JdbcTemplate db){this.db=db;}
 /** Commit claim before calling the provider. A crash leaves blocked/unconfirmed; it is never automatically resent. */
 @Transactional public Map<String,Object> claim(){db.update("UPDATE t_vc_delivery_outbox SET code='' WHERE status='blocked' AND code<>'' AND created_at<=TIMESTAMPADD(MINUTE,-10,CURRENT_TIMESTAMP(3))");db.update("UPDATE t_vc_delivery_outbox SET status='failed',error='CODE_EXPIRED',code='' WHERE channel='phone' AND status='pending' AND created_at<=TIMESTAMPADD(MINUTE,-9,CURRENT_TIMESTAMP(3))");var rows=db.queryForList("SELECT id,target,code FROM t_vc_delivery_outbox WHERE channel='phone' AND status='pending' AND created_at>TIMESTAMPADD(MINUTE,-9,CURRENT_TIMESTAMP(3)) ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED");if(rows.isEmpty())return null;var row=rows.getFirst();if(db.update("UPDATE t_vc_delivery_outbox SET status='blocked',error='SMS_DISPATCH_UNCONFIRMED' WHERE id=? AND status='pending'",row.get("id"))!=1)throw new IllegalStateException("SMS_CLAIM_CHANGED");return row;}
 @Transactional public void result(Object id,String status,String safeCode){if(!Set.of("sent","failed","blocked").contains(status)||!safeCode.matches("[A-Za-z0-9_:.-]{1,200}"))throw new IllegalArgumentException("SMS_RESULT_INVALID");if(db.update("UPDATE t_vc_delivery_outbox SET status=?,error=?,code=CASE WHEN ? IN('sent','failed') THEN '' ELSE code END WHERE id=? AND status='blocked' AND error='SMS_DISPATCH_UNCONFIRMED'",status,safeCode,status,id)!=1)throw new IllegalStateException("SMS_CLAIM_CHANGED");}
}
