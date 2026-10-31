package com.kuaixietech.mapper.order_fulfillment;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface LegacyShippingRpcMapper {
  Map<String,Object> lockOrder(@Param("orderId")String orderId);
  int addNode(@Param("id")String id,@Param("orderId")String orderId,@Param("actor")String actor,@Param("node")String node,@Param("detail")String detail);
  int addNotice(@Param("id")String id,@Param("userId")String userId,@Param("kind")String kind,@Param("title")String title,@Param("body")String body,@Param("orderId")String orderId);
  int recentNotice(@Param("userId")String userId,@Param("kind")String kind,@Param("orderId")String orderId);
  List<Map<String,Object>> stalledOrders();
  int isStillStalled(@Param("orderId")String orderId);
}
