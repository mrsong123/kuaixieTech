package com.kuaixietech.controller;

import com.kuaixietech.service.account.impl.AccountCentreService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class AccountCentreController {
  private final AccountCentreService service;
  public AccountCentreController(AccountCentreService service){this.service=service;}
  public record ProfileUpdate(String nickname,String city){}
  @GetMapping("/profile") public Map<String,Object> profile(){return service.profile();}
  @PutMapping("/profile") public Map<String,Object> update(@RequestBody ProfileUpdate input){return service.updateProfile(input.nickname(),input.city());}
  @GetMapping("/orders") public List<Map<String,Object>> orders(@RequestParam(defaultValue="all") String role,@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.orders(role,limit,offset);}
  @GetMapping("/orders/{id}") public Map<String,Object> order(@PathVariable String id){return service.order(id);}
  @GetMapping("/demands") public List<Map<String,Object>> demands(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.demands(limit,offset);}
  @GetMapping("/trips") public List<Map<String,Object>> trips(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.trips(limit,offset);}
  @GetMapping("/wallet") public Map<String,Object> wallet(){return service.wallet();}
  @GetMapping("/wallet/ledger") public List<Map<String,Object>> ledger(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.ledger(limit,offset);}
  @GetMapping("/notifications") public List<Map<String,Object>> notifications(@RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset){return service.notifications(limit,offset);}
  @PostMapping("/notifications/{id}/read") public Map<String,String> read(@PathVariable String id){return service.readNotification(id);}
}
