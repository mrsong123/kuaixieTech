package com.kuaixietech.controller;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
/** Admin methods remain denied by the default security filter until verified identity is configured. */
@RestController
@RequestMapping("/api/rpc")
public class AdminFinanceRpcController {
 private final LegacyAdminRpcService svc;
 public AdminFinanceRpcController(LegacyAdminRpcService svc){this.svc=svc;}
 public record BalanceBody(String userId,BigDecimal delta,String note) { }
 public record PoolBody(String tripId,String reason) { }
 public record LimitBody(Integer limit) { }
 @PostMapping("/admin_adjust_balance") public String adjust(@RequestBody BalanceBody b){return svc.adjustBalance(b.userId(),b.delta(),b.note());}
 @PostMapping("/admin_dissolve_pool") public String dissolve(@RequestBody PoolBody b){return svc.dissolvePool(b.tripId(),b.reason());}
 @PostMapping("/admin_commission_rows") public List<Map<String,Object>> commissions(@RequestBody LimitBody b){return svc.commissionRows(b.limit());}
}
