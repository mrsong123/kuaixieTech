package com.kuaixietech.controller;

import com.kuaixietech.mapper.catalog.PublicCatalogMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Safe read-only public endpoints. Never expose BaseMapper or an entity with confidential fields. */
@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {
  private final PublicCatalogMapper mapper;
  public PublicCatalogController(PublicCatalogMapper mapper) { this.mapper=mapper; }
  private int bounded(int count) { if(count<1 || count>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"limit must be 1..100"); return count; }
  @GetMapping("/categories")
  public List<Map<String,Object>> categories(@RequestParam(defaultValue="100") int limit, @RequestParam(required=false) String countryCode) {
    String c=countryCode==null?null:countryCode.trim().toLowerCase(java.util.Locale.ROOT);
    if(c!=null && !c.matches("[a-z]{2}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"2-letter country or region code required");
    return mapper.categories(bounded(limit),c);
  }
  @GetMapping("/products")
  public List<Map<String,Object>> products(@RequestParam(defaultValue="30") int limit) {return mapper.products(bounded(limit));}
  @GetMapping("/trips")
  public List<Map<String,Object>> trips(@RequestParam(defaultValue="30") int limit,@RequestParam(defaultValue="rank") String sort) {
    if(!java.util.Set.of("rank","latest","score").contains(sort))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid sort");
    return mapper.trips(bounded(limit),sort);
  }
  @GetMapping("/profiles/masked")
  public List<Map<String,Object>> maskedProfiles(@RequestParam String ids) {
    List<String> parsed=Arrays.stream(ids.split(",",-1)).map(String::trim).distinct().toList();
    if(parsed.isEmpty()||parsed.size()>50||parsed.stream().anyMatch(s->!s.matches("(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"1..50 UUIDs required");
    return mapper.maskedProfiles(parsed);
  }
}
