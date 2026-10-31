package com.kuaixietech.service.trading.support;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
/** Numeric port of close_pk ranking; ratios and weights match the final PostgreSQL expression. */
public final class PkScoring {
 private PkScoring() { }
 private static final BigDecimal ONE=BigDecimal.ONE;
 private static BigDecimal num(Object n){return n==null?BigDecimal.ZERO:new BigDecimal(n.toString());}
 private static BigDecimal ratio(BigDecimal n,BigDecimal d) {
   if(d.signum()==0)return BigDecimal.ZERO;
   return n.divide(d,12,RoundingMode.HALF_UP);
 }
 private static BigDecimal minimumOne(BigDecimal x){return x.min(ONE);}
 public static BigDecimal score(Map<String,Object> q,BigDecimal minPrice,int minDays) {
   BigDecimal price=num(q.get("price")),days=num(q.get("promise_days"));
   BigDecimal priceScore=minimumOne(ratio(minPrice,price.max(new BigDecimal("0.01"))));
   BigDecimal performance=ratio(num(q.get("performance_score")),new BigDecimal("100"));
   BigDecimal timeScore=minDays>0?minimumOne(ratio(new BigDecimal(minDays),days.max(ONE))):new BigDecimal("0.5");
   BigDecimal deals=num(q.get("deal_count"));
   BigDecimal onTime=deals.signum()>0?minimumOne(ratio(num(q.get("ontime_count")),deals)):new BigDecimal("0.5");
   return priceScore.multiply(new BigDecimal("0.45"))
     .add(performance.multiply(new BigDecimal("0.30")))
     .add(timeScore.multiply(new BigDecimal("0.15")))
     .add(onTime.multiply(new BigDecimal("0.10")))
     .setScale(4,RoundingMode.HALF_UP);
 }
 public static BigDecimal money(Object x){return num(x).setScale(2,RoundingMode.HALF_UP);}
}
