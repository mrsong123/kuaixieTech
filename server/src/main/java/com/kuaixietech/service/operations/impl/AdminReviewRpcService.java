package com.kuaixietech.service.operations.impl;
import com.kuaixietech.mapper.operations.AdminReviewRpcMapper;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Ports admin_list_violations, admin_review_violation, admin_stats, admin_review_enterprise. */
@Service public class AdminReviewRpcService {
 private final AdminReviewRpcMapper m; private final DomainAccessGuard guard;private final LegacyAdminRpcService admin;
 public AdminReviewRpcService(AdminReviewRpcMapper m,DomainAccessGuard guard,LegacyAdminRpcService admin){this.m=m;this.guard=guard;this.admin=admin;}
 @Transactional(readOnly=true) public List<Map<String,Object>> violations(Integer limit){guard.requireAdmin();return m.violations(limit==null?60:Math.max(limit,1));}
 @Transactional public String reviewViolation(String id){String actor=guard.requireAdmin();if(m.reviewViolation(id)==0)return "ERR:NO_ROW";admin.adminGuard("review_violation","chat_violation",id,"已复核引流违规记录");return "OK";}
 @Transactional(readOnly=true) public List<Map<String,Object>> inviteTierDistribution(){guard.requireAdmin();return m.inviteTierDistribution();}
 @Transactional(readOnly=true) public Map<String,Object> stats(){guard.requireAdmin();return m.adminStats();}
 @Transactional public String reviewEnterprise(String uid,boolean approve,String note){guard.requireAdmin();Map<String,Object> p=m.lockEnterprise(uid);if(p==null)return "ERR:NO_USER";if(!"enterprise".equals(p.get("user_role")))return "ERR:NOT_ENTERPRISE";if(!"pending".equals(p.get("enterprise_verify_status")))return "ERR:BAD_STATUS";if(note==null||note.isBlank())return "ERR:NO_NOTE";m.updateEnterprise(uid,approve);m.reviewEnterpriseDocs(uid,approve);m.notification(UUID.randomUUID().toString(),uid,"verify",approve?"企业认证已通过":"企业认证未通过",approve?"企业主体「"+p.get("enterprise_name")+"」认证通过，可正常发布求购与下单。":"认证未通过："+note+"。可修正后重新提交。");admin.adminGuard("review_enterprise","profile",uid,(approve?"通过：":"驳回：")+note);return "OK";}
}
