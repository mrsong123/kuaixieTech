package com.kuaixietech.mapper.operations;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface AdminReviewRpcMapper {
 List<Map<String,Object>> violations(@Param("limit")int limit);
 int reviewViolation(@Param("id")String id);
 List<Map<String,Object>> inviteTierDistribution();
 Map<String,Object> adminStats();
 Map<String,Object> lockEnterprise(@Param("id")String id);
 int updateEnterprise(@Param("id")String id,@Param("approved")boolean approved);
 int reviewEnterpriseDocs(@Param("id")String id,@Param("approved")boolean approved);
 int notification(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body);
}
