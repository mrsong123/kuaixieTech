package com.kuaixietech.payments;

import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes=PaymentsController.class)
public class PaymentErrors {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ProblemDetail> invalid(IllegalArgumentException error){return response(400,"PAYMENT_INPUT_REJECTED");}
  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<ProblemDetail> unavailable(IllegalStateException error){return response(503,"PAYMENT_OR_FX_UNAVAILABLE");}
  @ExceptionHandler(DataAccessException.class)
  ResponseEntity<ProblemDetail> database(DataAccessException error){return response(503,"PAYMENT_STORAGE_UNAVAILABLE");}
  private ResponseEntity<ProblemDetail> response(int status,String code){return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),code));}
}
