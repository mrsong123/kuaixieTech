package com.kuaixietech.integration;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/portal/admin/integrations")
public class IntegrationAdminController {
 private final IntegrationConfiguration configuration;private final org.springframework.jdbc.core.JdbcTemplate db;private final com.kuaixietech.security.DomainAccessGuard guard;
 public IntegrationAdminController(IntegrationConfiguration configuration,org.springframework.jdbc.core.JdbcTemplate db,com.kuaixietech.security.DomainAccessGuard guard){this.configuration=configuration;this.db=db;this.guard=guard;}
 @GetMapping("/sms-deliveries") public List<Map<String,Object>> deliveries(){guard.requireAdmin();return db.queryForList("SELECT id,channel,status,error,created_at,updated_at FROM t_vc_delivery_outbox ORDER BY id DESC LIMIT 100").stream().map(row->{var safe=new LinkedHashMap<String,Object>(row);String error=Objects.toString(row.get("error"),"");if(!error.isEmpty()&&!error.matches("CODE_EXPIRED|(SMS|EMAIL)_[A-Z_]+|PROVIDER_(ACCEPTED|REJECTED):[A-Za-z0-9_.:-]{1,160}"))safe.put("error","LEGACY_ERROR_REDACTED");return (Map<String,Object>)safe;}).toList();}
 @GetMapping public Map<String,Object> catalog(){return configuration.catalog();}
 @PutMapping("/{vendor}/{product}") public Map<String,String> save(@PathVariable String vendor,@PathVariable String product,@Valid @RequestBody IntegrationConfiguration.Input input){return configuration.save(vendor,product,input);}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<ProblemDetail> invalid(){return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"INTEGRATION_CONFIGURATION_REJECTED"));}
 @ExceptionHandler({IllegalStateException.class,DataAccessException.class}) public ResponseEntity<ProblemDetail> unavailable(){return ResponseEntity.status(503).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"INTEGRATION_CONFIGURATION_UNAVAILABLE"));}
}
