package com.kuaixietech.mapper.account;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ReadAccessMapper {
  boolean isActiveProfile(@Param("userId") String userId);
  boolean isAdmin(@Param("userId") String userId);
  boolean isVerifiedCarrier(@Param("userId") String userId);
  boolean canReadOrder(@Param("orderId") String orderId, @Param("userId") String userId);
}
