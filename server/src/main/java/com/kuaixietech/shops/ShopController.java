package com.kuaixietech.shops;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
public class ShopController {
 private final ShopService shops;
 public ShopController(ShopService shops){this.shops=shops;}
 @GetMapping("/api/me/shop") public Map<String,Object> mine(){return shops.mine();}
 @PostMapping("/api/me/shop/applications") public Map<String,String> apply(@Valid @RequestBody ShopService.Application input){return shops.apply(input);}
 @PutMapping("/api/me/shop") public Map<String,String> edit(@Valid @RequestBody ShopService.Edit input){shops.edit(input);return Map.of("status","saved");}
 @PutMapping("/api/me/shop/{id}/status") public Map<String,String> state(@PathVariable String id,@Valid @RequestBody ShopService.State input){shops.state(id,input,false);return Map.of("status","saved");}
 @GetMapping("/api/me/shop/products") public List<Map<String,Object>> linked(@RequestParam(defaultValue="100") int limit,@RequestParam(defaultValue="0") int offset){return shops.linked(limit,offset);}
 @PostMapping("/api/me/shop/products") public Map<String,String> link(@Valid @RequestBody ShopService.Link input){shops.link(input.productId(),false);return Map.of("status","saved");}
 @DeleteMapping("/api/me/shop/products/{id}") public Map<String,String> unlink(@PathVariable String id){shops.link(id,true);return Map.of("status","saved");}
 @GetMapping("/api/public/shops/{id}") public Map<String,Object> publicShop(@PathVariable String id,@RequestParam(defaultValue="100") int limit,@RequestParam(defaultValue="0") int offset){return shops.publicShop(id,limit,offset);}
 @GetMapping("/api/portal/admin/shops/applications") public List<Map<String,Object>> queue(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return shops.queue(limit,offset);}
 @PostMapping("/api/portal/admin/shops/applications/{id}/review") public Map<String,String> review(@PathVariable String id,@Valid @RequestBody ShopService.Review input){return shops.review(id,input);}
 @PutMapping("/api/portal/admin/shops/{id}/status") public Map<String,String> adminState(@PathVariable String id,@Valid @RequestBody ShopService.State input){shops.state(id,input,true);return Map.of("status","saved");}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> rejected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),Objects.toString(e.getReason(),"SHOP_REJECTED")));}
 @ExceptionHandler({DataAccessException.class,IllegalStateException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"SHOP_SERVICE_UNAVAILABLE"));}
}
