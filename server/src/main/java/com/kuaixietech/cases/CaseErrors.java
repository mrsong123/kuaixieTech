package com.kuaixietech.cases;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class CaseErrors {
 @ExceptionHandler(CaseOpenException.class) public ResponseEntity<ProblemDetail> open(){return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"DISPUTE_OPEN"));}
}
