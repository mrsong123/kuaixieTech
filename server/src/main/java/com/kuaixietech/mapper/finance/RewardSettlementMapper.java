package com.kuaixietech.mapper.finance;
import java.math.BigDecimal;
import java.util.*;
import org.apache.ibatis.annotations.*;
@Mapper public interface RewardSettlementMapper {
 java.util.List<String> activeCases(@Param("id") String id);
 java.util.List<String> activeAfterSales(@Param("id") String id);
 Map<String,Object> lockProfile(@Param("uid")String uid);
 String getInviter(@Param("uid")String uid);
 int hasFirstOrder(@Param("uid")String uid);
 int addReward(@Param("id")String id,@Param("inviter")String inviter,@Param("invitee")String invitee,@Param("orderId")String orderId,@Param("level")int level,@Param("event")String event,@Param("kind")String kind,@Param("amount")int amount,@Param("rate")BigDecimal rate,@Param("base")BigDecimal base,@Param("currency")String currency,@Param("paid")boolean paid,@Param("note")String note);
 int grantPoints(@Param("uid")String uid,@Param("points")int points);
 int notice(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body);
 int setCommissionMonth(@Param("uid")String uid,@Param("month")String month);
 int addCommissionMonth(@Param("uid")String uid,@Param("amount")BigDecimal amount);
 int alreadyCommission(@Param("orderId")String orderId,@Param("inviter")String inviter,@Param("level")int level);
 List<Map<String,Object>> lockCommissionRewards(@Param("orderId")String orderId);
 int revokeReward(@Param("id")String id,@Param("reason")String reason);
 int reduceCommission(@Param("uid")String uid,@Param("amount")BigDecimal amount);
 Map<String,Object> lockOrder(@Param("id")String id);
 int markOrderSettled(@Param("id")String id);
 int releaseDeposits(@Param("id")String id);
 int markNoProof(@Param("id")String id);
 int markTaxDegraded(@Param("id")String id,@Param("fee")BigDecimal fee);
 int setTaxRefundAmount(@Param("id")String id,@Param("tax")BigDecimal tax);
 int updateVendorStats(@Param("id")String id,@Param("onTime")boolean onTime);
 int message(@Param("id")String id,@Param("orderId")String orderId,@Param("sender")String sender,@Param("content")String content);
}
