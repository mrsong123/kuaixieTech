package com.kuaixietech.service.account.impl;

import com.kuaixietech.identity.ContactAndPasswordRules;
import com.kuaixietech.mapper.account.AuthLoginMapper;
import com.kuaixietech.security.LocalHmacJwtService;
import jakarta.annotation.PostConstruct;
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
  private final AuthLoginMapper mapper;
  private final LocalHmacJwtService jwt;
  private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
  private final String bootstrapPassword;
  private final boolean allowBootstrap;
  public AuthLoginService(AuthLoginMapper mapper,LocalHmacJwtService jwt,
      @Value("${app.admin-bootstrap-password:}") String bootstrapPassword,
      @Value("${app.environment:dev}") String environment){
    this.mapper=mapper;this.jwt=jwt;
    this.bootstrapPassword=bootstrapPassword==null?"":bootstrapPassword.trim();
    this.allowBootstrap=!this.bootstrapPassword.isEmpty() && ("dev".equalsIgnoreCase(environment)||"local".equalsIgnoreCase(environment));
  }
  @PostConstruct public void ready(){mapper.ensureCredentialTable();}

  @Transactional
  public Map<String,Object> login(String channel,String contact,String password){
    if(!jwt.available()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"LOCAL_JWT_SECRET_MISSING");
    if(!ContactAndPasswordRules.validChannel(channel)) throw bad("AUTH_BAD_CHANNEL");
    String normalized=ContactAndPasswordRules.normalize(channel,contact);
    if(normalized==null) throw bad("AUTH_BAD_CONTACT");
    if(!ContactAndPasswordRules.validPassword(password)) throw bad("AUTH_BAD_PASSWORD");
    Map<String,Object> profile=mapper.findProfile(channel,normalized);
    if(profile==null){
      profile=bootstrapFirstAdmin(channel,normalized,password);
    }
    String userId=String.valueOf(profile.get("id"));
    if(col(profile,"banned_until","bannedUntil")!=null) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"AUTH_BANNED");
    String hash=mapper.passwordHash(userId);
    if(hash==null||hash.isBlank()){
      if(!allowBootstrap || !bootstrapPassword.equals(password)) throw bad("AUTH_NO_PASSWORD");
      if(!isAdmin(profile) && mapper.adminCount()>0) throw bad("AUTH_NO_PASSWORD");
      mapper.savePassword(userId,encoder.encode(password));
      if(!isAdmin(profile)) mapper.markAdmin(userId);
    }else if(!encoder.matches(password,hash)){
      throw bad("AUTH_FAILED");
    }
    Map<String,Object> latest=mapper.findProfile(channel,normalized);
    boolean admin=isAdmin(latest);
    String token=jwt.issue(userId);
    Map<String,Object> out=new LinkedHashMap<>();
    out.put("accessToken",token);
    out.put("token",token);
    out.put("userId",userId);
    out.put("isAdmin",admin);
    out.put("expiresIn",8*3600);
    return out;
  }

  private Map<String,Object> bootstrapFirstAdmin(String channel,String contact,String password){
    if(!allowBootstrap || !bootstrapPassword.equals(password)) throw bad("AUTH_FAILED");
    if(mapper.adminCount()>0) throw bad("AUTH_FAILED");
    String id=UUID.randomUUID().toString();
    String phone="phone".equals(channel)?contact:null;
    String email="email".equals(channel)?contact:null;
    String username=contact.length()>32?contact.substring(0,32):contact;
    mapper.insertAdmin(id,username,"运营",phone,email);
    mapper.savePassword(id,encoder.encode(password));
    Map<String,Object> created=mapper.findProfile(channel,contact);
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
