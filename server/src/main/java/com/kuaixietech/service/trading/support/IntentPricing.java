package com.kuaixietech.service.trading.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Shared policy for new intent and PK orders. Values are yuan, rounded once per component. */
public final class IntentPricing {
  public static final String VERSION="escrow_v1";
  public static final String EXTERNAL_VERSION="escrow_v2_external_insurance";
  public static boolean isEscrowVersion(Object version){return VERSION.equals(version)||EXTERNAL_VERSION.equals(version);}
  private IntentPricing(){}
  /** New orders never collect a platform premium; the chosen insurer quotes and collects directly. */
  public static Quote externalQuote(BigDecimal goods,String choice){
    Quote base=quote(goods,"waived");
    if(!"yes".equals(choice)&&!"waived".equals(choice))throw new IllegalArgumentException("INSURANCE_CHOICE_REQUIRED");
    return new Quote(base.goodsAmount(),base.serviceFee(),new BigDecimal("0.00"),base.buyerEscrow(),base.vendorDeposit(),"CNY",EXTERNAL_VERSION);
  }
  public record Quote(BigDecimal goodsAmount,BigDecimal serviceFee,BigDecimal insurancePremium,
                      BigDecimal buyerEscrow,BigDecimal vendorDeposit,String currency,String pricingVersion){}
  public static Quote quote(BigDecimal goods,String insuranceChoice){
    if(goods==null||goods.signum()<=0||goods.compareTo(new BigDecimal("1000000"))>0)
      throw new IllegalArgumentException("INVALID_GOODS_AMOUNT");
    if(!"yes".equals(insuranceChoice)&&!"waived".equals(insuranceChoice))
      throw new IllegalArgumentException("INSURANCE_CHOICE_REQUIRED");
    goods=goods.setScale(2,RoundingMode.HALF_UP);
    BigDecimal fee=goods.multiply(new BigDecimal("0.05")).setScale(2,RoundingMode.HALF_UP);
    BigDecimal premium="yes".equals(insuranceChoice)
      ?goods.multiply(new BigDecimal("0.01")).setScale(2,RoundingMode.HALF_UP).max(new BigDecimal("5.00"))
      :new BigDecimal("0.00");
    BigDecimal vendor=goods.multiply(new BigDecimal("0.20")).setScale(2,RoundingMode.HALF_UP);
    return new Quote(goods,fee,premium,goods.add(fee).add(premium),vendor,"CNY",VERSION);
  }
}
