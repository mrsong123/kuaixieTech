package com.kuaixietech.mapper.communication;
import java.util.Map;import org.apache.ibatis.annotations.*;
@Mapper public interface Batch12PrivacyMapper {
 Map<String,Object> lockOrder(@Param("id") String id);
 String phone(@Param("user") String user);
 int releaseExpiredNumbers(@Param("order") String order);
 int releaseExpiredBindings(@Param("order") String order);
 Map<String,Object> activeBinding(@Param("order") String order);
 String availableNumber();
 int occupyNumber(@Param("number") String number);
 int createBinding(@Param("id") String id,@Param("order") String order,@Param("number") String number,@Param("buyer") String buyer,@Param("vendor") String vendor);
 int notifyUser(@Param("id") String id,@Param("user") String user,@Param("order") String order);
}
