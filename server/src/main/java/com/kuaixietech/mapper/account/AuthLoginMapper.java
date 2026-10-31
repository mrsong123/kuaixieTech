package com.kuaixietech.mapper.account;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthLoginMapper {
  void ensureCredentialTable();
  Map<String,Object> findProfile(@Param("channel") String channel,@Param("contact") String contact);
  String passwordHash(@Param("userId") String userId);
  int savePassword(@Param("userId") String userId,@Param("hash") String hash);
  int adminCount();
  int markAdmin(@Param("userId") String userId);
  int insertAdmin(@Param("id") String id,@Param("username") String username,@Param("nickname") String nickname,
      @Param("phone") String phone,@Param("email") String email);
}
