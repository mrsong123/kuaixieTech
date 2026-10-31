package com.kuaixietech.service.communication;
import java.util.*;
public interface SupportChatService{
 Map<String,Object> open();
 Map<String,Object> get(String id);
 List<Map<String,Object>> messages(String id,int limit);
 Map<String,Object> send(String id,String content,String locale);
 Map<String,Object> handoff(String id);
 List<Map<String,Object>> queue();
 List<Map<String,Object>> agentMessages(String id,int limit);
 Map<String,Object> claim(String id);
 Map<String,Object> agentReply(String id,String content);
}
