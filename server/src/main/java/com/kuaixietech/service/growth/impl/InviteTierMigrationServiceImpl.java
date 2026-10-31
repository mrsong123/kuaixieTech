package com.kuaixietech.service.growth.impl;
import com.kuaixietech.mapper.growth.InviteTierMigrationMapper;
import com.kuaixietech.service.growth.InviteTierMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
@Service public class InviteTierMigrationServiceImpl implements InviteTierMigrationService {
 private final InviteTierMigrationMapper mapper;
 public InviteTierMigrationServiceImpl(InviteTierMigrationMapper mapper){this.mapper=mapper;}
 @Override @Transactional public String refreshInviteTier(String uid){
  if(uid==null)return "l0";
  int count=mapper.validCount(uid);
  String tier=count>=80?"l4":count>=30?"l3":count>=10?"l2":count>=3?"l1":"l0";
  mapper.updateTier(uid,count,tier);
  return tier;
 }
 @Override @Transactional public Map<String,Object> getMyInviteSummary(String authenticatedUserId){
  if(authenticatedUserId==null)return Map.of("err","NO_AUTH");
  refreshInviteTier(authenticatedUserId);
  Map<String,Object> p=mapper.profileSummary(authenticatedUserId);
  if(p==null)throw new IllegalArgumentException("用户不存在");
  int count=((Number)p.get("valid_count")).intValue();
  int needed=count<3?3-count:count<10?10-count:count<30?30-count:count<80?80-count:0;
  Map<String,Object> sums=mapper.rewardTotals(authenticatedUserId);
  Map<String,Object> result=new LinkedHashMap<>();
  result.put("tier",p.get("tier"));result.put("valid_count",count);
  result.put("commission_mtd",p.get("commission_mtd"));result.put("next_needed",needed);
  result.put("total_cny",sums.get("total_cny"));result.put("total_points",sums.get("total_points"));
  return result;
 }
}
