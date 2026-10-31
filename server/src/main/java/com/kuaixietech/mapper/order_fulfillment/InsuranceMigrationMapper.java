package com.kuaixietech.mapper.order_fulfillment;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface InsuranceMigrationMapper {
  Map<String,Object> lockOrder(@Param("orderId") String orderId);
  int updateInsurance(@Param("orderId") String orderId,@Param("choice") String choice,
                      @Param("premium") BigDecimal premium,@Param("declaredValue") BigDecimal declaredValue);
  int insertSystemMessage(@Param("messageId") String messageId,@Param("orderId") String orderId,
                          @Param("senderId") String senderId,@Param("content") String content);
}
