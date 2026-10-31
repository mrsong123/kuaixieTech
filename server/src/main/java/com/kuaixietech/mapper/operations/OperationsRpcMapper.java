package com.kuaixietech.mapper.operations;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface OperationsRpcMapper {
 int grantPoints(@Param("uid")String uid,@Param("points")int points);
 int rewardLedger(@Param("id")String id,@Param("uid")String uid,@Param("points")int points,@Param("note")String note);
 int notice(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body,@Param("orderId")String orderId);
 Map<String,Object> lockTrip(@Param("id")String id);
 int saveArrival(@Param("id")String id,@Param("url")String url,@Param("city")String city);
 int notifyArrival(@Param("tripId")String tripId,@Param("city")String city);
 List<Map<String,Object>> expireTrips();
 List<Map<String,Object>> expireOrders(@Param("tripId")String tripId);
 int cancelExpiredOrder(@Param("orderId")String orderId);
 int expirePool(@Param("tripId")String tripId);
}
