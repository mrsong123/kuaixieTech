package com.kuaixietech.service.operations.impl;
import com.kuaixietech.service.operations.ChannelConfigMigrationService;
import com.kuaixietech.mapper.operations.ChannelConfigMigrationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class ChannelConfigMigrationServiceImpl implements ChannelConfigMigrationService {
 private static final Set<String> CHANNELS=Set.of("sms","email","wx_subscribe","privacy_number","logistics","payment","ai","rtc");
 private final ChannelConfigMigrationMapper mapper;
 private final ObjectMapper json=new ObjectMapper();
 public ChannelConfigMigrationServiceImpl(ChannelConfigMigrationMapper mapper){this.mapper=mapper;}
 private boolean admin(String actor){return actor!=null&&mapper.isAdmin(actor)>0;}
 private void log(String actor,String action,String type,String target,String detail){mapper.log(UUID.randomUUID().toString(),actor,action,type,target,detail);}
 @Override @Transactional public String upsert(String actor,String type,String provider,String configJson,boolean enabled,String remark){
  if(actor==null)return "ERR:NO_AUTH";
  if(!admin(actor))return "ERR:NOT_ADMIN";
  if(type==null||!CHANNELS.contains(type))return "ERR:BAD_CHANNEL";
  // Reject malformed JSON instead of injecting unescaped SQL/keys.
  String config=configJson==null?"{}":configJson;
  try { if(!json.readTree(config).isObject())throw new IllegalArgumentException("config must be object"); }
  catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalArgumentException("invalid JSON config",e);}
  if(provider==null||provider.isBlank())throw new IllegalArgumentException("provider required");
  // Serializes updates to existing rows of the same type; MySQL multi-instance coordination requires integration test.
  mapper.lockByType(type);
  if(enabled)mapper.disableOther(type,provider);
  mapper.upsert(UUID.randomUUID().toString(),type,provider,config,enabled,remark,actor);
  log(actor,"upsert_channel_config",type,provider,remark==null?"":remark);return "OK";
 }
 @Override @Transactional public String toggle(String actor,String id,boolean enabled){
  if(actor==null)return "ERR:NO_AUTH";
  if(!admin(actor))return "ERR:NOT_ADMIN";
  Map<String,Object> row=mapper.lockConfig(id);if(row==null)return "ERR:NOT_FOUND";
  String type=(String)row.get("channel_type");String provider=(String)row.get("provider");
  mapper.lockByType(type);
  if(enabled)mapper.disableOtherById(type,id);
  mapper.toggle(id,enabled,actor);
  log(actor,"toggle_channel_config",type,provider,enabled?"启用":"停用");return "OK";
 }
 @Override @Transactional public String delete(String actor,String id){
  if(actor==null)return "ERR:NO_AUTH";
  if(!admin(actor))return "ERR:NOT_ADMIN";
  Map<String,Object> row=mapper.lockConfig(id);if(row==null)return "ERR:NOT_FOUND";
  mapper.delete(id);log(actor,"delete_channel_config",(String)row.get("channel_type"),(String)row.get("provider"),"");return "OK";
 }
 @Override @Transactional(readOnly=true) public Map<String,Object> summary(String actor){
  if(!admin(actor))return Map.of("err","NOT_ADMIN");
  List<Map<String,Object>> out=new ArrayList<>();
  for(Map<String,Object> row:mapper.allConfigs()){
   Map<String,Object> item=new LinkedHashMap<>();
   for(String k:List.of("id","channel_type","provider","enabled","remark","updated_at"))item.put(k,row.get(k));
   Map<String,Object> masked=new LinkedHashMap<>();
   try {
    String config=Objects.toString(row.get("config"),"{}");
    Map<String,Object> raw=json.readValue(config,new TypeReference<Map<String,Object>>(){});
    for(var e:raw.entrySet()){
     String v=e.getValue()==null?"":String.valueOf(e.getValue());
     masked.put(e.getKey(),e.getKey().matches("(?i).*(secret|key|password|token).*") && !v.isEmpty() ? v.substring(0,Math.min(v.length(),3))+"****" : v);
    }
   }catch(Exception ex){masked.put("_invalid_config","****");}
   item.put("masked_config",masked);out.add(item);
  }
  return Map.of("items",out);
 }
}
