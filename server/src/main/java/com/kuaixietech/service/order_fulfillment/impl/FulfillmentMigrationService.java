package com.kuaixietech.service.order_fulfillment.impl;

import com.kuaixietech.mapper.order_fulfillment.FulfillmentMigrationMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ports consume_product_stock and append_logistics_node. Never call from an unauthenticated controller. */
@Service
public class FulfillmentMigrationService {
  private final FulfillmentMigrationMapper mapper;
  public FulfillmentMigrationService(FulfillmentMigrationMapper mapper){this.mapper=mapper;}
  @Transactional
  public String consumeProductStock(String productId,Integer qty,String orderId) {
    if(qty==null||qty<=0)return "ERR:BAD_QTY";
    Map<String,Object> product=mapper.lockProduct(productId);
    if(product==null)return "ERR:NO_PRODUCT";
    if(!"on".equals(product.get("status")))return "ERR:OFF_SHELF";
    int stock=((Number)product.get("stock")).intValue();
    if(stock<qty)return "ERR:NO_STOCK";
    int updated=mapper.decrementProductStock(productId,qty);
    if(updated!=1) throw new IllegalStateException("Concurrent stock update failed");
    int remain=stock-qty;
    String detail="锁定商品「"+product.get("name")+"」×"+qty+"，剩余库存 "+remain;
    mapper.insertLogistics(UUID.randomUUID().toString(),orderId,String.valueOf(product.get("owner_id")),"procured","system",detail);
    return "OK:"+remain;
  }
  @Transactional
  public String appendLogisticsNode(String orderId,String node,String detail) {
    String actor=TrustedActor.currentUserId();
    if(node==null||!Set.of("departed","customs","domestic").contains(node))return "ERR:BAD_NODE";
    Map<String,Object> order=mapper.orderParties(orderId);
    if(order==null)return "ERR:NO_ORDER";
    if(!actor.equals(String.valueOf(order.get("vendor_id"))))return "ERR:NOT_VENDOR";
    String normalized=detail==null||detail.isBlank()?null:detail.trim();
    mapper.insertLogistics(UUID.randomUUID().toString(),orderId,actor,node,"vendor",normalized);
    String body=switch(node) {
      case "departed" -> "包裹已从境外启程（国际段发出）";
      case "customs" -> "包裹已进入清关";
      default -> "包裹已进入国内派送";
    };
    mapper.insertBuyerNotification(UUID.randomUUID().toString(),String.valueOf(order.get("buyer_id")),orderId,body);
    return "OK";
  }
}
