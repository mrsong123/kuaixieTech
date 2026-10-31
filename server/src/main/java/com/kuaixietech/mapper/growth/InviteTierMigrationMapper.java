package com.kuaixietech.mapper.growth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.Map;
@Mapper public interface InviteTierMigrationMapper {
 Integer validCount(@Param("uid") String uid);
 int updateTier(@Param("uid") String uid,@Param("count") int count,@Param("tier") String tier);
 Map<String,Object> profileSummary(@Param("uid") String uid);
 Map<String,Object> rewardTotals(@Param("uid") String uid);
}
