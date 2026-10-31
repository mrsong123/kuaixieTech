package com.kuaixietech.service.community;
public interface CommunityMigrationService {
 String toggleLike(String authenticatedUserId,String postId);
 String adminRemovePost(String authenticatedAdminId,String postId,String reason);
}
