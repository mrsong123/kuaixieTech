package com.kuaixietech.service.platform.impl;
import com.kuaixietech.mapper.platform.AnnouncementMigrationMapper;
import com.kuaixietech.service.platform.AnnouncementMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;
@Service public class AnnouncementMigrationServiceImpl implements AnnouncementMigrationService {
 private final AnnouncementMigrationMapper mapper;
 public AnnouncementMigrationServiceImpl(AnnouncementMigrationMapper mapper){this.mapper=mapper;}
 private static String id(){return UUID.randomUUID().toString();}
 private boolean admin(String id){return id!=null && mapper.isAdmin(id)>0;}
 @Override @Transactional public String publish(String admin,String title,String body,String kind,Boolean pinned){
   if(!admin(admin))return "ERR:NO_PERM";
   if(title==null || title.trim().length()<2)return "ERR:BAD_TITLE";
   if(body==null || body.trim().length()<5)return "ERR:BAD_BODY";
   if(kind==null || !Set.of("notice","activity","rule").contains(kind))return "ERR:BAD_KIND";
   mapper.publish(id(),admin,title.trim(),body.trim(),kind,Boolean.TRUE.equals(pinned));
   mapper.log(id(),admin,"announcement_published","",title);
   return "OK";
 }
 @Override @Transactional public String toggle(String admin,String announcementId,Boolean active){
   if(!admin(admin))return "ERR:NO_PERM";
   if(mapper.exists(announcementId)==0)return "ERR:NO_ROW";
   mapper.toggle(announcementId,Boolean.TRUE.equals(active));
   mapper.log(id(),admin,Boolean.TRUE.equals(active)?"announcement_enabled":"announcement_disabled",announcementId,"");
   return "OK";
 }
}
