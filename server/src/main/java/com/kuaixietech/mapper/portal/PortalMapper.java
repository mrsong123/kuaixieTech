package com.kuaixietech.mapper.portal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
@Mapper public interface PortalMapper {
 List<Map<String,Object>> publicDemands(@Param("limit") int limit);
 List<Map<String,Object>> communityFeed(@Param("limit") int limit);
 Map<String,Object> profileRole(@Param("userId") String userId);
 List<Map<String,Object>> pendingDemands();
 List<Map<String,Object>> pendingTrips();
 List<Map<String,Object>> pendingPosts();
}
