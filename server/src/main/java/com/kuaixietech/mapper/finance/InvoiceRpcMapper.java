package com.kuaixietech.mapper.finance;
import org.apache.ibatis.annotations.*;
import java.util.*;
import java.math.*;
@Mapper public interface InvoiceRpcMapper {
 Map<String,Object> lockOrder(@Param("id") String id);
 Integer invoiceExists(@Param("orderId") String orderId);
 int createInvoice(@Param("id")String id,@Param("orderId")String orderId,@Param("userId")String userId,@Param("titleType")String titleType,@Param("titleName")String titleName,@Param("taxNo")String taxNo,@Param("kind")String kind,@Param("amount")BigDecimal amount,@Param("email")String email,@Param("remark")String remark);
 int recordEmail(@Param("id")String id,@Param("email")String email);
 Map<String,Object> lockInvoice(@Param("id")String id);
 int issue(@Param("id")String id,@Param("approve")boolean approve,@Param("invoiceNo")String invoiceNo,@Param("fileUrl")String fileUrl,@Param("reason")String reason);
 int notifyUser(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,@Param("body")String body);
}
