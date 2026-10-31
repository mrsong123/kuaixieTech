package com.kuaixietech.service.communication.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.kuaixietech.integration.LegacyCloudFunctionFacade;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.communication.SupportChatService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service public class SupportChatServiceImpl implements SupportChatService {
 private final JdbcTemplate jdbc;private final DomainAccessGuard guard;private final LegacyCloudFunctionFacade ai;
 public SupportChatServiceImpl(JdbcTemplate jdbc,DomainAccessGuard guard,LegacyCloudFunctionFacade ai){this.jdbc=jdbc;this.guard=guard;this.ai=ai;}
 private Map<String,Object> conversation(String id,boolean admin){
  String uid=admin?guard.requireAdmin():guard.requireActiveProfile();
  List<Map<String,Object>> records=jdbc.queryForList("SELECT id,user_id,status,assigned_agent_id,created_at,updated_at FROM kx_support_conversations WHERE id=? AND (?=1 OR user_id=?)",id,admin?1:0,uid);
  if(records.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"support conversation not found");return records.getFirst();
 }
 public Map<String,Object> open(){
  String uid=guard.requireActiveProfile();
  List<Map<String,Object>> records=jdbc.queryForList("SELECT id,user_id,status,assigned_agent_id,created_at,updated_at FROM kx_support_conversations WHERE user_id=? AND status IN ('ai','waiting','human') ORDER BY created_at DESC LIMIT 1",uid);
  if(!records.isEmpty())return records.getFirst();
  String id=UUID.randomUUID().toString();jdbc.update("INSERT INTO kx_support_conversations(id,user_id,status) VALUES(?,?,'ai')",id,uid);
  return conversation(id,false);
 }
 public Map<String,Object> get(String id){return conversation(id,false);}
 public List<Map<String,Object>> messages(String id,int limit){conversation(id,false);
  if(limit<1||limit>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"limit must be 1..100");
  List<Map<String,Object>> rows=jdbc.queryForList("SELECT id,sender_type,content,created_at FROM kx_support_messages WHERE conversation_id=? ORDER BY id DESC LIMIT ?",id,limit);
  Collections.reverse(rows);return rows;
 }
 private boolean isHandoff(String content){String s=content.trim().toLowerCase(Locale.ROOT);return Set.of("转人工","人工客服","真人客服","human","human agent","agent","transfer to human").contains(s);}
 public Map<String,Object> send(String id,String content,String locale){
  String user=guard.requireActiveProfile();Map<String,Object> session=conversation(id,false);
  String text=content==null?"":content.trim();if(text.isEmpty()||text.length()>2000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"message length must be 1..2000");
  jdbc.update("INSERT INTO kx_support_messages(conversation_id,sender_type,sender_id,content) VALUES(?,'user',?,?)",id,user,text);
  String state=String.valueOf(session.get("status"));
  if(isHandoff(text))return handoff(id);
  if(!"ai".equals(state))return Map.of("status",state,"aiStatus","not_requested");
  try{
   JsonNode response=ai.supportAiReply(Map.of("conversationId",id,"message",text,"locale",locale==null?"zh-CN":locale));
   // The upstream reply is never synthesized. Parse only documented text fields, no free-form JSON dump.
   JsonNode candidate=response==null?null:response.path("reply");
   if(candidate==null||!candidate.isTextual())candidate=response==null?null:response.path("content");
   String answer=candidate!=null&&candidate.isTextual()?candidate.asText().trim():"";
   if(answer.isBlank()||answer.length()>5000)return Map.of("status","ai","aiStatus","unavailable");
   jdbc.update("INSERT INTO kx_support_messages(conversation_id,sender_type,content) VALUES(?,'ai',?)",id,answer);
   return Map.of("status","ai","aiStatus","responded");
  }catch(Exception e){
   // Persist user's real message even if AI provider fails. Do not fabricate an assistant reply.
   return Map.of("status","ai","aiStatus","unavailable");
  }
 }
 @Transactional public Map<String,Object> handoff(String id){
  conversation(id,false);
  jdbc.update("UPDATE kx_support_conversations SET status='waiting',updated_at=NOW() WHERE id=? AND status='ai'",id);
  return conversation(id,false);
 }
 public List<Map<String,Object>> agentMessages(String id,int limit){
  conversation(id,true);
  if(limit<1||limit>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"limit must be 1..100");
  List<Map<String,Object>> rows=jdbc.queryForList("SELECT id,sender_type,content,created_at FROM kx_support_messages WHERE conversation_id=? ORDER BY id DESC LIMIT ?",id,limit);
  Collections.reverse(rows);return rows;
 }
 public List<Map<String,Object>> queue(){guard.requireAdmin();return jdbc.queryForList("SELECT id,user_id,status,created_at FROM kx_support_conversations WHERE status='waiting' ORDER BY created_at LIMIT 100");}
 @Transactional public Map<String,Object> claim(String id){String admin=guard.requireAdmin();
  int count=jdbc.update("UPDATE kx_support_conversations SET status='human',assigned_agent_id=?,updated_at=NOW() WHERE id=? AND status='waiting'",admin,id);
  if(count!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"conversation already claimed");return conversation(id,true);
 }
 @Transactional public Map<String,Object> agentReply(String id,String content){String admin=guard.requireAdmin();Map<String,Object> session=conversation(id,true);
  if(!"human".equals(session.get("status"))||!admin.equals(session.get("assigned_agent_id")))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"agent has not claimed conversation");
  String text=content==null?"":content.trim();if(text.isEmpty()||text.length()>2000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"message length must be 1..2000");
  jdbc.update("INSERT INTO kx_support_messages(conversation_id,sender_type,sender_id,content) VALUES(?,'agent',?,?)",id,admin,text);
  return Map.of("status","human");
 }
}
