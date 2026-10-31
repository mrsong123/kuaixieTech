package com.kuaixietech.service.order_fulfillment.impl;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.kuaixietech.mapper.order_fulfillment.CancellationTierMigrationMapper;

/** Exact conditional order of final compute_cancel_tier PostgreSQL function (20261021_145452). */
@Service
public class CancellationTierMigrationService {
  private final CancellationTierMigrationMapper mapper;
  public CancellationTierMigrationService(CancellationTierMigrationMapper mapper) {this.mapper=mapper;}
  public String compute(String orderId,boolean isBuyer) {
    return decide(mapper.orderTierFacts(orderId),isBuyer,LocalDateTime.now(java.time.Clock.systemUTC()));
  }
  /** Pure function exposed for reproducible tests. Timestamps expected in UTC. */
  public static String decide(Map<String,Object> facts,boolean isBuyer,LocalDateTime now) {
    if(facts==null) return "ERR:NO_ORDER";
    String status=String.valueOf(facts.get("status"));
    if(!java.util.Set.of("requesting","negotiating","vendor_deposit_pending","locked","purchased").contains(status))return "breach";
    LocalDateTime confirmed=timestamp(facts.get("confirmed_at"));
    if(confirmed!=null && now.isBefore(confirmed.plusMinutes(30)))return "cooldown";
    if(!isBuyer)return "breach";
    if(facts.get("trip_id")==null)return "free";
    LocalDateTime departure=timestamp(facts.get("depart_datetime"));
    if(departure==null)return "free";
    if(departure.isAfter(now.plusHours(24))) return "free";
    if(departure.isAfter(now.plusHours(2))) return "hour_24";
    return "breach";
  }
  private static LocalDateTime timestamp(Object v) {
    if(v==null)return null;
    if(v instanceof LocalDateTime t)return t;
    if(v instanceof java.sql.Timestamp t)return t.toLocalDateTime();
    throw new IllegalArgumentException("Unexpected timestamp type: "+v.getClass());
  }
}
