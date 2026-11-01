package com.kuaixietech.mapper.order_fulfillment;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface OrderProofMigrationMapper {
 java.util.List<String> activeCases(@Param("id") String id);
 java.util.List<String> activeAfterSales(@Param("id") String id);
 Map<String,Object> lockOrder(@Param("id")String id);
 Map<String,Object> trip(@Param("id")String id);
 Map<String,Object> lockTrip(@Param("id")String id);
 Map<String,Object> latestFace(@Param("actor")String actor,@Param("orderId")String orderId,@Param("scene")String scene);
 Map<String,Object> exactFace(@Param("id")String id,@Param("actor")String actor,@Param("orderId")String orderId);
 int activeGoodsInsurance(@Param("id")String id);
 int savePurchase(@Param("id")String id,@Param("evidence")String evidence,@Param("meta")String meta,@Param("images")String images,@Param("lat")BigDecimal lat,@Param("lng")BigDecimal lng,@Param("city")String city,@Param("addr")String addr,@Param("face")String face,@Param("flags")String flags,@Param("tax")BigDecimal tax,@Param("mismatch")boolean mismatch);
 int saveShipment(@Param("id")String id,@Param("carrier")String carrier,@Param("tracking")String tracking,@Param("labels")String labels,@Param("lat")BigDecimal lat,@Param("lng")BigDecimal lng,@Param("city")String city,@Param("addr")String addr,@Param("flags")String flags);
 int saveBoarding(@Param("id")String id,@Param("url")String url,@Param("lat")BigDecimal lat,@Param("lng")BigDecimal lng,@Param("city")String city,@Param("flags")String flags);
 int saveDelivery(@Param("id")String id,@Param("face")String face,@Param("lat")BigDecimal lat,@Param("lng")BigDecimal lng,@Param("city")String city);
 int cancelNoBoarding(@Param("id")String id);
 int refundDepositRows(@Param("id")String id);
 int addNotice(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body,@Param("orderId")String orderId);
 int addMessage(@Param("id")String id,@Param("orderId")String orderId,@Param("actor")String actor,@Param("content")String content);
}
