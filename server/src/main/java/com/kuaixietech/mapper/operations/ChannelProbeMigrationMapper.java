package com.kuaixietech.mapper.operations;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface ChannelProbeMigrationMapper { int setProbe(@Param("channel") String channel,@Param("ready") boolean ready); }
