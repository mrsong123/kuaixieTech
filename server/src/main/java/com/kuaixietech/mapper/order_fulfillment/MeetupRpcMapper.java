package com.kuaixietech.mapper.order_fulfillment;
import org.apache.ibatis.annotations.*;
import java.util.*;
import java.math.*;
@Mapper public interface MeetupRpcMapper {
 Map<String,Object> lockOrder(@Param("id")String id);
 int issueCode(@Param("id")String id,@Param("code")String code);
 int confirm(@Param("id")String id,@Param("buyer")String buyer,@Param("lat")BigDecimal lat,@Param("lng")BigDecimal lng,@Param("city")String city,@Param("cityGap")boolean cityGap);
 int message(@Param("id")String id,@Param("orderId")String orderId,@Param("sender")String sender,@Param("content")String content);
 int notice(@Param("id")String id,@Param("uid")String uid,@Param("orderId")String orderId,@Param("kind")String kind,@Param("title")String title,@Param("body")String body);
}
