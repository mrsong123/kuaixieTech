package com.kuaixietech.mapper.account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ContactCodeVerificationMapper {
 int consumeLatestValid(@Param("target") String target,@Param("channel") String channel,@Param("code") String code);
}
