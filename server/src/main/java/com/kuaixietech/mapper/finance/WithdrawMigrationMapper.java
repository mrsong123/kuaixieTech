package com.kuaixietech.mapper.finance;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface WithdrawMigrationMapper {
 Integer isAdmin(@Param("actor") String actor);
 BigDecimal lockBalance(@Param("user") String user);
 Integer openWithdrawals(@Param("user") String user);
 int debitBalance(@Param("user") String user, @Param("amount") BigDecimal amount);
 int creditBalance(@Param("user") String user, @Param("amount") BigDecimal amount);
 int insertWithdrawal(@Param("id") String id,@Param("user") String user,@Param("amount") BigDecimal amount);
 Map<String,Object> lockWithdrawal(@Param("id") String id);
 int reviewWithdrawal(@Param("id") String id,@Param("status") String status,@Param("admin") String admin,@Param("note") String note);
 int insertLedger(@Param("id") String id,@Param("user") String user,@Param("kind") String kind,@Param("amount") BigDecimal amount,@Param("note") String note);
 int insertNotification(@Param("id") String id,@Param("user") String user,@Param("title") String title,@Param("body") String body);
 int insertAdminLog(@Param("id") String id,@Param("admin") String admin,@Param("action") String action,@Param("target") String target,@Param("detail") String detail);
}
