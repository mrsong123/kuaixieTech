package com.kuaixietech.aftersalesfunds;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
public class AftersalesEscrowController {
 private final AftersalesEscrowFunds funds;
 public AftersalesEscrowController(AftersalesEscrowFunds funds){this.funds=funds;}
 @GetMapping("/api/portal/admin/after-sales/{id}/escrow-budget") public Map<String,Object> budget(@PathVariable String id){return funds.budget(id);}
 @PostMapping("/api/portal/admin/after-sales/{id}/escrow-refund") public Map<String,Object> execute(@PathVariable String id,@Valid @RequestBody AftersalesEscrowFunds.Execute input){return funds.execute(id,input);}
 @GetMapping("/api/me/after-sales/escrow-refunds") public List<Map<String,Object>> mine(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return funds.mine(limit,offset);}
 @GetMapping("/api/me/after-sales/escrow-refunds/{id}/events") public List<Map<String,Object>> events(@PathVariable String id,@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="0") int offset){return funds.events(id,false,limit,offset);}
 @GetMapping("/api/portal/admin/after-sales/escrow-refunds/{id}/events") public List<Map<String,Object>> adminEvents(@PathVariable String id,@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="0") int offset){return funds.events(id,true,limit,offset);}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> rejected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),Objects.toString(e.getReason(),"AS_ESCROW_REJECTED")));}
 @ExceptionHandler(EscrowFundsProof.Blocked.class) public ResponseEntity<ProblemDetail> blocked(EscrowFundsProof.Blocked e){return ResponseEntity.status(409).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.getMessage()));}
 @ExceptionHandler({DataAccessException.class,IllegalStateException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"AS_ESCROW_SERVICE_UNAVAILABLE"));}
}
