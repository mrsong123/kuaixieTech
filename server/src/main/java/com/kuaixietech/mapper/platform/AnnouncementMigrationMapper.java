package com.kuaixietech.mapper.platform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface AnnouncementMigrationMapper {
 Integer isAdmin(@Param("admin") String admin);
 int publish(@Param("id") String id,@Param("admin") String admin,@Param("title") String title,@Param("body") String body,@Param("kind") String kind,@Param("pinned") boolean pinned);
 Integer exists(@Param("id") String id);
 int toggle(@Param("id") String id,@Param("active") boolean active);
 int log(@Param("id") String id,@Param("admin") String admin,@Param("action") String action,@Param("target") String target,@Param("detail") String detail);
}
