package com.kuaixietech.mapper.account;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountCentreMapper {
  Map<String,Object> profile(@Param("user") String user);
  int updateProfile(@Param("user") String user,@Param("nickname") String nickname,@Param("city") String city);
  List<Map<String,Object>> orders(@Param("user") String user,@Param("role") String role,@Param("limit") int limit,@Param("offset") int offset);
  Map<String,Object> order(@Param("user") String user,@Param("id") String id);
  List<Map<String,Object>> demands(@Param("user") String user,@Param("limit") int limit,@Param("offset") int offset);
  List<Map<String,Object>> trips(@Param("user") String user,@Param("limit") int limit,@Param("offset") int offset);
  Map<String,Object> wallet(@Param("user") String user);
  List<Map<String,Object>> ledger(@Param("user") String user,@Param("limit") int limit,@Param("offset") int offset);
  List<Map<String,Object>> notifications(@Param("user") String user,@Param("limit") int limit,@Param("offset") int offset);
  int readNotification(@Param("user") String user,@Param("id") String id);
  int ownsNotification(@Param("user") String user,@Param("id") String id);
}
