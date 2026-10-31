package com.kuaixietech.controller;



import com.kuaixietech.service.account.impl.Batch12AccountRpcService;
import com.kuaixietech.service.assist.impl.Batch12AssistRpcService;
import com.kuaixietech.service.communication.impl.Batch12CallRpcService;
import com.kuaixietech.service.communication.impl.Batch12PrivacyRpcService;
import com.kuaixietech.service.community.impl.Batch12ContentModerationService;
import com.kuaixietech.security.TrustedActor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
/**
 * RPC layer: protected by default-deny ApiSecurityConfig until trusted identity is configured.
 * Caller ID is NEVER taken from a request body.
 */
@RestController @RequestMapping("/api/rpc/batch12")
public class Batch12RpcController {
 private final Batch12AssistRpcService assist;
 private final Batch12CallRpcService call;
 private final Batch12ContentModerationService moderation;
 private final Batch12AccountRpcService account;
 private final Batch12PrivacyRpcService privacy;
 public Batch12RpcController(Batch12AssistRpcService assist,Batch12CallRpcService call,Batch12ContentModerationService moderation,Batch12AccountRpcService account,Batch12PrivacyRpcService privacy){
  this.assist=assist;this.call=call;this.moderation=moderation;this.account=account;this.privacy=privacy;
 }
 public record CreateAssist(String orderId,BigDecimal rate,String note){}
 public record AssistId(String id){}
 public record Handover(String id,String urlsJson,String city){}
 public record OrderId(String orderId){}
 public record CallId(String callId){}
 public record RejectCall(String callId,boolean asCallee){}
 public record Comment(String postId,String content){}
 public record Enterprise(String enterpriseName,String businessLicenseNo,List<String> proofUrls){}
 public record Carrier(String realName,String idCardNo,String passportNo,String phoneCode,String inviteCode,String note){}
 public record MatchJoin(String applicationId){}
 public record VerifyCode(String target,String channel){}
 @PostMapping("/create_assist_request") public String create(@RequestBody CreateAssist r){return assist.createAssistRequest(r.orderId(),r.rate(),r.note());}
 @PostMapping("/accept_assist") public String accept(@RequestBody AssistId r){return assist.acceptAssist(r.id());}
 @PostMapping("/cancel_assist") public String cancel(@RequestBody AssistId r){return assist.cancelAssist(r.id());}
 @PostMapping("/submit_assist_handover_a") public String handover(@RequestBody Handover r){return assist.submitAssistHandoverA(r.id(),r.urlsJson(),r.city());}
 @PostMapping("/finish_assist") public String finish(@RequestBody Handover r){return assist.finishAssist(r.id(),r.urlsJson(),r.city());}
 @PostMapping("/start_order_call") public Map<String,Object> startCall(@RequestBody OrderId r){return call.startOrderCall(r.orderId());}
 @PostMapping("/accept_order_call") public Map<String,Object> acceptCall(@RequestBody CallId r){return call.acceptOrderCall(r.callId());}
 @PostMapping("/reject_order_call") public String rejectCall(@RequestBody RejectCall r){return call.rejectOrderCall(r.callId(),r.asCallee());}
 @PostMapping("/hangup_order_call") public Map<String,Object> hangup(@RequestBody CallId r){return call.hangupOrderCall(r.callId());}
 @PostMapping("/add_post_comment") public String comment(@RequestBody Comment r){return moderation.addPostComment(r.postId(),r.content());}
 @PostMapping("/screen_chat_content") public Batch12ContentModerationService.Screening screen(@RequestBody Comment r){return moderation.screenChatContent(TrustedActor.currentUserId(),r.content());}
 @PostMapping("/submit_enterprise_verify") public String enterprise(@RequestBody Enterprise r){return account.submitEnterpriseVerify(r.enterpriseName(),r.businessLicenseNo(),r.proofUrls());}
 @PostMapping("/submit_carrier_kyc") public boolean carrier(@RequestBody Carrier r){return account.submitCarrierKyc(r.realName(),r.idCardNo(),r.passportNo(),r.phoneCode(),r.inviteCode(),r.note());}
 @PostMapping("/match_join_application") public boolean match(@RequestBody MatchJoin r){return account.matchJoinApplication(r.applicationId());}
 @PostMapping("/request_verification_code") public String code(@RequestBody VerifyCode r){return account.requestVerificationCode(r.target(),r.channel());}
 @PostMapping("/get_or_bind_privacy_number") public Map<String,Object> privacy(@RequestBody OrderId r){return privacy.getOrBindPrivacyNumber(r.orderId());}
}
