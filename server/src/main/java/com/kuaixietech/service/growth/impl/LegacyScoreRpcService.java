package com.kuaixietech.service.growth.impl;
import com.kuaixietech.mapper.growth.LegacyScoreRpcMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Port of apply_score_delta. Caller must validate the event's business authorization. */
@Service
public class LegacyScoreRpcService {
  private final LegacyScoreRpcMapper mapper;
  public LegacyScoreRpcService(LegacyScoreRpcMapper mapper){this.mapper=mapper;}
  @Transactional
  public int applyScoreDelta(String userId,int delta,String event,String orderId,String note){
    Integer old=mapper.lockScore(userId);
    // PostgreSQL source attempted a FK-violating score log if user missing. Fail explicitly instead.
    if(old==null) throw new IllegalArgumentException("profile missing");
    int next=Math.min(100,Math.max(0,old+delta));
    mapper.writeScore(userId,next);
    mapper.insertScoreLog(UUID.randomUUID().toString(),userId,orderId,event,delta,next,note);
    BigDecimal factor=next>=90?new BigDecimal("1.60"):
        next>=80?new BigDecimal("1.35"):next>=70?new BigDecimal("1.15"):
        next<=30?new BigDecimal("0.60"):new BigDecimal("1.00");
    mapper.writeBoostFactor(userId,factor);
    return next;
  }
}
