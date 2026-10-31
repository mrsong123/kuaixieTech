package com.kuaixietech.mapper.assist;
import java.util.Map;
import org.apache.ibatis.annotations.*;
@Mapper public interface Batch12AssistMapper {
 Map<String,Object> lockOrder(@Param("id") String id);
 Map<String,Object> lockAssist(@Param("id") String id);
 int activeCount(@Param("order") String order);
 int verifiedCarrier(@Param("id") String id);
 int create(@Param("id") String id,@Param("order") String order,@Param("actor") String actor,@Param("rate") java.math.BigDecimal rate,@Param("note") String note);
 int accept(@Param("id") String id,@Param("actor") String actor);
 int cancel(@Param("id") String id);
 int handoverA(@Param("id") String id,@Param("urls") String urls,@Param("city") String city);
 int finish(@Param("id") String id,@Param("urls") String urls,@Param("city") String city,@Param("amount") java.math.BigDecimal amount);
 int notifyUser(@Param("id") String id,@Param("user") String user,@Param("title") String title,@Param("body") String body);
 int notifyCarriers(@Param("actor") String actor,@Param("rate") String rate);
}
