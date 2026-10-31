package com.kuaixietech.service.community.impl;
import com.kuaixietech.mapper.community.Batch12ModerationMapper;
import com.kuaixietech.security.TrustedActor;
import java.util.*;import java.util.regex.*;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
/** Port of screen_chat_content (final 20261024_162223) and add_post_comment. */
@Service public class Batch12ContentModerationService {
 private final Batch12ModerationMapper m;
 public Batch12ContentModerationService(Batch12ModerationMapper m){this.m=m;}
 public record Screening(String decision,List<String> codes){}
 private static final Pattern STRIP=Pattern.compile("[\\s\\-_.*·、,，。:：;；/\\\\()（）\\[\\]【】#*+~`^=!<>?\"'‘“”]");
 private static final Pattern WECHAT=Pattern.compile("加v|威信|薇信|微芯|微信|绿泡泡|扣扣|抠抠|(^|[^a-z])(vx|wx|weixin|wechat|qq)([^a-z]|$)");
 private static final Pattern OFF_PLATFORM=Pattern.compile("电话|手机|号码|联系方式|私聊|私下|加我好友|换个地方|站外联系|线下交易|不走平台");
 private static final Pattern PHONE=Pattern.compile("1[3-9][0-9]{9}");
 private static final Pattern NUMBER_HINT=Pattern.compile("[0-9]{3,4}");
 private static final Pattern INTENT_HINT=Pattern.compile("打|拨|联系|找我|号码");
 private static String normalized(String text){return STRIP.matcher(text.toLowerCase(Locale.ROOT)).replaceAll("");}
 private static String digits(String text){return text.replaceAll("[^0-9]","");}
 private static String chineseNumeral(String text){
  String from="〇零一二三四五六七八九",to="00123456789";StringBuilder b=new StringBuilder();
  for(char c:text.toCharArray()){int i=from.indexOf(c);b.append(i<0?c:to.charAt(i));}return b.toString();
 }
 private void log(String actor,String code,String text){if(actor!=null)m.violation(UUID.randomUUID().toString(),actor,code,text.length()>120?text.substring(0,120):text);}
 @Transactional public Screening screenChatContent(String actor,String text){
  if(actor==null||text==null||text.trim().isEmpty())return new Screening("pass",List.of());
  String n=normalized(text),d=digits(n);List<String> codes=new ArrayList<>();
  if(WECHAT.matcher(n).find())codes.add("contact_wechat_variant");
  if(OFF_PLATFORM.matcher(n).find())codes.add("off_platform_intent");
  if(PHONE.matcher(n).find())codes.add("phone_direct");
  if(PHONE.matcher(chineseNumeral(n)).find())codes.add("phone_homoglyph");
  if(d.length()>=11&&PHONE.matcher(d).find()&&!PHONE.matcher(n).find())codes.add("phone_split");
  if(codes.isEmpty()&&m.recentSplitCount(actor)>=3)codes.add("phone_split_suspect");
  if(!codes.isEmpty()){for(String code:codes)log(actor,code,text);return new Screening("block",codes);}
  if(NUMBER_HINT.matcher(d).find()&&INTENT_HINT.matcher(n).find())log(actor,"phone_split_suspect",text);
  return new Screening("pass",List.of());
 }
 @Transactional public String addPostComment(String post,String content){
  String actor=TrustedActor.currentUserId();if(content==null||content.trim().isEmpty()||content.length()>500)return "ERR:BAD_CONTENT";
  Map<String,Object> p=m.lockPost(post);if(p==null||!"published".equals(String.valueOf(p.get("status"))))return "ERR:NO_POST";
  if("block".equals(screenChatContent(actor,content).decision()))return "ERR:CONTENT_BLOCKED";
  if(m.newComment(UUID.randomUUID().toString(),post,actor,content.trim())!=1)throw new IllegalStateException("comment insert failed");
  m.updateCommentCount(post);String author=String.valueOf(p.get("author_id"));
  if(!actor.equals(author))m.notification(UUID.randomUUID().toString(),author,content.substring(0,Math.min(40,content.length())));
  return "OK";
 }
}
