package com.kuaixietech.mapper.trading;

import com.kuaixietech.entity.portal.IntentInput;
import com.kuaixietech.service.trading.support.IntentPricing;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IntentOrderMapper {
  Map<String,Object> trip(@Param("id") String id);
  Map<String,Object> lockTrip(@Param("id") String id);
  int reservedQuantityTotal(@Param("trip") String trip);
  Map<String,Object> lockExisting(@Param("id") String id);
  int categoryAllowed(@Param("trip") String trip,@Param("category") String category);
  int insertOrder(@Param("id") String id,@Param("orderNo") String orderNo,@Param("buyer") String buyer,
                  @Param("vendor") String vendor,@Param("input") IntentInput input,@Param("price") IntentPricing.Quote price,
                  @Param("snapshot") String snapshot,@Param("intentHours") int intentHours);
  int insertDeposit(@Param("id") String id,@Param("order") String order,@Param("buyer") String buyer,@Param("amount") java.math.BigDecimal amount);
}
