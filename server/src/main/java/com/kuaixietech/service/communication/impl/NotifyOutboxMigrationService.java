package com.kuaixietech.service.communication.impl;
import com.kuaixietech.mapper.communication.NotifyOutboxMigrationMapper;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Ports flush_notify_outbox; external channels stay pending unless provisioned, never marked 'sent' by configuration alone. */
@Service public class NotifyOutboxMigrationService {
 private final NotifyOutboxMigrationMapper m;public NotifyOutboxMigrationService(NotifyOutboxMigrationMapper m){this.m=m;}
 @Transactional public String flushNotifyOutbox(){int moved=0,blocked=0,kept=0;
  for(Map<String,Object> row:m.pendingInapp()){
   String id=String.valueOf(row.get("id")),uid=String.valueOf(row.get("user_id"));
   if(m.insertInapp(UUID.randomUUID().toString(),uid,String.valueOf(row.get("kind")),String.valueOf(row.get("title")),row.get("body")==null?null:String.valueOf(row.get("body")))!=1)throw new IllegalStateException("Missing inapp notice");
   if(m.markSent(id)!=1)throw new IllegalStateException("Outbox transition raced");moved++;
  }
  for(Map<String,Object> row:m.pendingExternal()){
   String channel=String.valueOf(row.get("channel"));String mapped=switch(channel){case "wechat_subscribe"->"wx_subscribe";case "ai_call"->"sms";default->channel;};
   if(m.readyProvider(channel,mapped)>0)kept++;else{if(m.block(String.valueOf(row.get("id")))!=1)throw new IllegalStateException("Outbox transition raced");blocked++;}
  }
  return "OK:"+moved+":"+blocked+":"+kept;
 }
}
