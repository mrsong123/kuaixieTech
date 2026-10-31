package com.kuaixietech.service.account.impl;
import com.kuaixietech.mapper.account.LegacyAccountClosureRpcMapper;
import com.kuaixietech.security.TrustedActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** delete_my_account; soft-delete and notification deletion, not physical user deletion. */
@Service
public class LegacyAccountClosureRpcService {
 private final LegacyAccountClosureRpcMapper mapper;
 public LegacyAccountClosureRpcService(LegacyAccountClosureRpcMapper mapper){this.mapper=mapper;}
 @Transactional public String deleteMyAccount(){
   String id=TrustedActor.currentUserId();
   if(mapper.lockAccount(id)==null)return "ERR:NO_AUTH";
   int active=mapper.activeOrders(id);
   if(active>0)return "ERR:HAS_UNFINISHED:"+active;
   mapper.softDelete(id);mapper.clearNotices(id);return "OK";
 }
}
