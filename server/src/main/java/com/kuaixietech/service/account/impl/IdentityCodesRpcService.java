package com.kuaixietech.service.account.impl;
import com.kuaixietech.mapper.account.IdentityCodesRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Ports verify_contact_code and consume_invite_code, no arbitrary user_id accepted from client. */
@Service public class IdentityCodesRpcService {
 private final IdentityCodesRpcMapper m;public IdentityCodesRpcService(IdentityCodesRpcMapper m){this.m=m;}
 @Transactional public boolean verifyContactCode(String target,String channel,String code){
  String actor=TrustedActor.currentUserId();if(target==null||code==null||!("phone".equals(channel)||"email".equals(channel)))return false;
  Map<String,Object> row=m.lockValidCode(target,channel,code);if(row==null)return false;
  if(m.markCodeUsed(String.valueOf(row.get("id")))!=1)throw new IllegalStateException("Concurrent code consumption");
  int updated="phone".equals(channel)?m.updateVerifiedPhone(actor,target):m.updateVerifiedEmail(actor,target);
  if(updated!=1)throw new IllegalStateException("No profile exists for verified actor");
  return true;
 }
 @Transactional public boolean consumeInviteCode(String code){
  String actor=TrustedActor.currentUserId();if(code==null||code.isBlank())return false;
  return m.consumeInvite(code.trim(),actor)==1;
 }
}
