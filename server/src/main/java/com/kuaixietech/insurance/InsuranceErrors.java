package com.kuaixietech.insurance;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes=InsuranceController.class)
public class InsuranceErrors {
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ProblemDetail> input(IllegalArgumentException error){String code=error.getMessage();if(code==null||!code.matches("INSURANCE_[A-Z_]+"))code="INSURANCE_INPUT_REJECTED";return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,code));}
 @ExceptionHandler({IllegalStateException.class,DataAccessException.class}) ResponseEntity<ProblemDetail> unavailable(Exception error){return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"INSURANCE_PROVIDER_OR_STORAGE_UNAVAILABLE"));}
}
