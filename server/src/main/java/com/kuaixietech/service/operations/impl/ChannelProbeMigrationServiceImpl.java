package com.kuaixietech.service.operations.impl;
import com.kuaixietech.mapper.operations.ChannelProbeMigrationMapper;
import com.kuaixietech.service.operations.ChannelProbeMigrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
@Service public class ChannelProbeMigrationServiceImpl implements ChannelProbeMigrationService {
 private final ChannelProbeMigrationMapper mapper;
 public ChannelProbeMigrationServiceImpl(ChannelProbeMigrationMapper mapper){this.mapper=mapper;}
 // Supabase source granted set_channel_probe exclusively to service_role. Do not expose via HTTP.
 @Override @Transactional public String setProbe(String channel,boolean ready){
   if(channel==null||!Set.of("wechat_subscribe","sms","email","ai_call").contains(channel))throw new IllegalArgumentException("unsupported channel");
   mapper.setProbe(channel,ready);return "OK";
 }
}
