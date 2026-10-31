package com.kuaixietech.mapper.account;
import java.util.Map;
import org.apache.ibatis.annotations.*;
@Mapper public interface IdentityCodesRpcMapper {
 Map<String,Object> lockValidCode(@Param("target")String target,@Param("channel")String channel,@Param("code")String code);
 int markCodeUsed(@Param("id")String id);
 int updateVerifiedPhone(@Param("actor")String actor,@Param("target")String target);
 int updateVerifiedEmail(@Param("actor")String actor,@Param("target")String target);
 int consumeInvite(@Param("code")String code,@Param("actor")String actor);
}
