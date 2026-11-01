package com.kuaixietech.controller;
import com.kuaixietech.service.catalog.support.ProductPublishingService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/me/products")
public class ProductPublishingController {
  private final ProductPublishingService service;
  public ProductPublishingController(ProductPublishingService service){this.service=service;}
  @GetMapping public List<Map<String,Object>> mine(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.mine(limit,offset);}
  @PostMapping public Map<String,String> create(@Valid @RequestBody ProductPublishingService.Input input){return service.create(input);}
  @PutMapping("/{id}") public Map<String,String> update(@PathVariable String id,@Valid @RequestBody ProductPublishingService.Input input){return service.update(id,input);}
  @PutMapping("/{id}/status") public Map<String,String> state(@PathVariable String id,@Valid @RequestBody ProductPublishingService.State input){return service.state(id,input);}
}
