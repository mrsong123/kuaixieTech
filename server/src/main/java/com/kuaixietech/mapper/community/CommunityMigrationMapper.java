package com.kuaixietech.mapper.community;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface CommunityMigrationMapper {
 Integer isAdmin(@Param("actor") String actor);
 String lockPost(@Param("post") String post);
 String lockPostAuthor(@Param("post") String post);
 Integer isLiked(@Param("post") String post,@Param("user") String user);
 int insertLike(@Param("post") String post,@Param("user") String user);
 int deleteLike(@Param("post") String post,@Param("user") String user);
 Integer updateLikeCount(@Param("post") String post);
 int storeLikeCount(@Param("post") String post);
 int removePost(@Param("post") String post,@Param("reason") String reason);
 int log(@Param("id") String id,@Param("admin") String admin,@Param("post") String post,@Param("reason") String reason);
 int notify(@Param("id") String id,@Param("user") String user,@Param("reason") String reason);
}
