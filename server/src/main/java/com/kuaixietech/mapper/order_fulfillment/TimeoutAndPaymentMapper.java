package com.kuaixietech.mapper.order_fulfillment;
import java.util.*;
import org.apache.ibatis.annotations.*;
@Mapper public interface TimeoutAndPaymentMapper {
 List<Map<String,Object>> expiredIntentOrders(@Param("limit")int limit);
 List<Map<String,Object>> expiredQuotes(@Param("limit")int limit);
 int closeOrder(@Param("id")String id,@Param("oldStatus")String oldStatus,@Param("reason")String reason);
 int releaseOrderDeposits(@Param("id")String id);
 int expireShippingFees();
 List<Map<String,Object>> overduePkDemands(@Param("limit")int limit);
 int savePkResult(@Param("id")String id,@Param("result")String result);
 List<Map<String,Object>> closeOverdueDemands(@Param("limit")int limit);
 int closeDemand(@Param("id")String id);
 Map<String,Object> lockRecharge(@Param("tradeNo")String tradeNo);
 int markRecharge(@Param("id")String id,@Param("transactionId")String transactionId);
 int notifyUser(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body,@Param("orderId")String orderId);
}
