package com.kuaixietech.mapper.growth;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface InviteReadRpcMapper {
 List<Map<String,Object>> teamMembers(@Param("root")String root);
 String inviteCodeOwner(@Param("code")String code,@Param("actor")String actor);
 String operatorInviteCode(@Param("operator")String operator,@Param("actor")String actor);
 int isAdmin(@Param("actor")String actor);
}
