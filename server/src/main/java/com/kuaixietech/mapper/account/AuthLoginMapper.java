package com.kuaixietech.mapper.account;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthLoginMapper {
  void ensureCredentialTable();
  void ensureCountryColumn();
  Map<String,Object> findProfile(@Param("channel") String channel,@Param("contact") String contact,@Param("country") String country);
  int rememberCountry(@Param("userId") String userId,@Param("country") String country);
  Map<String,Object> findByOpenid(@Param("openid") String openid);
  int insertWechat(@Param("id") String id,@Param("username") String username,@Param("nickname") String nickname,@Param("openid") String openid,@Param("unionid") String unionid);
  String passwordHash(@Param("userId") String userId);
  int savePassword(@Param("userId") String userId,@Param("hash") String hash);
  int adminCount();
  int markAdmin(@Param("userId") String userId);
  int insertAdmin(@Param("id") String id,@Param("username") String username,@Param("nickname") String nickname,
      @Param("phone") String phone,@Param("email") String email,@Param("country") String country);
  int recentCodeCount(@Param("target") String target,@Param("channel") String channel);
  int insertCode(@Param("id") String id,@Param("target") String target,@Param("channel") String channel,@Param("code") String code,@Param("purpose") String purpose);
  int enqueueCode(@Param("target") String target,@Param("channel") String channel,@Param("code") String code);
  String lockCode(@Param("target") String target,@Param("channel") String channel,@Param("code") String code,@Param("purpose") String purpose);
  int consumeCode(@Param("id") String id);
  int insertUser(@Param("id") String id,@Param("username") String username,@Param("nickname") String nickname,
      @Param("phone") String phone,@Param("email") String email,@Param("country") String country,@Param("role") String role,
      @Param("enterpriseName") String enterpriseName,@Param("licenseNo") String licenseNo,@Param("inviteCode") String inviteCode);
}
