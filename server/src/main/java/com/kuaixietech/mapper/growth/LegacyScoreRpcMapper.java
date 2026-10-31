package com.kuaixietech.mapper.growth;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface LegacyScoreRpcMapper {
 Integer lockScore(@Param("userId") String userId);
 int writeScore(@Param("userId") String userId,@Param("score")int score);
 int insertScoreLog(@Param("id")String id,@Param("userId")String userId,@Param("orderId")String orderId,@Param("event")String event,@Param("delta")int delta,@Param("after")int after,@Param("note")String note);
 int writeBoostFactor(@Param("userId")String userId,@Param("factor")BigDecimal factor);
}
