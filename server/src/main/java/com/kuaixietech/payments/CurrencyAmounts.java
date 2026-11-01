package com.kuaixietech.payments;

import java.math.*;
import java.util.*;

/** ISO currency precision; all wire amounts are decimal strings, never binary floating point. */
public final class CurrencyAmounts {
  private CurrencyAmounts(){}
  public static Currency currency(String code){
    if(code==null||!code.matches("[A-Z]{3}"))throw new IllegalArgumentException("INVALID_CURRENCY");
    Currency currency=Currency.getInstance(code);
    if(currency.getDefaultFractionDigits()<0)throw new IllegalArgumentException("NON_MONETARY_CURRENCY");
    return currency;
  }
  public static BigInteger minor(String amount,String code){
    BigDecimal value=new BigDecimal(amount);
    if(value.signum()<0)throw new IllegalArgumentException("NEGATIVE_AMOUNT");
    return value.movePointRight(currency(code).getDefaultFractionDigits()).toBigIntegerExact();
  }
  public static String major(BigInteger amount,String code){
    if(amount.signum()<0)throw new IllegalArgumentException("NEGATIVE_AMOUNT");
    return new BigDecimal(amount,currency(code).getDefaultFractionDigits()).toPlainString();
  }
  public static BigInteger convert(BigInteger sourceMinor,String source,String target,BigDecimal rate,int spreadBps){
    if(sourceMinor.signum()<=0||rate.signum()<=0||spreadBps<0||spreadBps>2000)throw new IllegalArgumentException("INVALID_QUOTE");
    BigDecimal targetMajor=new BigDecimal(sourceMinor,currency(source).getDefaultFractionDigits())
      .multiply(rate).multiply(BigDecimal.ONE.add(BigDecimal.valueOf(spreadBps,4)));
    return targetMajor.movePointRight(currency(target).getDefaultFractionDigits()).setScale(0,RoundingMode.HALF_UP).toBigIntegerExact();
  }
  public static List<Map<String,Object>> catalog(){
    return Currency.getAvailableCurrencies().stream().filter(c->c.getDefaultFractionDigits()>=0)
      .sorted(Comparator.comparing(Currency::getCurrencyCode)).map(c->{Map<String,Object> out=new LinkedHashMap<>();
        out.put("code",c.getCurrencyCode());out.put("minorDigits",c.getDefaultFractionDigits());
        out.put("name",c.getDisplayName(Locale.SIMPLIFIED_CHINESE));out.put("nameEn",c.getDisplayName(Locale.ENGLISH));return out;}).toList();
  }
}
