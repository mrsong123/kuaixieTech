package com.kuaixietech.commerce;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
public class ProductCheckoutController {
 private final ProductCheckoutService service;
 public ProductCheckoutController(ProductCheckoutService service){this.service=service;}
 @GetMapping("/api/public/products/{id}/trips") public List<Map<String,Object>> trips(@PathVariable String id,@RequestParam(defaultValue="100") int limit,@RequestParam(defaultValue="0") int offset){return service.trips(id,limit,offset);}
 @PostMapping("/api/me/product-orders/quote") public ProductCheckoutModels.Quote quote(@Valid @RequestBody ProductCheckoutModels.Input input){return service.quote(input);}
 @PostMapping("/api/me/product-orders") public Map<String,String> create(@Valid @RequestBody ProductCheckoutModels.Input input){return service.create(input);}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> rejected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),Objects.toString(e.getReason(),"PRODUCT_ORDER_REJECTED")));}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<ProblemDetail> invalid(IllegalArgumentException e){String code=Objects.toString(e.getMessage(),"");if(!code.matches("[A-Z][A-Z0-9_]{2,100}"))code="PRODUCT_ORDER_INPUT_REJECTED";return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,code));}
 @ExceptionHandler({DataAccessException.class,IllegalStateException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"PRODUCT_ORDER_SERVICE_UNAVAILABLE"));}
}
