package com.kuaixietech.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.security.DomainAccessGuard;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
/** Administrator-only delivery metadata. Never exposes business rows or credential values. */
@RestController
@RequestMapping("/api/portal/admin")
public class BusinessCoverageController {
 private final DomainAccessGuard guard;private final ObjectMapper json;
 public BusinessCoverageController(DomainAccessGuard guard,ObjectMapper json){this.guard=guard;this.json=json;}
 @GetMapping("/business-coverage") public JsonNode coverage() throws IOException {
  guard.requireAdmin();
  try(var input=new ClassPathResource("business-coverage.json").getInputStream()){return json.readTree(input);}
 }
}
