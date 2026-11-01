package com.kuaixietech.mapper.order_fulfillment;
import org.apache.ibatis.annotations.*;
import java.util.*;
import java.math.BigDecimal;
@Mapper public interface ShippingNegotiationMapper {
 Map<String,Object> lockOrder(@Param("id")String id);
 int proposeFee(@Param("id")String id,@Param("fee")BigDecimal fee,@Param("note")String note);
 int acceptFee(@Param("id")String id);
 int holdFee(@Param("id")String id,@Param("fee")BigDecimal fee);
 int growBuyerDeposit(@Param("id")String id,@Param("buyer")String buyer,@Param("fee")BigDecimal fee);
 int counterFee(@Param("id")String id,@Param("fee")BigDecimal fee);
 int message(@Param("id")String id,@Param("orderId")String orderId,@Param("sender")String sender,@Param("content")String content);
 int notice(@Param("id")String id,@Param("user")String user,@Param("orderId")String orderId,@Param("body")String body,@Param("title")String title);
}
