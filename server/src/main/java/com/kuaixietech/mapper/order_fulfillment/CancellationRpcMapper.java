package com.kuaixietech.mapper.order_fulfillment;
import java.math.BigDecimal;
import java.util.*;
import org.apache.ibatis.annotations.*;
@Mapper public interface CancellationRpcMapper {
 java.util.List<String> activeCases(@Param("id") String id);
 java.util.List<String> activeAfterSales(@Param("id") String id);
 Map<String,Object> lockOrder(@Param("id")String id);
 Map<String,Object> latestPending(@Param("id")String id);
 int markCancelled(@Param("id")String id,@Param("status")String status,@Param("tier")String tier,@Param("reason")String reason,@Param("force")boolean force,@Param("proof")String proof,@Param("buyerStatus")String buyerStatus,@Param("vendorStatus")String vendorStatus,@Param("penalty")BigDecimal penalty,@Param("closedReason")String closedReason);
 List<Map<String,Object>> frozenDepositRows(@Param("id")String id);
 int finalizeDepositRows(@Param("id")String id,@Param("buyerStatus")String buyerStatus,@Param("vendorStatus")String vendorStatus,@Param("reason")String reason);
 int markDepositRows(@Param("id")String id,@Param("reason")String reason);
 int reduceScore(@Param("uid")String uid,@Param("delta")int delta,@Param("breach")boolean breach);
 int saveDecision(@Param("id")String id,@Param("approve")boolean approve,@Param("reason")String reason,@Param("op")String op);
 int restoreLocked(@Param("id")String id);
 int addNotice(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body,@Param("orderId")String orderId);
}
