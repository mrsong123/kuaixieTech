package com.kuaixietech.service.account.impl;

import com.kuaixietech.identity.ContactAndPasswordRules;
import com.kuaixietech.mapper.account.AuthLoginMapper;
import com.kuaixietech.security.LocalHmacJwtService;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthRecoveryService {
  private static final String LICENSE="[0-9A-HJ-NPQRTUWXY]{18}";
  private final AuthLoginMapper mapper;
  private final LocalHmacJwtService jwt;
  private final AuthCaptchaService captcha;
  private final JdbcTemplate jdbc;
  private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
  private final SecureRandom random=new SecureRandom();

  public AuthRecoveryService(AuthLoginMapper mapper,LocalHmacJwtService jwt,AuthCaptchaService captcha,JdbcTemplate jdbc){
    this.mapper=mapper;this.jwt=jwt;this.captcha=captcha;this.jdbc=jdbc;
  }

  public Map<String,String> captcha(){return captcha.issue();}

  @Transactional
  public Map<String,Object> sendCode(String channel,String contact,String countryCode,String purpose,String captchaId,String captchaCode){
    captcha.consume(captchaId,captchaCode);
    String use=purpose(purpose);
    Contact contactValue=requireContact(channel,contact,countryCode);
    Map<String,Object> profile=mapper.findProfile(contactValue.channel(),contactValue.normalized(),contactValue.country());
    if("signup".equals(use)&&profile!=null) throw fail(HttpStatus.CONFLICT,"AUTH_CONTACT_TAKEN");
    if(!"signup".equals(use)&&profile==null) return Map.of("status","OK","retryAfter",60);
    String target=ContactAndPasswordRules.codeTarget(contactValue.channel(),contactValue.normalized(),contactValue.country());
    if(mapper.recentCodeCount(target,contactValue.channel())>0) throw fail(HttpStatus.TOO_MANY_REQUESTS,"CODE_TOO_SOON");
    String code=String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));
    mapper.insertCode(UUID.randomUUID().toString(),target,contactValue.channel(),code,use);
    mapper.enqueueCode("phone".equals(contactValue.channel())?contactValue.country()+contactValue.normalized():contactValue.normalized(),contactValue.channel(),code);
    return Map.of("status","OK","retryAfter",60);
  }

  @Transactional
  public Map<String,Object> resetPassword(String channel,String contact,String countryCode,String code,String newPassword){
    if(!jwt.available()) throw fail(HttpStatus.SERVICE_UNAVAILABLE,"LOCAL_JWT_SECRET_MISSING");
    if(newPassword==null||!ContactAndPasswordRules.validPassword(newPassword.trim())) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_PASSWORD");
    Contact contactValue=requireContact(channel,contact,countryCode);
    Map<String,Object> profile=mapper.findProfile(contactValue.channel(),contactValue.normalized(),contactValue.country());
    String codeId=profile==null?null:mapper.lockCode(ContactAndPasswordRules.codeTarget(contactValue.channel(),contactValue.normalized(),contactValue.country()),contactValue.channel(),code==null?"":code.trim(),"reset");
    if(profile==null||codeId==null||mapper.consumeCode(codeId)!=1) throw fail(HttpStatus.UNAUTHORIZED,"AUTH_FAILED");
    String userId=String.valueOf(profile.get("id"));
    mapper.savePassword(userId,encoder.encode(newPassword.trim()));
    return session(userId,profile,contactValue);
  }

  @Transactional
  public Map<String,Object> register(String channel,String contact,String countryCode,String password,String code,String role,String inviteCode,String enterpriseName,String businessLicenseNo){
    if(!jwt.available()) throw fail(HttpStatus.SERVICE_UNAVAILABLE,"LOCAL_JWT_SECRET_MISSING");
    if(password==null||!ContactAndPasswordRules.validPassword(password.trim())) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_PASSWORD");
    String useRole=role==null?"":role.trim();
    if(!"buyer".equals(useRole)&&!"carrier".equals(useRole)&&!"enterprise".equals(useRole)) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_ROLE");
    Contact contactValue=requireContact(channel,contact,countryCode);
    if(mapper.findProfile(contactValue.channel(),contactValue.normalized(),contactValue.country())!=null) throw fail(HttpStatus.CONFLICT,"AUTH_CONTACT_TAKEN");
    String name=null;
    String license=null;
    if("enterprise".equals(useRole)){
      name=enterpriseName==null?"":enterpriseName.trim();
      license=businessLicenseNo==null?"":businessLicenseNo.trim().toUpperCase(Locale.ROOT);
      if(name.length()<2||name.length()>80) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_ENTERPRISE");
      if(!license.matches(LICENSE)) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_LICENSE");
    }
    String invite=inviteCode==null?"":inviteCode.trim();
    if(!invite.isEmpty()&&!invite.matches("[A-Za-z0-9_-]{1,40}")) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_INVITE");
    String codeId=mapper.lockCode(ContactAndPasswordRules.codeTarget(contactValue.channel(),contactValue.normalized(),contactValue.country()),contactValue.channel(),code==null?"":code.trim(),"signup");
    if(codeId==null||mapper.consumeCode(codeId)!=1) throw fail(HttpStatus.UNAUTHORIZED,"AUTH_FAILED");
    String id=UUID.randomUUID().toString();
    String username="u"+id.replace("-","").substring(0,31);
    String nickname="enterprise".equals(useRole)?name:"用户";
    if(nickname.length()>32) nickname=nickname.substring(0,32);
    try{
      mapper.insertUser(id,username,nickname,
          "phone".equals(contactValue.channel())?contactValue.normalized():null,
          "email".equals(contactValue.channel())?contactValue.normalized():null,
          contactValue.country(),useRole,name,license,invite.isEmpty()?null:invite);
    }catch(DuplicateKeyException ex){
      throw fail(HttpStatus.CONFLICT,"AUTH_CONTACT_TAKEN");
    }
    mapper.savePassword(id,encoder.encode(password.trim()));
    Map<String,Object> profile=mapper.findProfile(contactValue.channel(),contactValue.normalized(),contactValue.country());
    if(profile==null) throw fail(HttpStatus.INTERNAL_SERVER_ERROR,"AUTH_PROFILE_CREATE_FAILED");
    return session(id,profile,contactValue);
  }

  public Map<String,Object> recoveryHelp(String channel,String contact,String countryCode,String captchaId,String captchaCode){
    captcha.consume(captchaId,captchaCode);
    Contact contactValue=requireContact(channel,contact,countryCode);
    Map<String,Object> profile=mapper.findProfile(contactValue.channel(),contactValue.normalized(),contactValue.country());
    if(profile==null) return Map.of("status","OK");
    String userId=String.valueOf(profile.get("id"));
    try{
      Integer waiting=jdbc.queryForObject("SELECT COUNT(*) FROM t_support_conversations WHERE user_id=? AND status='waiting'",Integer.class,userId);
      if(waiting!=null&&waiting>0) return Map.of("status","OK");
      String id=UUID.randomUUID().toString();
      jdbc.update("INSERT INTO t_support_conversations(id,user_id,status) VALUES(?,?,'waiting')",id,userId);
      jdbc.update("INSERT INTO t_support_conversation_messages(conversation_id,sender_type,sender_id,content) VALUES(?,'user',?,?)",id,userId,"忘记密码，且收不到验证码，请求人工协助找回。");
    }catch(DataAccessException ex){
      throw fail(HttpStatus.SERVICE_UNAVAILABLE,"SUPPORT_UNAVAILABLE");
    }
    return Map.of("status","OK");
  }

  private Map<String,Object> session(String userId,Map<String,Object> profile,Contact contactValue){
    String token=jwt.issue(userId);
    Map<String,Object> out=new LinkedHashMap<>();
    out.put("accessToken",token);
    out.put("token",token);
    out.put("userId",userId);
    out.put("isAdmin",false);
    out.put("expiresIn",8*3600);
    if(profile.get("is_admin")!=null||profile.get("isAdmin")!=null){
      Object admin=profile.get("is_admin")!=null?profile.get("is_admin"):profile.get("isAdmin");
      out.put("isAdmin",Boolean.TRUE.equals(admin)||"1".equals(String.valueOf(admin))||"true".equalsIgnoreCase(String.valueOf(admin)));
    }
    if("phone".equals(contactValue.channel())){
      out.put("phoneCountryCode",contactValue.country());
      out.put("locale",ContactAndPasswordRules.localeForCountry(contactValue.country()));
    }
    return out;
  }

  private Contact requireContact(String channel,String contact,String countryCode){
    if(!ContactAndPasswordRules.validChannel(channel)) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_CHANNEL");
    String country="phone".equals(channel)?ContactAndPasswordRules.normalizeCountry(countryCode):null;
    if("phone".equals(channel)&&country==null) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_CONTACT");
    String normalized="phone".equals(channel)?ContactAndPasswordRules.normalizePhone(contact,country):ContactAndPasswordRules.normalize(channel,contact);
    if(normalized==null) throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_CONTACT");
    return new Contact(channel,normalized,country);
  }

  private static String purpose(String value){
    if("signin".equals(value)||"signup".equals(value)||"reset".equals(value)) return value;
    throw fail(HttpStatus.BAD_REQUEST,"AUTH_BAD_PURPOSE");
  }

  private static ResponseStatusException fail(HttpStatus status,String code){return new ResponseStatusException(status,code);}

  private record Contact(String channel,String normalized,String country){}
}
