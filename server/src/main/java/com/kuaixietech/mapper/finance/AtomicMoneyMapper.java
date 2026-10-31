package com.kuaixietech.mapper.finance;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface AtomicMoneyMapper {
  BigDecimal lockBalance(@Param("uid") String uid);
  int setBalance(@Param("uid") String uid, @Param("amount") BigDecimal amount);
  int addLedger(@Param("id") String id, @Param("uid") String uid, @Param("kind") String kind,
    @Param("delta") BigDecimal delta,@Param("after") BigDecimal after,
    @Param("orderId") String orderId,@Param("note") String note);
}
