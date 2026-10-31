package com.kuaixietech.mapper.account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface KycRpcMapper {
 int hasDoc(@Param("uid")String uid,@Param("kind")String kind,@Param("challenge")boolean challenge);
 int updateBuyerSubmission(@Param("uid")String uid,@Param("name")String name,@Param("mask")String masked);
 int upsertDoc(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,
       @Param("url")String url,@Param("code")String challenge);
 int isAdmin(@Param("uid")String uid);
 int reviewBuyer(@Param("uid")String uid,@Param("approved")boolean approved,@Param("reason")String reason);
 int reviewDocs(@Param("uid")String uid,@Param("approved")boolean approved,@Param("reason")String reason);
 int addNotice(@Param("id")String id,@Param("uid")String uid,@Param("title")String title,@Param("body")String body);
 int addAdminLog(@Param("id")String id,@Param("admin")String admin,@Param("target")String target,@Param("detail")String detail);
 int insertChallenge(@Param("id")String id,@Param("uid")String uid,@Param("channel")String channel,@Param("code")String code);
 String lockChallenge(@Param("uid")String uid,@Param("code")String code);
 int markChallengeUsed(@Param("id")String id);
 int insertFaceRecord(@Param("id")String id,@Param("uid")String uid,@Param("scene")String scene,
    @Param("url")String url,@Param("device")String device,@Param("orderId")String orderId,@Param("code")String code);
}
