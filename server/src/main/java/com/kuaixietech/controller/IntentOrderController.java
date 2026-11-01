package com.kuaixietech.controller;

import com.kuaixietech.entity.portal.IntentInput;
import com.kuaixietech.service.trading.impl.IntentOrderService;
import com.kuaixietech.service.trading.support.IntentPricing;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/intents")
public class IntentOrderController {
  private final IntentOrderService service;
  public IntentOrderController(IntentOrderService service){this.service=service;}
  @PostMapping("/quote") public IntentPricing.Quote quote(@Valid @RequestBody IntentInput input){return service.quote(input);}
  @PostMapping public Map<String,String> create(@Valid @RequestBody IntentInput input){return service.create(input);}
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> rejected(ResponseStatusException error){
    ProblemDetail body=ProblemDetail.forStatusAndDetail(error.getStatusCode(),error.getReason()==null?"ORDER_REJECTED":error.getReason());
    return ResponseEntity.status(error.getStatusCode()).body(body);
  }
}
