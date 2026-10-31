package com.kuaixietech.service.growth.impl;
import com.kuaixietech.mapper.growth.InviteReadRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
/** Exact column projections and row-level rules of team invite RPCs from 20261021_224442. */
@Service
public class InviteReadRpcService {
  private final InviteReadRpcMapper mapper;
  public InviteReadRpcService(InviteReadRpcMapper mapper){this.mapper=mapper;}
  public List<Map<String,Object>> listTeamMembers(String root){
    String actor=TrustedActor.currentUserId();
    if(!actor.equals(root)&&mapper.isAdmin(actor)==0)return List.of();
    return mapper.teamMembers(root);
  }
  public String getInviteCodeOwner(String code){
    return mapper.inviteCodeOwner(code,TrustedActor.currentUserId());
  }
  public String getOperatorInviteCode(String operatorId){
    return mapper.operatorInviteCode(operatorId,TrustedActor.currentUserId());
  }
}
