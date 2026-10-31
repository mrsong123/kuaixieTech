package com.kuaixietech.identity;

import java.util.Locale;

/** Pure validation and identity normalization retained from the last PostgreSQL functions. */
public final class ContactAndPasswordRules {
    private ContactAndPasswordRules(){}
    public static boolean validPassword(String password){
        return password!=null&&password.trim().length()>=8&&password.trim().length()<=20;
    }
    public static boolean validChannel(String channel){return "phone".equals(channel)||"email".equals(channel);}
    public static String normalize(String channel,String target){
        if(!validChannel(channel)||target==null)return null;
        String normalized="email".equals(channel)?target.trim().toLowerCase(Locale.ROOT):target;
        if("phone".equals(channel))return normalizePhone(normalized,"+86");
        return normalized.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")?normalized:null;
    }
    /** Dial code stored in t_profiles.phone_country_code. Blank means the mainland default. */
    public static String normalizeCountry(String country){
        if(country==null||country.isBlank())return "+86";
        String value=country.trim();
        return value.matches("\\+[1-9][0-9]{0,3}")?value:null;
    }
    /** National number only. The country code is never concatenated into t_profiles.phone. */
    public static String normalizePhone(String national,String country){
        if(national==null||normalizeCountry(country)==null)return null;
        String digits=national.replaceAll("\\s+","");
        if("+86".equals(normalizeCountry(country)))return digits.matches("1[3-9][0-9]{9}")?digits:null;
        return digits.matches("[0-9]{5,15}")?digits:null;
    }
    /** Mainland numbers stay on Chinese; every other dial code uses English. */
    public static String localeForCountry(String country){
        return "+86".equals(normalizeCountry(country))?"zh-CN":"en-US";
    }
    /** Verification-code row key. Kept apart from the national number so a login code cannot satisfy KYC. */
    public static String codeTarget(String channel,String normalized,String country){
        if("phone".equals(channel))return "auth:phone:"+normalizeCountry(country)+":"+normalized;
        return "auth:email:"+normalized;
    }
    public static String loginIdentifier(String channel,String normalized){
        return "phone".equals(channel)?normalized+"@phone.meoo.local":normalized;
    }
}
