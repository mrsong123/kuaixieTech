package com.kuaixietech.mapper.account;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IdentitySettingsMapper {
    Map<String,Object> lockProfile(@Param("userId") String userId);
    String lockValidCode(@Param("target") String target,@Param("channel") String channel,@Param("code") String code);
    int consumeLockedCode(@Param("codeId") String codeId);
    int countContactTaken(@Param("userId") String userId,@Param("channel") String channel,@Param("target") String target);
    int updateContact(@Param("userId") String userId,@Param("channel") String channel,@Param("target") String target);
    String findUserByContact(@Param("channel") String channel,@Param("target") String target);
}
