package com.kuaixietech.controller;
import com.kuaixietech.service.growth.impl.InviteReadRpcService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
/** Return only source-defined safe invite profile columns. */
@RestController
@RequestMapping("/api/rpc")
public class InviteReadRpcController {
 private final InviteReadRpcService svc;
 public InviteReadRpcController(InviteReadRpcService svc){this.svc=svc;}
 public record Root(String root) { }
 public record Code(String code) { }
 public record Operator(String operatorId) { }
 @PostMapping("/list_team_members") public List<Map<String,Object>> members(@RequestBody Root body){return svc.listTeamMembers(body.root());}
 @PostMapping("/get_invite_code_owner") public String owner(@RequestBody Code body){return svc.getInviteCodeOwner(body.code());}
 @PostMapping("/get_operator_invite_code") public String operator(@RequestBody Operator body){return svc.getOperatorInviteCode(body.operatorId());}
}
