package com.kuaixietech.service.community.impl;
import com.kuaixietech.mapper.community.CommunityMigrationMapper;
import com.kuaixietech.service.community.CommunityMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service public class CommunityMigrationServiceImpl implements CommunityMigrationService {
 private final CommunityMigrationMapper mapper;
 public CommunityMigrationServiceImpl(CommunityMigrationMapper mapper){this.mapper=mapper;}
 @Override @Transactional public String toggleLike(String actor,String post){
  if(actor==null)return "ERR:NO_LOGIN";
  // Lock the parent row to serialize like-count updates for the same post.
  if(mapper.lockPost(post)==null)return "ERR:NO_POST";
  if(mapper.isLiked(post,actor)>0)mapper.deleteLike(post,actor);else mapper.insertLike(post,actor);
  mapper.storeLikeCount(post);
  return "OK:"+mapper.updateLikeCount(post);
 }
 @Override @Transactional public String adminRemovePost(String admin,String post,String reason){
  if(admin==null||mapper.isAdmin(admin)==0)return "ERR:NO_PERM";
  if(reason==null||reason.trim().length()<2)return "ERR:NO_NOTE";
  String author=mapper.lockPostAuthor(post);if(author==null)return "ERR:NO_ROW";
  mapper.removePost(post,reason);
  mapper.log(UUID.randomUUID().toString(),admin,post,reason);
  mapper.notify(UUID.randomUUID().toString(),author,reason);
  return "OK";
 }
}
