package com.kuaixietech.service.internationalization;
public interface LocaleMigrationService {
 String upsert(String authenticatedAdminId,String code,String nativeName,String labelZh,Integer sort,String fallback);
 String setActive(String authenticatedAdminId,String code,boolean active,String note);
 String remove(String authenticatedAdminId,String code,String note);
}
