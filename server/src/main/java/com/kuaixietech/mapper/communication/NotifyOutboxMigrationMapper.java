package com.kuaixietech.mapper.communication;
import java.util.*;
import org.apache.ibatis.annotations.*;
@Mapper public interface NotifyOutboxMigrationMapper {
 List<Map<String,Object>> pendingInapp();
 List<Map<String,Object>> pendingExternal();
 int insertInapp(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body);
 int markSent(@Param("id")String id);
 int readyProvider(@Param("channel")String channel,@Param("mapped")String mapped);
 int block(@Param("id")String id);
}
