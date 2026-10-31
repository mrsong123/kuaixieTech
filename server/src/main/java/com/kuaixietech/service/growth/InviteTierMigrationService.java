package com.kuaixietech.service.growth;
import java.util.Map;
public interface InviteTierMigrationService {
 String refreshInviteTier(String uid);
 Map<String,Object> getMyInviteSummary(String authenticatedUserId);
}
