package com.kuaixietech.mapper.order_fulfillment;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper public interface EvidencePrivacyRpcMapper {
 Map<String,Object> findOrderEvidence(@Param("id")String id);
 Map<String,Object> findTripEvidence(@Param("id")String id);
 List<Map<String,Object>> findFaces(@Param("orderId")String orderId);
 Map<String,Object> activeBinding(@Param("orderId")String orderId);
 Map<String,Object> orderParties(@Param("orderId")String orderId);
 int callLog(@Param("id")String id,@Param("orderId")String orderId,@Param("bindingId")String bindingId,@Param("caller")String caller,@Param("role")String role,@Param("peer")String peer,@Param("number")String number,@Param("result")String result);
}
