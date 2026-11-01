package com.kuaixietech.service.account.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.identity.ContactAndPasswordRules;
import com.kuaixietech.mapper.account.AuthLoginMapper;
import com.kuaixietech.security.LocalHmacJwtService;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthLoginService {
 private com.kuaixietech.coupler.CouplerModeGate couplerMode;
 @org.springframework.beans.factory.annotation.Autowired public void couplerMode(com.kuaixietech.coupler.CouplerModeGate gate){this.couplerMode=gate;}

  private final AuthLoginMapper mapper;
  private final LocalHmacJwtService jwt;
  private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
  private final String bootstrapPassword;
  private final boolean allowBootstrap;
  private com.kuaixietech.integration.IntegrationConfiguration configuration;private com.kuaixietech.integration.IntegrationHttp transport;
  @org.springframework.beans.factory.annotation.Autowired public void integrationConfiguration(com.kuaixietech.integration.IntegrationConfiguration configuration,com.kuaixietech.integration.IntegrationHttp transport){this.configuration=configuration;this.transport=transport;}
  private final String wechatAppId;
  private final String wechatSecret;
  private final ObjectMapper json=new ObjectMapper();
  private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  public AuthLoginService(AuthLoginMapper mapper,LocalHmacJwtService jwt,
      @Value("${app.admin-bootstrap-password:}") String bootstrapPassword,
      @Value("${app.environment:dev}") String environment,
      @Value("${app.wechat.app-id:}") String wechatAppId,
      @Value("${app.wechat.app-secret:}") String wechatSecret){
    this.mapper=mapper;this.jwt=jwt;
    this.bootstrapPassword=bootstrapPassword==null?"":bootstrapPassword.trim();
    this.allowBootstrap=!this.bootstrapPassword.isEmpty() && ("dev".equalsIgnoreCase(environment)||"local".equalsIgnoreCase(environment));
    this.wechatAppId=wechatAppId==null?"":wechatAppId.trim();
    this.wechatSecret=wechatSecret==null?"":wechatSecret.trim();
  }

  @Transactional
  public Map<String,Object> login(String channel,String contact,String password,String countryCode){
    if(!jwt.available()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"LOCAL_JWT_SECRET_MISSING");
    if(!ContactAndPasswordRules.validChannel(channel)) throw bad("AUTH_BAD_CHANNEL");
    String country="phone".equals(channel)?ContactAndPasswordRules.normalizeCountry(countryCode):null;
    if("phone".equals(channel)&&country==null) throw bad("AUTH_BAD_CONTACT");
    String normalized="phone".equals(channel)?ContactAndPasswordRules.normalizePhone(contact,country):ContactAndPasswordRules.normalize(channel,contact);
    if(normalized==null) throw bad("AUTH_BAD_CONTACT");
    String secret=password==null?"":password.trim();
    boolean otp=secret.matches("\\d{6}");
    if(!otp && !ContactAndPasswordRules.validPassword(secret)) throw bad("AUTH_BAD_PASSWORD");
    Map<String,Object> profile=mapper.findProfile(channel,normalized,country);
    if(profile==null){
      if(otp) throw bad("AUTH_FAILED");
      profile=bootstrapFirstAdmin(channel,normalized,secret,country);
    }
    String userId=String.valueOf(profile.get("id"));
    if(col(profile,"banned_until","bannedUntil")!=null) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"AUTH_BANNED");
    if(otp){
      String codeId=mapper.lockCode(ContactAndPasswordRules.codeTarget(channel,normalized,country),channel,secret,"signin");
      if(codeId==null || mapper.consumeCode(codeId)!=1) throw bad("AUTH_FAILED");
    }else{
      String hash=mapper.passwordHash(userId);
      if(hash==null||hash.isBlank()){
        if(!allowBootstrap || !bootstrapPassword.equals(secret)) throw bad("AUTH_NO_PASSWORD");
        if(!isAdmin(profile) && mapper.adminCount()>0) throw bad("AUTH_NO_PASSWORD");
        mapper.savePassword(userId,encoder.encode(secret));
        if(!isAdmin(profile)) mapper.markAdmin(userId);
      }else if(!encoder.matches(secret,hash)){
        throw bad("AUTH_FAILED");
      }
    }
    Map<String,Object> latest=mapper.findProfile(channel,normalized,country);
    boolean admin=isAdmin(latest);
    String token=jwt.issue(userId);
    Map<String,Object> out=new LinkedHashMap<>();
    out.put("accessToken",token);
    out.put("token",token);
    out.put("userId",userId);
    out.put("isAdmin",admin);
    out.put("expiresIn",8*3600);
    if("phone".equals(channel)){
      out.put("phoneCountryCode",country);
      out.put("locale",ContactAndPasswordRules.localeForCountry(country));
    }
    return out;
  }

  @Transactional
  public Map<String,Object> wechat(String code){
  if(couplerMode!=null)couplerMode.requireLiveNewCall("wechat_login");
    if(!jwt.available()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"LOCAL_JWT_SECRET_MISSING");

    if(code==null||!code.matches("[A-Za-z0-9_-]{4,128}")) throw bad("WX_LOGIN_FAILED");
    JsonNode session=wechatSession(code);
    String openid=text(session,"openid");
    if(session.has("errcode")&&session.get("errcode").asInt()!=0) throw bad("WX_LOGIN_FAILED");
    if(openid==null||!openid.matches("[A-Za-z0-9_-]{8,64}")) throw bad("WX_LOGIN_FAILED");
    String unionid=text(session,"unionid");
    if(unionid!=null&&!unionid.matches("[A-Za-z0-9_-]{8,64}")) unionid=null;
    Map<String,Object> profile=mapper.findByOpenid(openid);
    if(profile==null){
      String id=UUID.randomUUID().toString();
      String username=("wx"+openid);
      if(username.length()>32) username=username.substring(0,32);
      mapper.insertWechat(id,username,"微信用户",openid,unionid);
      profile=mapper.findByOpenid(openid);
      if(profile==null) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"AUTH_PROFILE_CREATE_FAILED");
    }
    if(col(profile,"banned_until","bannedUntil")!=null) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"AUTH_BANNED");
    String userId=String.valueOf(profile.get("id"));
    String token=jwt.issue(userId);
    Map<String,Object> out=new LinkedHashMap<>();
    out.put("accessToken",token);
    out.put("token",token);
    out.put("userId",userId);
    out.put("isAdmin",isAdmin(profile));
    out.put("expiresIn",8*3600);
    return out;
  }

  private JsonNode wechatSession(String code){
    String appId=wechatAppId,secret=wechatSecret;
    if(configuration!=null&&configuration.exists("wechat","miniprogram")){var fields=configuration.runtime("wechat","miniprogram");if(fields==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"WX_CONFIG_MISSING");appId=fields.get("app_id");secret=fields.get("app_secret");}
    if(appId.isBlank()||secret.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"WX_CONFIG_MISSING");
    String url="https://api.weixin.qq.com/sns/jscode2session?appid="+encode(appId)
        +"&secret="+encode(secret)+"&js_code="+encode(code)+"&grant_type=authorization_code";
    try{
      HttpRequest request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).GET().build();
      if(transport!=null)return json.readTree(transport.send(request));
      HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()!=200) throw bad("WX_LOGIN_FAILED");
      return json.readTree(response.body());
    }catch(ResponseStatusException ex){
      throw ex;
    }catch(Exception ex){
      throw bad("WX_LOGIN_FAILED");
    }
  }
  private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
  private static String text(JsonNode node,String field){
    JsonNode value=node.get(field);
    if(value==null||value.isNull())return null;
    String text=value.asText();
    return text==null||text.isBlank()?null:text;
  }

  private Map<String,Object> bootstrapFirstAdmin(String channel,String contact,String password,String country){
    if(!allowBootstrap || !bootstrapPassword.equals(password)) throw bad("AUTH_FAILED");
    if(mapper.adminCount()>0) throw bad("AUTH_FAILED");
    String id=UUID.randomUUID().toString();
    String phone="phone".equals(channel)?contact:null;
    String email="email".equals(channel)?contact:null;
    String username=contact.length()>32?contact.substring(0,32):contact;
    mapper.insertAdmin(id,username,"运营",phone,email,"phone".equals(channel)?country:null);
    mapper.savePassword(id,encoder.encode(password));
    Map<String,Object> created=mapper.findProfile(channel,contact,"phone".equals(channel)?country:null);
    if(created==null) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"AUTH_PROFILE_CREATE_FAILED");
    return created;
  }

  private static boolean isAdmin(Map<String,Object> row){
    Object v=col(row,"is_admin","isAdmin");
    return Boolean.TRUE.equals(v)||Integer.valueOf(1).equals(v)||"1".equals(String.valueOf(v))||"true".equalsIgnoreCase(String.valueOf(v));
  }
  private static Object col(Map<String,Object> row,String snake,String camel){
    if(row==null)return null;
    Object v=row.get(snake);
    return v!=null?v:row.get(camel);
  }
  private static ResponseStatusException bad(String code){return new ResponseStatusException(HttpStatus.UNAUTHORIZED,code);}
}
