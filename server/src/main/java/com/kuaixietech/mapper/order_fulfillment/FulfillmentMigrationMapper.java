package com.kuaixietech.mapper.order_fulfillment;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface FulfillmentMigrationMapper {
  Map<String,Object> lockProduct(@Param("id") String id);
  int decrementProductStock(@Param("id") String id,@Param("qty") int qty);
  Map<String,Object> orderParties(@Param("orderId") String orderId);
  int insertLogistics(@Param("id") String id,@Param("orderId") String orderId,
     @Param("operatorId") String operatorId,@Param("node") String node,
     @Param("operatorRole") String operatorRole,@Param("detail") String detail);
  int insertBuyerNotification(@Param("id") String id,@Param("buyerId") String buyerId,
     @Param("orderId") String orderId,@Param("body") String body);
}
