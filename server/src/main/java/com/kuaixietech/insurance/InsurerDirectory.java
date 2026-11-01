package com.kuaixietech.insurance;
import java.util.*;
/** Reference names only. Inclusion does not imply a commercial relationship or API support. */
public final class InsurerDirectory {
 private InsurerDirectory(){}
 public static List<Map<String,String>> entries(){return List.of(
  row("picc","中国人民财产保险股份有限公司"),row("pingan","中国平安财产保险股份有限公司"),
  row("cpic","中国太平洋财产保险股份有限公司"),row("ccic","中国大地财产保险股份有限公司"),
  row("zhongan","众安在线财产保险股份有限公司"),row("sunshine","阳光财产保险股份有限公司"),
  row("taiping","太平财产保险有限公司"),row("china-life-pc","中国人寿财产保险股份有限公司"),
  row("jd-allianz","京东安联财产保险有限公司"),row("axa-tianping","安盛天平财产保险有限公司"),
  row("aig-china","美亚财产保险有限公司"),row("zurich-china","苏黎世财产保险（中国）有限公司"));}
 private static Map<String,String> row(String code,String name){return Map.of("code",code,"name",name,"status","reference_only");}
}
