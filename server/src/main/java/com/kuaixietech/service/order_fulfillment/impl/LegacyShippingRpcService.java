package com.kuaixietech.service.order_fulfillment.impl;
import com.kuaixietech.mapper.order_fulfillment.LegacyShippingRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Exact final PostgreSQL RPC branches: update_ship_logistics, nudge_ship_update, notify_stalled_shipments. */
@Service
public class LegacyShippingRpcService {
  private final LegacyShippingRpcMapper mapper;
  public LegacyShippingRpcService(LegacyShippingRpcMapper mapper) {this.mapper=mapper;}
  private static final Map<String,String> NODES=Map.of(
    "procured","商品已采购入库", "shipped_abroad","海外段已寄出", "departed","国际航班已离港",
    "customs","已到港清关中", "domestic","国内寄出段已交承运商");
  @Transactional
  public String updateShipLogistics(String orderId,String node,String detail) {
    String actor=TrustedActor.currentUserId();
    String normalized=node==null?"":node.trim();
    if(!NODES.containsKey(normalized)) return "ERR:BAD_NODE";
    Map<String,Object> o=mapper.lockOrder(orderId);
    if(o==null)return "ERR:NO_ORDER";
    if(!actor.equals(o.get("vendor_id")))return "ERR:NOT_VENDOR";
    if(!Set.of("purchased","shipped").contains(String.valueOf(o.get("status"))))return "ERR:BAD_STATUS";
    String clean=detail==null||detail.trim().isEmpty()?null:detail.trim();
    mapper.addNode(UUID.randomUUID().toString(),orderId,actor,normalized,clean);
    mapper.addNotice(UUID.randomUUID().toString(),(String)o.get("buyer_id"),"logistics",
        "物流有新动态",NODES.get(normalized)+(detail==null?"":"："+detail),orderId);
    return "OK";
  }
  @Transactional
  public String nudgeShipUpdate(String orderId) {
    String actor=TrustedActor.currentUserId();
    Map<String,Object> o=mapper.lockOrder(orderId);
    if(o==null||!actor.equals(o.get("buyer_id")))return "ERR:FORBIDDEN";
    if(!Set.of("purchased","shipped").contains(String.valueOf(o.get("status"))))return "ERR:BAD_STATUS";
    // Serialize concurrent nudges against the order row to retain one per rolling 24h.
    if(mapper.recentNotice((String)o.get("vendor_id"),"logistics_nudge",orderId)>0)return "ERR:TOO_FREQUENT";
    mapper.addNotice(UUID.randomUUID().toString(),(String)o.get("vendor_id"),"logistics_nudge",
       "买家在等物流更新","买家提醒你更新本单物流节点，请尽快在订单详情补充轨迹。",orderId);
    return "OK";
  }
  /** Server-only task, never expose in an authenticated user controller. */
  @Transactional
  public int notifyStalledShipments() {
    int count=0;
    for(Map<String,Object> o: mapper.stalledOrders()) {
      String orderId=(String)o.get("id");
      // Re-check under exclusive order lock; concurrent runs must not duplicate alerts.
      Map<String,Object> current=mapper.lockOrder(orderId);
      if(current==null||!"shipped".equals(current.get("status"))||mapper.isStillStalled(orderId)==0)continue;
      String buyerId=(String)o.get("buyer_id");
      if(mapper.recentNotice(buyerId,"logistics_stall",orderId)>0)continue;
      mapper.addNotice(UUID.randomUUID().toString(),buyerId,"logistics_stall","物流信息超48小时未更新",
        "订单 "+o.get("order_no")+" 已超过 48 小时没有新的物流动态，可一键提醒带单人更新，或在争议入口反馈。",orderId);
      count++;
    }
    return count;
  }
}
