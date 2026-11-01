package com.kuaixietech.controller;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
/** Read stored tier and effective counts. A GET never recalculates grades or commission rates. */
@RestController
@RequestMapping("/api/me/invite")
public class InviteSummaryController {
 private final JdbcTemplate db;private final DomainAccessGuard guard;
 public InviteSummaryController(JdbcTemplate db,DomainAccessGuard guard){this.db=db;this.guard=guard;}
 @GetMapping("/tier") public Map<String,Object> tier(){String user=guard.requireActiveProfile();var rows=db.queryForList("SELECT p.invite_tier AS tier,p.valid_invitee_count AS valid_count,p.points,CASE WHEN p.commission_mtd_month=DATE_FORMAT(CURRENT_DATE(),'%Y-%m') THEN p.commission_mtd ELSE 0 END AS commission_mtd,(SELECT COUNT(*) FROM t_profiles child WHERE child.invited_by=p.id AND child.deleted_at IS NULL) AS direct_count FROM t_profiles p WHERE p.id=? AND p.deleted_at IS NULL",user);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"profile unavailable");return rows.getFirst();}
}
