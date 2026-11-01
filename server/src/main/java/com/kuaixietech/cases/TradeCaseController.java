package com.kuaixietech.cases;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.kuaixietech.cases.CaseModels.*;
@RestController
public class TradeCaseController {
 private final TradeCaseService service;
 public TradeCaseController(TradeCaseService service){this.service=service;}
 @GetMapping("/api/me/disputes") public List<Map<String,Object>> mine(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.mine(limit,offset);}
 @GetMapping("/api/me/disputes/{id}") public Map<String,Object> detail(@PathVariable String id){return service.detail(id,false);}
 @PostMapping("/api/me/disputes") public Map<String,String> create(@Valid @RequestBody Create input){return service.create(input);}
 @PostMapping("/api/me/disputes/{id}/respond") public Map<String,String> respond(@PathVariable String id,@Valid @RequestBody Respond input){return service.respond(id,input);}
 @PostMapping("/api/me/disputes/{id}/escalate") public Map<String,String> escalate(@PathVariable String id,@Valid @RequestBody Escalate input){return service.escalate(id,input);}
 @PostMapping("/api/me/disputes/{id}/appeal") public Map<String,String> appeal(@PathVariable String id,@Valid @RequestBody Appeal input){return service.appeal(id,input);}
 @PostMapping("/api/me/disputes/{id}/finish") public Map<String,String> finish(@PathVariable String id,@Valid @RequestBody Finish input){return service.finish(id,input);}
 @GetMapping("/api/me/disputes/{id}/events") public List<Map<String,Object>> history(@PathVariable String id,@RequestParam(defaultValue="100") int limit,@RequestParam(defaultValue="0") int offset){return service.history(id,limit,offset,false);}
 @GetMapping("/api/portal/admin/disputes") public List<Map<String,Object>> queue(@RequestParam(defaultValue="50") int limit,@RequestParam(defaultValue="0") int offset){return service.queue(limit,offset);}
 @GetMapping("/api/portal/admin/disputes/{id}") public Map<String,Object> adminDetail(@PathVariable String id){return service.detail(id,true);}
 @PostMapping("/api/portal/admin/disputes/{id}/decision") public Map<String,String> decide(@PathVariable String id,@Valid @RequestBody Decision input){return service.decide(id,input);}
 @GetMapping("/api/portal/admin/disputes/{id}/events") public List<Map<String,Object>> adminHistory(@PathVariable String id,@RequestParam(defaultValue="100") int limit,@RequestParam(defaultValue="0") int offset){return service.history(id,limit,offset,true);}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> rejected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),e.getReason()==null?"DISPUTE_REJECTED":e.getReason()));}
 @ExceptionHandler({DataAccessException.class,IllegalStateException.class}) public ResponseEntity<ProblemDetail> unavailable(Exception e){return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"DISPUTE_STORAGE_UNAVAILABLE"));}
}
