package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.mapper.order_fulfillment.InsuranceMigrationMapper;
import com.kuaixietech.security.TrustedActor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Original 20261021_224107 set_order_insurance function. */
@Service
public class InsuranceMigrationService {
  private final InsuranceMigrationMapper mapper;
  public InsuranceMigrationService(InsuranceMigrationMapper mapper) {this.mapper=mapper;}
  public static BigDecimal premium(String choice,BigDecimal declaredValue) {
    if(choice==null || !Set.of("yes","waived").contains(choice)) throw new IllegalArgumentException("ERR:BAD_CHOICE");
    if(declaredValue==null || declaredValue.signum()<0 || declaredValue.compareTo(new BigDecimal("1000000"))>0)
      throw new IllegalArgumentException("ERR:BAD_VALUE");
    if(choice.equals("waived"))return BigDecimal.ZERO.setScale(2);
    return declaredValue.multiply(new BigDecimal("0.01")).setScale(2,RoundingMode.HALF_UP)
      .max(new BigDecimal("5.00"));
  }
  @Transactional
  public String setOrderInsurance(String orderId,String choice,BigDecimal declaredValue) {
    // Authenticate independently of request payload; never accept arbitrary actorId from the HTTP body.
    String actor=TrustedActor.currentUserId();
    Map<String,Object> order=mapper.lockOrder(orderId);
    if(order==null)return "ERR:NO_ORDER";
    if(!actor.equals(String.valueOf(order.get("buyer_id"))))return "ERR:NOT_BUYER";
    if(!Set.of("requesting","negotiating","vendor_deposit_pending").contains(String.valueOf(order.get("status"))))return "ERR:BAD_STATUS";
    final BigDecimal premium;
    try {premium=premium(choice,declaredValue);}
    catch(IllegalArgumentException e){return e.getMessage();}
    int updated=mapper.updateInsurance(orderId,choice,premium,declaredValue);
    if(updated!=1)throw new IllegalStateException("Order changed during insurance update");
    String content=choice.equals("yes")
      ? "买家已为本单投保：申报货值 ¥"+declaredValue.setScale(2,RoundingMode.HALF_UP)+"，保费 ¥"+premium+"（结算时随服务费扣除）"
      : "买家确认放弃保价：高价值商品运输风险自担，纠纷理赔时不适用保价优先规则";
    mapper.insertSystemMessage(UUID.randomUUID().toString(),orderId,actor,content);
    return "OK";
  }
}
