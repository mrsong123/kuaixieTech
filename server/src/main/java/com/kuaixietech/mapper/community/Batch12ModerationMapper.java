package com.kuaixietech.mapper.community;
import java.util.Map;import org.apache.ibatis.annotations.*;
@Mapper public interface Batch12ModerationMapper {
 int recentSplitCount(@Param("actor") String actor);
 int violation(@Param("id") String id,@Param("actor") String actor,@Param("code") String code,@Param("snippet") String snippet);
 Map<String,Object> lockPost(@Param("id") String id);
 int newComment(@Param("id") String id,@Param("post") String post,@Param("actor") String actor,@Param("content") String content);
 int updateCommentCount(@Param("id") String id);
 int notification(@Param("id") String id,@Param("user") String user,@Param("body") String body);
}
