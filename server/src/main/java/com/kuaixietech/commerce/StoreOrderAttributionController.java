package com.kuaixietech.commerce;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
/** Read the original store attribution, never infer a new charge or disclose buyer contact details. */
@RestController
public class StoreOrderAttributionController {
 private final JdbcTemplate db;private final DomainAccessGuard guard;
 public StoreOrderAttributionController(JdbcTemplate db,DomainAccessGuard guard){this.db=db;this.guard=guard;}
 private void page(int limit,int offset){if(limit<1||limit>100||offset<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"PRODUCT_INVALID_PAGE");}
 private static final String SELECT="SELECT o.id,o.order_no,o.item_name,o.status,r.product_id,r.quantity,r.goods_amount,r.service_fee,r.currency,r.stock_state,r.created_at FROM t_product_order_reservations r JOIN t_orders o ON o.id=r.order_id JOIN t_stores s ON s.id=r.store_id AND s.owner_id=r.vendor_id ";
 @GetMapping("/api/me/shop/orders") public List<Map<String,Object>> mine(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){String actor=guard.requireActiveProfile();page(limit,offset);return db.queryForList(SELECT+"WHERE s.owner_id=? ORDER BY r.created_at DESC,o.id DESC LIMIT ? OFFSET ?",actor,limit,offset);}
 @GetMapping("/api/portal/admin/shops/{id}/orders") public List<Map<String,Object>> admin(@PathVariable String id,@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){guard.requireAdmin();page(limit,offset);return db.queryForList(SELECT+"WHERE s.id=? ORDER BY r.created_at DESC,o.id DESC LIMIT ? OFFSET ?",id,limit,offset);}
}
