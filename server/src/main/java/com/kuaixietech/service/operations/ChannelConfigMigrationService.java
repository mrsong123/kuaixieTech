package com.kuaixietech.service.operations;
import java.util.Map;
public interface ChannelConfigMigrationService {
 String upsert(String authenticatedAdminId,String type,String provider,String configJson,boolean enabled,String remark);
 String toggle(String authenticatedAdminId,String configId,boolean enabled);
 String delete(String authenticatedAdminId,String configId);
 Map<String,Object> summary(String authenticatedAdminId);
}
