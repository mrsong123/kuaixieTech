package com.kuaixietech.controller;
import com.kuaixietech.service.portal.impl.PublicInformationService;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/public")
public class PublicInformationController {
 private final PublicInformationService service;
 public PublicInformationController(PublicInformationService service){this.service=service;}
 @GetMapping("/announcements") public List<Map<String,Object>> announcements(@RequestParam(defaultValue="30") int limit){return service.announcements(limit);}
 @GetMapping("/legal-documents/{key}") public Map<String,Object> legal(@PathVariable String key){return service.legal(key);}
 @GetMapping("/reviews") public List<Map<String,Object>> reviews(@RequestParam String targetId,@RequestParam(defaultValue="50") int limit){return service.reviews(targetId,limit);}
}
