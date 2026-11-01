package com.kuaixietech.insurance;
import java.net.URI;
import java.util.*;
public final class InsuranceUrlPolicy {
 private InsuranceUrlPolicy(){}
 public static String require(String value,Set<String> hosts){
  URI uri=URI.create(value);
  if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getFragment()!=null||uri.getPort()!=-1&&uri.getPort()!=443||!hosts.contains(uri.getHost().toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("INSURANCE_URL_REJECTED");
  return value;
 }
}
