package com.kuaixietech.mapper.trip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;
@Mapper public interface PoolGroupMigrationMapper {
 Map<String,Object> lockTrip(@Param("id") String id);
 Integer existingSlot(@Param("tripId") String tripId,@Param("userId") String userId);
 int occupied(@Param("tripId") String tripId);
 int lockedCount(@Param("tripId") String tripId);
 int setForming(@Param("id") String id);
 int setFormed(@Param("id") String id);
 int notifyFormed(@Param("id") String id);
}
