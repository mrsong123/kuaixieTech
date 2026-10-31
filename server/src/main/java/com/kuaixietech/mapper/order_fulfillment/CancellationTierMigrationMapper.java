package com.kuaixietech.mapper.order_fulfillment;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface CancellationTierMigrationMapper {
  Map<String,Object> orderTierFacts(@Param("orderId") String orderId);
}
