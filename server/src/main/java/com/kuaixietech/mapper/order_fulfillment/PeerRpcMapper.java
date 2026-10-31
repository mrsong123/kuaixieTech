package com.kuaixietech.mapper.order_fulfillment;
import java.util.Map;
import org.apache.ibatis.annotations.*;
@Mapper public interface PeerRpcMapper {
 Map<String,Object> lockOrder(@Param("orderId")String orderId);
 Map<String,Object> peer(@Param("uid")String uid);
 Map<String,Object> existingThread(@Param("orderId")String orderId,@Param("uid")String uid,@Param("peer")String peer);
 int reopenThread(@Param("id")String id);
 int createThread(@Param("id")String id,@Param("orderId")String orderId,@Param("uid")String uid,@Param("peer")String peer);
 int related(@Param("actor")String actor,@Param("target")String target);
}
