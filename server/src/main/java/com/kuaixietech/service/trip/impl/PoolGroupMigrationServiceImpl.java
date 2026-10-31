package com.kuaixietech.service.trip.impl;
import com.kuaixietech.service.trip.PoolGroupMigrationService;
import com.kuaixietech.mapper.trip.PoolGroupMigrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
@Service public class PoolGroupMigrationServiceImpl implements PoolGroupMigrationService {
 private final PoolGroupMigrationMapper mapper;
 public PoolGroupMigrationServiceImpl(PoolGroupMigrationMapper mapper){this.mapper=mapper;}
 @Transactional public String claimPoolSlot(String tripId,String userId){
  if(userId==null||userId.isBlank())return "ERR:NO_AUTH";
  Map<String,Object> t=mapper.lockTrip(tripId);if(t==null)return "ERR:NO_TRIP";
  if(!"pool".equals(t.get("carry_mode")))return "ERR:NOT_POOL";
  if(!"active".equals(t.get("status")))return "ERR:TRIP_CLOSED";
  if("expired".equals(t.get("pool_status")))return "ERR:POOL_EXPIRED";
  Integer existing=mapper.existingSlot(tripId,userId);if(existing!=null)return "OK:"+existing;
  int occupied=mapper.occupied(tripId);int capacity=((Number)t.get("pool_slots")).intValue();
  if(occupied>=capacity)return "ERR:POOL_FULL";
  mapper.setForming(tripId);return "OK:"+(occupied+1);
 }
 @Transactional public String checkPoolGroup(String tripId){
  Map<String,Object> t=mapper.lockTrip(tripId);if(t==null||!"pool".equals(t.get("carry_mode")))return "ERR:NOT_POOL";
  int n=mapper.lockedCount(tripId);int capacity=((Number)t.get("pool_slots")).intValue();
  if(n>=capacity){mapper.setFormed(tripId);mapper.notifyFormed(tripId);return "OK:FORMED";}
  return "OK:FORMING:"+n+"/"+capacity;
 }
}
