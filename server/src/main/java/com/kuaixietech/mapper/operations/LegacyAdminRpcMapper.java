package com.kuaixietech.mapper.operations;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface LegacyAdminRpcMapper {
 List<Map<String,Object>> commissionRows(@Param("limit")int limit);
 int adminExists(@Param("uid")String uid);
 List<Map<String,Object>> lockPoolOrders(@Param("id")String id);
 int refundBuyerDeposit(@Param("id")String id,@Param("reason")String reason);
 int cancelPoolOrder(@Param("id")String id,@Param("reason")String reason);
 int expireDissolvedTrip(@Param("id")String id);
 int writeAdminLog(@Param("id")String id,@Param("actor")String actor,@Param("action")String action,@Param("type")String type,@Param("target")String target,@Param("detail")String detail);
 int broadcastByRole(@Param("title")String title,@Param("body")String body,@Param("scope")String scope);
 int broadcastSelected(@Param("title")String title,@Param("body")String body,@Param("userIds")List<String> userIds);
 Map<String,Object> lockDemand(@Param("id")String id);
 int closeDemand(@Param("id")String id);
 int rejectOpenQuotes(@Param("id")String id);
 int notifyUser(@Param("id")String id,@Param("userId")String userId,@Param("kind")String kind,@Param("title")String title,@Param("body")String body,@Param("orderId")String orderId);
 Map<String,Object> lockTrip(@Param("id")String id);
 int takenPoolSeats(@Param("id")String id);
 int markPoolFormed(@Param("id")String id);
 int notifyPoolBuyers(@Param("id")String id);
 Integer lockProfile(@Param("id")String id);
 int setBan(@Param("id")String id,@Param("ban")boolean ban);
 int countOpenOrders(@Param("id")String id);
 int pauseTrips(@Param("id")String id);
}
