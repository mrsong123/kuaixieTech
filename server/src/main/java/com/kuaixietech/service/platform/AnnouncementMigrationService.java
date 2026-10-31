package com.kuaixietech.service.platform;
public interface AnnouncementMigrationService {
 String publish(String authenticatedAdminId,String title,String body,String kind,Boolean pinned);
 String toggle(String authenticatedAdminId,String announcementId,Boolean active);
}
