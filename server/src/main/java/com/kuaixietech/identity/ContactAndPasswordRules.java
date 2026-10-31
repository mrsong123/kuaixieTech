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
        if("phone".equals(channel))return normalized.matches("1[3-9][0-9]{9}")?normalized:null;
        return normalized.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")?normalized:null;
    }
    public static String loginIdentifier(String channel,String normalized){
        return "phone".equals(channel)?normalized+"@phone.meoo.local":normalized;
    }
}
