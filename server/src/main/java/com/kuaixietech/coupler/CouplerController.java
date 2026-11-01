package com.kuaixietech.coupler;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static com.kuaixietech.coupler.CouplerModels.*;
import com.kuaixietech.coupler.CouplerModels.Runtime;
@RestController
@RequestMapping("/api/portal/admin/integration-center")
public class CouplerController {
 private final CouplerService service;
 public CouplerController(CouplerService service){this.service=service;}
 @GetMapping public Map<String,Object> settings(){return service.settings();}
 @PutMapping("/runtime") public Runtime runtime(@Valid @RequestBody Switch input){return service.switchMode(input);}
 @PutMapping("/{category}/{provider}/{mode}") public Map<String,Long> save(@PathVariable String category,@PathVariable String provider,@PathVariable Mode mode,@Valid @RequestBody Save input){return service.save(category,provider,mode,input);}
 @PostMapping("/{category}/{provider}/{mode}/validate") public Map<String,Object> validate(@PathVariable String category,@PathVariable String provider,@PathVariable Mode mode){return service.validate(category,provider,mode);}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ProblemDetail> rejected(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),Objects.toString(e.getReason(),"COUPLER_REJECTED")));}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<ProblemDetail> invalid(){return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"COUPLER_CONFIGURATION_REJECTED"));}
 @ExceptionHandler({IllegalStateException.class,DataAccessException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"COUPLER_CONFIGURATION_UNAVAILABLE"));}
}
