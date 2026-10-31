package com.kuaixietech.mapper.account;
import java.util.Map;import org.apache.ibatis.annotations.*;
@Mapper public interface Batch12AccountMapper {
 Map<String,Object> lockProfile(@Param("actor") String actor);
 int updateEnterprise(@Param("actor") String actor,@Param("name") String name,@Param("license") String license);
 int deleteLicense(@Param("actor") String actor);
 int deleteAttachments(@Param("actor") String actor);
 int addAttachment(@Param("id") String id,@Param("actor") String actor,@Param("url") String url,@Param("pos") int pos);
 int createLicense(@Param("id") String id,@Param("actor") String actor,@Param("url") String url);
 int carrierDocs(@Param("actor") String actor);
 int consumeCode(@Param("target") String target,@Param("code") String code);
 int consumeInvite(@Param("code") String code,@Param("actor") String actor);
 int carrierPending(@Param("actor") String actor,@Param("name") String name,@Param("idMasked") String idMasked,@Param("passMasked") String passMasked,@Param("note") String note);
 int carrierDocsReset(@Param("actor") String actor);
 int notify(@Param("id") String id,@Param("actor") String actor,@Param("title") String title,@Param("body") String body);
 Map<String,Object> lockJoin(@Param("id") String id);
 String lookupByUsername(@Param("username") String username);
 int bindJoin(@Param("id") String id,@Param("user") String user);
 int recentCodeCount(@Param("target") String target,@Param("channel") String channel);
 int newCode(@Param("id") String id,@Param("target") String target,@Param("channel") String channel,@Param("code") String code);
 int enqueueCode(@Param("target") String target,@Param("channel") String channel,@Param("code") String code);
}
