package com.kuaixietech.service.internationalization.impl;
import com.kuaixietech.mapper.internationalization.LocaleMigrationMapper;
import com.kuaixietech.service.internationalization.LocaleMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service public class LocaleMigrationServiceImpl implements LocaleMigrationService {
 private final LocaleMigrationMapper mapper;
 public LocaleMigrationServiceImpl(LocaleMigrationMapper mapper){this.mapper=mapper;}
 private boolean admin(String actor){return actor!=null&&mapper.isAdmin(actor)>0;}
 private String log(String admin,String action,String target,String detail){mapper.log(UUID.randomUUID().toString(),admin,action,target,detail);return "OK";}
 @Override @Transactional public String upsert(String admin,String code,String name,String label,Integer sort,String fallback){
  if(!admin(admin))return "ERR:NOT_ADMIN";
  if(code==null||!code.matches("^[a-z]{2}(-[A-Z]{2})?$"))return "ERR:BAD_CODE";
  if(name==null||name.isEmpty())return "ERR:NO_NAME";
  mapper.upsert(code,name,label==null?"":label,sort==null?100:sort,fallback==null?"zh":fallback);
  log(admin,"locale_upsert",code,"语种维护："+name);return "OK:LOCALE:"+code;
 }
 @Override @Transactional public String setActive(String admin,String code,boolean active,String note){
  if(!admin(admin))return "ERR:NOT_ADMIN";
  if("zh".equals(code)&&!active)return "ERR:CANNOT_DISABLE_ZH";
  if(note==null||note.isEmpty())return "ERR:NO_NOTE";
  if(mapper.exists(code)==0)return "ERR:NO_SUCH_LOCALE";
  mapper.toggle(code,active);log(admin,"locale_set_active",code,note);
  return "OK:ACTIVE:"+code+":"+active;
 }
 @Override @Transactional public String remove(String admin,String code,String note){
  if(!admin(admin))return "ERR:NOT_ADMIN";
  if("zh".equals(code)||"en".equals(code))return "ERR:PROTECTED";
  if(note==null||note.isEmpty())return "ERR:NO_NOTE";
  if(mapper.delete(code)==0)return "ERR:NO_SUCH_LOCALE";
  log(admin,"locale_remove",code,note);return "OK:REMOVED:"+code;
 }
}
