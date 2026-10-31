package com.kuaixietech.mapper.communication;
import java.util.Map;import org.apache.ibatis.annotations.*;
@Mapper public interface Batch12CallMapper {
 Map<String,Object> lockOrder(@Param("id") String id);
 Map<String,Object> lockCall(@Param("id") String id);
 int expireRinging(@Param("order") String order);
 int activeRinging(@Param("order") String order);
 int createCall(@Param("id") String id,@Param("order") String order,@Param("caller") String caller,@Param("callee") String callee,@Param("room") String room);
 int accept(@Param("id") String id);
 int reject(@Param("id") String id,@Param("status") String status);
 int hangup(@Param("id") String id,@Param("duration") long duration);
 int notifyPeer(@Param("id") String id,@Param("peer") String peer,@Param("order") String order,@Param("kind") String kind,@Param("title") String title,@Param("body") String body);
 int systemMessage(@Param("id") String id,@Param("order") String order,@Param("sender") String sender,@Param("duration") long duration);
}
