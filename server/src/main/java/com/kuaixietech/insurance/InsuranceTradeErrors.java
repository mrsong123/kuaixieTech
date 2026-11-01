package com.kuaixietech.insurance;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class InsuranceTradeErrors {
 @ExceptionHandler(InsurancePremiumReconciliationRequired.class)
 public ResponseEntity<ProblemDetail> reconciliation(){return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"INSURANCE_PREMIUM_RECONCILIATION_REQUIRED"));}
}
