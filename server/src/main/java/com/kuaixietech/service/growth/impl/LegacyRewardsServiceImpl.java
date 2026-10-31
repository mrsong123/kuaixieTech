package com.kuaixietech.service.growth.impl;
import com.kuaixietech.mapper.growth.LegacyRewardsMapper;
import com.kuaixietech.service.growth.LegacyRewardsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;
@Service public class LegacyRewardsServiceImpl implements LegacyRewardsService {
 private final LegacyRewardsMapper mapper;
 private final SecureRandom random = new SecureRandom();
 public LegacyRewardsServiceImpl(LegacyRewardsMapper mapper){this.mapper=mapper;}
 @Override @Transactional public String generateInviteCode(String userId){
   String current=mapper.getOwnInviteCode(userId); if(current!=null)return current;
   for(int attempt=0;attempt<8;attempt++){
     String alphabet="0123456789ABCDEF";StringBuilder b=new StringBuilder("DD");
     for(int j=0;j<6;j++)b.append(alphabet.charAt(random.nextInt(alphabet.length())));
     String code=b.toString();
     try {if(mapper.assignInviteCode(userId,code)==1)return code;
          current=mapper.getOwnInviteCode(userId);if(current!=null)return current;
          throw new IllegalArgumentException("用户不存在");}
     catch(DuplicateKeyException collision){ /* retry random code */ }
   }
   throw new IllegalStateException("生成邀请码失败，请重试");
 }
 @Override @Transactional public boolean bindInviter(String userId,String code){
   String inviter=mapper.findInviter(code);if(inviter==null||inviter.equals(userId))return false;
   if(mapper.invitedBy(userId)!=null)return false;
   String walk=inviter;for(int depth=0;walk!=null&&depth<10;depth++){
     if(walk.equals(userId))return false;walk=mapper.invitedBy(walk);
   }
   if(mapper.bindInviter(userId,inviter)!=1)return false;
   mapper.notifyInviter(UUID.randomUUID().toString(),inviter);return true;
 }
 @Override @Transactional public String exchangeCoupon(String userId,int points){
   if(points<100||points%10!=0)throw new IllegalArgumentException("兑换需 100 分起、10 分一档");
   if(mapper.deductPoints(userId,points)!=1)throw new IllegalStateException("积分不足");
   String id=UUID.randomUUID().toString();BigDecimal face=BigDecimal.valueOf(points).movePointLeft(1).setScale(2);
   mapper.insertCoupon(id,userId,face,points);return id;
 }
 @Override @Transactional public BigDecimal useCoupon(String couponId,String userId,String orderId){
   BigDecimal face=mapper.getUsableCouponValue(couponId,userId);
   if(face==null||mapper.markCouponUsed(couponId,userId,orderId)!=1)throw new IllegalStateException("代金券不可用或已过期");
   return face;
 }
}
