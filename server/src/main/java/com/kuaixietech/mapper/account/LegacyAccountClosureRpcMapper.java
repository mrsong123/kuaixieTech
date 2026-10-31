package com.kuaixietech.mapper.account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface LegacyAccountClosureRpcMapper {
 Integer lockAccount(@Param("userId")String userId);
 int activeOrders(@Param("userId")String userId);
 int softDelete(@Param("userId")String userId);
 int clearNotices(@Param("userId")String userId);
}
