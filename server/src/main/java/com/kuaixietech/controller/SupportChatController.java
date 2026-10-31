package com.kuaixietech.controller;
import com.kuaixietech.entity.communication.SupportDtos;
import com.kuaixietech.service.communication.SupportChatService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/support") public class SupportChatController {
 private final SupportChatService svc;
 public SupportChatController(SupportChatService svc){this.svc=svc;}
 @PostMapping("/conversations") public Map<String,Object> open(){return svc.open();}
 @GetMapping("/conversations/{id}") public Map<String,Object> get(@PathVariable String id){return svc.get(id);}
 @GetMapping("/conversations/{id}/messages") public List<Map<String,Object>> messages(@PathVariable String id,@RequestParam(defaultValue="60") int limit){return svc.messages(id,limit);}
 @PostMapping("/conversations/{id}/messages") public Map<String,Object> send(@PathVariable String id,@Valid @RequestBody SupportDtos.Send request,@RequestHeader(value="Accept-Language",defaultValue="zh-CN") String locale){return svc.send(id,request.content(),locale);}
 @PostMapping("/conversations/{id}/handoff") public Map<String,Object> handoff(@PathVariable String id){return svc.handoff(id);}
 @GetMapping("/admin/queue") public List<Map<String,Object>> queue(){return svc.queue();}
 @GetMapping("/admin/{id}/messages") public List<Map<String,Object>> agentMessages(@PathVariable String id,@RequestParam(defaultValue="60") int limit){return svc.agentMessages(id,limit);}
 @PostMapping("/admin/{id}/claim") public Map<String,Object> claim(@PathVariable String id){return svc.claim(id);}
 @PostMapping("/admin/{id}/reply") public Map<String,Object> agentReply(@PathVariable String id,@Valid @RequestBody SupportDtos.AgentReply b){return svc.agentReply(id,b.content());}
}
