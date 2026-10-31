package com.kuaixietech.service.account.impl;
import com.kuaixietech.mapper.account.KycRpcMapper;
import com.kuaixietech.security.TrustedActor;
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Source: 20261021_145350 and 20261021_140355. Selfie URL is evidence, NOT face recognition. */
@Service
public class KycRpcMigrationService {
 private final KycRpcMapper mapper;
 private static final SecureRandom RANDOM=new SecureRandom();
 private static final Set<String> DOCUMENTS=Set.of("id_card_front","id_card_back","passport","holding_id","real_name_selfie","visa");
 public KycRpcMigrationService(KycRpcMapper mapper){this.mapper=mapper;}
 private static String id(){return UUID.randomUUID().toString();}
 private static String blank(String s){return s==null?"":s.trim();}
 private static void inserted(int count){if(count!=1)throw new IllegalStateException("KYC state write failed; rollback");}
 @Transactional
 public String submitBuyerKyc(String realName,String idCardNo){
   String actor=TrustedActor.currentUserId();
   String name=blank(realName),number=blank(idCardNo);
   if(name.length()<2)return "ERR:BAD_NAME";
   if(!number.matches("[0-9]{17}[0-9Xx]"))return "ERR:BAD_IDCARD";
   if(mapper.hasDoc(actor,"id_card_front",false)==0)return "ERR:NO_FRONT";
   if(mapper.hasDoc(actor,"id_card_back",false)==0)return "ERR:NO_BACK";
   if(mapper.hasDoc(actor,"holding_id",true)==0)return "ERR:NO_HOLDING";
   String masked=number.substring(0,4)+"**********"+number.substring(number.length()-4);
   inserted(mapper.updateBuyerSubmission(actor,name,masked));
   inserted(mapper.addNotice(id(),actor,"实名认证审核中","你已提交身份证三件套，运营将在 24 小时内完成比对核验；通过后即可发布求购单与代购意向。"));
   return "OK";
 }
 @Transactional
 public String upsertKycDocWithChallenge(String docType,String storageUrl,String challengeCode){
   String actor=TrustedActor.currentUserId();
   if(!DOCUMENTS.contains(docType))return "ERR:BAD_TYPE";
   if(storageUrl==null||storageUrl.length()<8)return "ERR:BAD_URL";
   // INSERT returns 1; MySQL ON DUPLICATE KEY UPDATE can return 2 for an updated row.
   if(mapper.upsertDoc(id(),actor,docType,storageUrl,challengeCode)<1)
     throw new IllegalStateException("KYC upsert did not affect a row");
   return "OK";
 }
 @Transactional
 public String adminReviewBuyerKyc(String target,boolean approve,String reason){
   String actor=TrustedActor.currentUserId();
   if(mapper.isAdmin(actor)==0)return "ERR:NO_PERM";
   if(target==null)return "ERR:BAD_ID";
   String r=blank(reason);
   if(mapper.reviewBuyer(target,approve,r.isEmpty()?null:r)!=1)return "ERR:NO_USER";
   mapper.reviewDocs(target,approve,r.isEmpty()?null:r);
   inserted(mapper.addNotice(id(),target,approve?"实名认证已通过":"实名认证被驳回",
      approve?"恭喜，你现在可以发布求购单与代购意向了。":"驳回原因："+(r.isEmpty()?"材料不清晰，请重新拍摄上传":r)+"。修改后可重新提交。"));
   inserted(mapper.addAdminLog(id(),actor,target,(approve?"批准":"驳回")+" 买家实名 "+r));
   return "OK";
 }
 public record FaceChallenge(String challengeId,String code){ }
 @Transactional
 public FaceChallenge issueFaceChallenge(String scene,String orderId){
   String actor=TrustedActor.currentUserId();
   String channel="face_"+(blank(scene).isEmpty()?"generic":scene);
   if(channel.length()>32)throw new IllegalArgumentException("Face scene too long for channel");
   String code=String.format("%04d",RANDOM.nextInt(10000));
   String challengeId=id();
   inserted(mapper.insertChallenge(challengeId,actor,channel,code));
   return new FaceChallenge(challengeId,code);
 }
 @Transactional
 public String recordFaceVerification(String scene,String screenshotUrl,String deviceInfo,String orderId,String challengeCode){
   String actor=TrustedActor.currentUserId();
   if(blank(screenshotUrl).isEmpty())throw new IllegalArgumentException("缺少刷脸留证截图");
   if("purchase_confirm".equals(scene)){
     if(challengeCode==null||challengeCode.length()!=4)throw new IllegalArgumentException("缺少刷脸口令，请重新发起核验");
     String codeId=mapper.lockChallenge(actor,challengeCode);
     if(codeId==null)throw new IllegalArgumentException("刷脸口令不正确或已过期，请重新发起核验");
     inserted(mapper.markChallengeUsed(codeId));
   }
   String recordId=id();
   inserted(mapper.insertFaceRecord(recordId,actor,blank(scene).isEmpty()?"invite_share":scene,
        screenshotUrl,deviceInfo,orderId,challengeCode));
   return recordId;
 }
}
