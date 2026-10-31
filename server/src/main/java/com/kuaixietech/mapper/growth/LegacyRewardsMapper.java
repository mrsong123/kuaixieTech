package com.kuaixietech.mapper.growth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
@Mapper public interface LegacyRewardsMapper {
 String getOwnInviteCode(@Param("userId") String userId);
 int assignInviteCode(@Param("userId") String userId,@Param("code") String code);
 String findInviter(@Param("code") String code);
 String invitedBy(@Param("userId") String userId);
 int bindInviter(@Param("userId") String userId,@Param("inviter") String inviter);
 int deductPoints(@Param("userId") String userId,@Param("points") int points);
 int insertCoupon(@Param("id") String id,@Param("userId") String userId,@Param("face") BigDecimal face,@Param("points") int points);
 BigDecimal getUsableCouponValue(@Param("id") String id,@Param("userId") String userId);
 int markCouponUsed(@Param("id") String id,@Param("userId") String userId,@Param("orderId") String orderId);
 int notifyInviter(@Param("id") String id,@Param("inviter") String inviter);
}
