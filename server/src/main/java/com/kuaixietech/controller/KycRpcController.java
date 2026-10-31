package com.kuaixietech.controller;

import com.kuaixietech.service.account.impl.KycRpcMigrationService;
import org.springframework.web.bind.annotation.*;
/** Authenticated-only RPC wrappers. ApiSecurityConfig stays fail-closed until JWT integration. */
@RestController
@RequestMapping("/api/rpc")
public class KycRpcController {
 private final KycRpcMigrationService svc;
 public KycRpcController(KycRpcMigrationService svc){this.svc=svc;}
 public record BuyerKyc(String realName,String idCardNo) { }
 public record KycDoc(String docType,String storageUrl,String challengeCode) { }
 public record ReviewKyc(String userId,boolean approve,String reason) { }
 public record FaceIssue(String scene,String orderId) { }
 public record FaceRecord(String scene,String snapshotUrl,String deviceInfo,String orderId,String challengeCode) { }
 @PostMapping("/submit_buyer_kyc") public String submit(@RequestBody BuyerKyc body){
    return svc.submitBuyerKyc(body.realName(),body.idCardNo());
 }
 @PostMapping("/upsert_kyc_doc_with_challenge") public String doc(@RequestBody KycDoc body){
    return svc.upsertKycDocWithChallenge(body.docType(),body.storageUrl(),body.challengeCode());
 }
 @PostMapping("/admin_review_buyer_kyc") public String review(@RequestBody ReviewKyc body){
    return svc.adminReviewBuyerKyc(body.userId(),body.approve(),body.reason());
 }
 @PostMapping("/issue_face_challenge") public KycRpcMigrationService.FaceChallenge challenge(@RequestBody FaceIssue body){
    return svc.issueFaceChallenge(body.scene(),body.orderId());
 }
 @PostMapping("/record_face_verification") public String record(@RequestBody FaceRecord body){
    return svc.recordFaceVerification(body.scene(),body.snapshotUrl(),body.deviceInfo(),body.orderId(),body.challengeCode());
 }
}
