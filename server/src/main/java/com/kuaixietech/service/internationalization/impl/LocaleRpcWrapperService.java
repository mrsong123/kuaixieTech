package com.kuaixietech.service.internationalization.impl;
import com.kuaixietech.service.internationalization.LocaleMigrationService;

import com.kuaixietech.mapper.account.ReadAccessMapper;
import com.kuaixietech.security.TrustedActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Ports admin_locale_{upsert,set_active,remove}_rpc security wrappers and is_admin_cached. */
@Service public class LocaleRpcWrapperService {
 private final LocaleMigrationService locales;private final ReadAccessMapper access;
 public LocaleRpcWrapperService(LocaleMigrationService locales,ReadAccessMapper access){this.locales=locales;this.access=access;}
 @Transactional(readOnly=true) public boolean isAdminCached(){return access.isAdmin(TrustedActor.currentUserId());}
 @Transactional public String upsert(String code,String nativeName,String labelZh,Integer sort,String fallback){
  String uid=TrustedActor.currentUserId();if(!access.isAdmin(uid))return "ERR:NOT_ADMIN";
  return locales.upsert(uid,code,nativeName,labelZh,sort,fallback);
 }
 @Transactional public String setActive(String code,boolean active,String note){
  String uid=TrustedActor.currentUserId();if(!access.isAdmin(uid))return "ERR:NOT_ADMIN";
  return locales.setActive(uid,code,active,note);
 }
 @Transactional public String remove(String code,String note){
  String uid=TrustedActor.currentUserId();if(!access.isAdmin(uid))return "ERR:NOT_ADMIN";
  return locales.remove(uid,code,note);
 }
}
