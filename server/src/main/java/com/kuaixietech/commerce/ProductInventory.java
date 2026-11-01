package com.kuaixietech.commerce;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Called inside the order transition transaction, after the order row is locked. */
@Service
public class ProductInventory {
 private final JdbcTemplate db;
 public ProductInventory(JdbcTemplate db){this.db=db;}
 @Transactional public void transition(String orderId,String target){if(!Set.of("consumed","released").contains(target))throw new IllegalArgumentException("PRODUCT_STOCK_TRANSITION_INVALID");
  // Legacy orders have no inventory reservation. Avoid gap locks for those orders.
  if(db.queryForList("SELECT order_id FROM t_product_order_reservations WHERE order_id=?",orderId).isEmpty())return;
  var rows=db.queryForList("SELECT product_id,quantity,stock_state FROM t_product_order_reservations WHERE order_id=? FOR UPDATE",orderId);if(rows.isEmpty())throw new IllegalStateException("PRODUCT_RESERVATION_DISAPPEARED");var r=rows.getFirst();String state=Objects.toString(r.get("stock_state"),"");if(!Set.of("held","consumed","released").contains(state))throw new IllegalStateException("PRODUCT_RESERVATION_INVALID_STATE");if(state.equals("released")&&target.equals("consumed"))throw new IllegalStateException("PRODUCT_RESERVATION_ALREADY_RELEASED");if(!state.equals("held"))return;
  if(target.equals("released")){if(db.update("UPDATE t_products SET stock=stock+?,updated_at=CURRENT_TIMESTAMP(3) WHERE id=?",r.get("quantity"),r.get("product_id"))!=1)throw new IllegalStateException("PRODUCT_STOCK_RESTORE_FAILED");}
  if(db.update("UPDATE t_product_order_reservations SET stock_state=?,updated_at=CURRENT_TIMESTAMP(3) WHERE order_id=? AND stock_state='held'",target,orderId)!=1)throw new IllegalStateException("PRODUCT_RESERVATION_CHANGED");
 }
}
