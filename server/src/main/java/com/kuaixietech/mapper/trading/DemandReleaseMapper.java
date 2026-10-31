package com.kuaixietech.mapper.trading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
@Mapper
public interface DemandReleaseMapper {
    List<Map<String,Object>> lockDueDemands(@Param("limit") int limit);
    int clearScheduledAt(@Param("id") String id);
    int insertReleaseNotification(@Param("id") String id, @Param("userId") String userId,
       @Param("title") String title, @Param("body") String body);
}
