package com.kuaixietech.service.growth;
import java.math.BigDecimal;
public interface LegacyRewardsService {
 String generateInviteCode(String userId);
 boolean bindInviter(String userId, String code);
 String exchangeCoupon(String userId, int points);
 BigDecimal useCoupon(String couponId, String userId, String orderId);
}
