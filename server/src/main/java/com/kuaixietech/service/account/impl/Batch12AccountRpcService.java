package com.kuaixietech.service.account.impl;
import com.kuaixietech.mapper.account.Batch12AccountMapper;
import com.kuaixietech.security.TrustedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;import java.security.*;import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
/** Enterprise/carrier KYC and invitation matcher. No Supabase auth.users dependency. */
@Service public class Batch12AccountRpcService {
 private final Batch12AccountMapper m;private final com.kuaixietech.security.DomainAccessGuard guard;private final ObjectMapper json;private final SecureRandom random=new SecureRandom();
 public Batch12AccountRpcService(Batch12AccountMapper m,ObjectMapper json,com.kuaixietech.security.DomainAccessGuard guard){this.m=m;this.json=json;this.guard=guard;}
 private static String s(Object o){return o==null?null:o.toString();}
 @Transactional public String submitEnterpriseVerify(String name,String license,List<String> urls){
  String actor=TrustedActor.currentUserId();Map<String,Object> p=m.lockProfile(actor);
  if(p==null||!"enterprise".equals(s(p.get("user_role"))))return "ERR:NOT_ENTERPRISE";
  if(name==null||name.trim().isEmpty())return "ERR:NO_NAME";
  if(license==null||!license.trim().toUpperCase(Locale.ROOT).matches("[0-9A-HJ-NPQRTUWXY]{18}"))return "ERR:BAD_LICENSE";
  if(urls==null||urls.isEmpty()||urls.stream().anyMatch(x->x==null||x.isBlank()))return "ERR:NO_PROOF";
  if(m.updateEnterprise(actor,name.trim(),license.trim().toUpperCase(Locale.ROOT))!=1)throw new IllegalStateException("enterprise KYC update failed");
  m.deleteLicense(actor);m.deleteAttachments(actor);
  m.createLicense(UUID.randomUUID().toString(),actor,urls.get(0));
  for(int i=0;i<urls.size();i++)m.addAttachment(UUID.randomUUID().toString(),actor,urls.get(i),i);
  m.notify(UUID.randomUUID().toString(),actor,"企业认证已提交","营业执照与主体信息已进入人工审核。");return "OK";
 }
 @Transactional public boolean submitCarrierKyc(String name,String idCard,String passport,String code,String invite,String note){
  String actor=TrustedActor.currentUserId();if(name==null||name.trim().length()<2)throw new IllegalArgumentException("请填写真实姓名");
  if(idCard==null||!idCard.matches("[0-9]{17}[0-9Xx]"))throw new IllegalArgumentException("身份证号格式不正确（18 位）");
  if(passport==null||passport.trim().length()<5)throw new IllegalArgumentException("请填写护照号");
  Map<String,Object> p=m.lockProfile(actor);if(p==null)throw new IllegalArgumentException("账号资料未就绪，请稍后重试");
  if(s(p.get("phone"))==null||s(p.get("phone")).isEmpty())throw new IllegalArgumentException("请先完成手机号验证");
  if(!"1".equals(s(p.get("phone_verified")))&&!"true".equalsIgnoreCase(s(p.get("phone_verified"))))throw new IllegalArgumentException("手机号尚未通过验证码校验");
  if(m.consumeCode(s(p.get("phone")),code)!=1)throw new IllegalArgumentException("手机验证码不正确或已过期");
  if(invite!=null&&!invite.trim().isEmpty()&&m.consumeInvite(invite.trim(),actor)!=1)throw new IllegalArgumentException("邀请码无效或已被使用");
  if(m.carrierDocs(actor)<4)throw new IllegalArgumentException("请完整上传身份证正面、反面、护照信息页与手持身份证照片");
  String card=idCard.substring(0,4)+"**********"+idCard.substring(14);
  String digest;try{byte[] hash=MessageDigest.getInstance("MD5").digest(passport.getBytes(StandardCharsets.UTF_8));digest=HexFormat.of().formatHex(hash);}
  catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
  if(m.carrierPending(actor,name.trim(),card,"PASS-"+digest.substring(digest.length()-8),"KYC:"+(note==null?"":note))!=1)throw new IllegalStateException("carrier update failed");
  m.carrierDocsReset(actor);return true;
 }
 @Transactional public boolean matchJoinApplication(String appId){
  // Final legacy RPC had no admin guard; preserving operational utility but hardening: only admins can match pending invitations.
  // Do not resolve against Supabase auth.users (not present). Match canonical t_profiles.username.
  guard.requireAdmin();
  Map<String,Object> a=m.lockJoin(appId);if(a==null||a.get("target_user_id")!=null)return false;
  String user=m.lookupByUsername(s(a.get("target_username")));if(user==null)return false;
  if(m.bindJoin(appId,user)!=1)return false;
  m.notify(UUID.randomUUID().toString(),user,"收到加入邀请","平台运营邀请你加入，请前往「我的-加入申请」确认");return true;
 }
 @Transactional public String requestVerificationCode(String target,String channel){
  if(!"phone".equals(channel)&&!"email".equals(channel))throw new IllegalArgumentException("不支持的验证渠道");
  if(target==null||("phone".equals(channel)&&!target.matches("1[0-9]{10}|\\+[1-9][0-9]{0,2}[0-9]{5,15}"))||("email".equals(channel)&&!target.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")))throw new IllegalArgumentException("验证目标格式不正确");
  if(m.recentCodeCount(target,channel)>0)throw new IllegalArgumentException("请 60 秒后再重新获取验证码");
  String code=String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));
  m.newCode(UUID.randomUUID().toString(),target,channel,code);m.enqueueCode(target,channel,code);
  // Code only enters server-side outbox. Never return it in response or logs.
  return "OK";
 }
}
