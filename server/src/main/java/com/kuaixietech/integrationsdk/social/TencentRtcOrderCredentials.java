package com.kuaixietech.integrationsdk.social;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class TencentRtcOrderCredentials {
 private final JdbcTemplate db;private final DomainAccessGuard guard;private final TencentRtcUserSig issuer;
 public TencentRtcOrderCredentials(JdbcTemplate db,DomainAccessGuard guard,TencentRtcUserSig issuer){this.db=db;this.guard=guard;this.issuer=issuer;}
 /** Caller supplies one server-resolved context; request bodies must never supply SDK keys, user IDs or room IDs. */
 public TencentRtcUserSig.Credentials forAcceptedCall(SocialServiceContext context,String callId){String user=guard.requireActiveProfile();var rows=db.queryForList("SELECT c.caller_id,c.callee_id,c.status,c.room_id,o.buyer_id,o.vendor_id,o.status AS order_status FROM t_call_records c JOIN t_orders o ON o.id=c.order_id WHERE c.id=?",callId);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"RTC_CALL_UNAVAILABLE");var call=rows.getFirst();if(!user.equals(call.get("caller_id"))&&!user.equals(call.get("callee_id")))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"RTC_CALL_UNAVAILABLE");if(!"accepted".equals(call.get("status"))||!Set.of("locked","purchased","shipped").contains(call.get("order_status"))||!user.equals(call.get("buyer_id"))&&!user.equals(call.get("vendor_id")))throw new ResponseStatusException(HttpStatus.CONFLICT,"RTC_CALL_NOT_ACTIVE");return issuer.issue(context,UUID.fromString(user).toString().replace("-",""),Objects.toString(call.get("room_id"),""));}
}
