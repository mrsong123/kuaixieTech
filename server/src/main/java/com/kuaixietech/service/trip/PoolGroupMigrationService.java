package com.kuaixietech.service.trip;
public interface PoolGroupMigrationService {
 String claimPoolSlot(String tripId,String userId);
 String checkPoolGroup(String tripId);
}
