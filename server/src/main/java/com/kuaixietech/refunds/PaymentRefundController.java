package com.kuaixietech.refunds;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.kuaixietech.refunds.RefundModels.*;
@RestController
public class PaymentRefundController {
 private final RefundService service;
 public PaymentRefundController(RefundService service){this.service=service;}
 @GetMapping("/api/me/payment-refunds/eligible") public List<Map<String,Object>> eligible(){return service.eligible();}
 @GetMapping("/api/me/payment-refunds") public List<Map<String,Object>> mine(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.list(false,limit,offset);}
 @PostMapping("/api/me/payment-refunds") public Map<String,Object> create(@Valid @RequestBody Create input){return service.create(input);}
 @GetMapping("/api/me/payment-refunds/{id}") public Map<String,Object> detail(@PathVariable String id){return service.detail(id,false);}
 @GetMapping("/api/me/payment-refunds/{id}/events") public List<Map<String,Object>> history(@PathVariable String id,@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="0") int offset){return service.events(id,false,limit,offset);}
 @PostMapping("/api/me/payment-refunds/{id}/cancel") public Map<String,Object> cancel(@PathVariable String id){return service.cancel(id);}
 @PostMapping("/api/me/payment-refunds/{id}/refresh") public Map<String,Object> refresh(@PathVariable String id){return service.refresh(id,false);}
 @GetMapping("/api/portal/admin/payment-refunds") public List<Map<String,Object>> queue(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.list(true,limit,offset);}
 @GetMapping("/api/portal/admin/payment-refunds/{id}") public Map<String,Object> adminDetail(@PathVariable String id){return service.detail(id,true);}
 @GetMapping("/api/portal/admin/payment-refunds/{id}/events") public List<Map<String,Object>> adminHistory(@PathVariable String id,@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="0") int offset){return service.events(id,true,limit,offset);}
 @PostMapping("/api/portal/admin/payment-refunds/{id}/review") public Map<String,Object> review(@PathVariable String id,@Valid @RequestBody Review input){return service.review(id,input);}
 @PostMapping("/api/portal/admin/payment-refunds/{id}/dispatch") public Map<String,Object> dispatch(@PathVariable String id){return service.dispatch(id);}
 @PostMapping("/api/portal/admin/payment-refunds/{id}/refresh") public Map<String,Object> adminRefresh(@PathVariable String id){return service.refresh(id,true);}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> rejected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),Objects.toString(e.getReason(),"REFUND_REJECTED")));}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<ProblemDetail> invalid(){return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"REFUND_INPUT_REJECTED"));}
 @ExceptionHandler({DataAccessException.class,IllegalStateException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"REFUND_SERVICE_UNAVAILABLE"));}
}
