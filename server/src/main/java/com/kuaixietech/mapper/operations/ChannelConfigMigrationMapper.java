package com.kuaixietech.mapper.operations;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;
import java.util.List;
@Mapper public interface ChannelConfigMigrationMapper {
 Integer isAdmin(@Param("actor") String actor);
 List<Map<String,Object>> lockByType(@Param("type") String type);
 Map<String,Object> lockConfig(@Param("id") String id);
 int disableOther(@Param("type") String type,@Param("provider") String provider);
 int disableOtherById(@Param("type") String type,@Param("id") String id);
 int upsert(@Param("id") String id,@Param("type") String type,@Param("provider") String provider,@Param("config") String config,@Param("enabled") boolean enabled,@Param("remark") String remark,@Param("actor") String actor);
 int toggle(@Param("id") String id,@Param("enabled") boolean enabled,@Param("actor") String actor);
 int delete(@Param("id") String id);
 List<Map<String,Object>> allConfigs();
 int log(@Param("id") String id,@Param("actor") String actor,@Param("action") String action,@Param("type") String type,@Param("target") String target,@Param("detail") String detail);
}
