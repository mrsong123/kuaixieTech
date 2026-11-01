package com.kuaixietech.refunds;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.payments.PaymentModels.Merchant;
import java.math.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class RefundStore {
 final JdbcTemplate db;private final ObjectMapper json;
 public RefundStore(JdbcTemplate db,ObjectMapper json){this.db=db;this.json=json;}
 Map<String,Object> row(String sql,Object...args){return db.queryForList(sql,args).stream().findFirst().orElse(null);}
 public Map<String,Object> refund(String id){return row("SELECT * FROM t_payment_refunds WHERE id=?",id);}
 public Merchant merchant(Map<String,Object> refund){try{return json.readValue(refund.get("merchant_snapshot").toString(),Merchant.class);}catch(Exception e){throw new IllegalStateException("REFUND_MERCHANT_SNAPSHOT_INVALID");}}
 static BigInteger minor(Object value){return new BigDecimal(value.toString()).toBigIntegerExact();}
 static String s(Map<String,Object> row,String field){return Objects.toString(row.get(field),null);}
 static String time(Object value){if(value==null)return null;if(value instanceof java.sql.Timestamp stamp)return stamp.toInstant().toString();if(value instanceof java.time.LocalDateTime date)return date.toInstant(java.time.ZoneOffset.UTC).toString();return value.toString();}
 public static Map<String,Object> view(Map<String,Object> row){var result=new LinkedHashMap<String,Object>();for(String[] field:new String[][]{{"id","id"},{"paymentId","payment_id"},{"status","status"},{"baseCurrency","base_currency"},{"paymentCurrency","payment_currency"},{"reason","reason"},{"reviewNote","review_note"},{"providerRefundId","provider_refund_id"},{"lastError","last_error_code"},{"createdAt","created_at"},{"updatedAt","updated_at"}})result.put(field[0],row.get(field[1]));result.put("createdAt",time(row.get("created_at")));result.put("updatedAt",time(row.get("updated_at")));result.put("baseAmount",new BigDecimal(row.get("base_minor").toString()).movePointLeft(2).setScale(2).toPlainString());result.put("paymentAmount",new BigDecimal(row.get("payment_minor").toString()).movePointLeft(2).setScale(2).toPlainString());return result;}
 void event(String id,String actor,String action,String detail){if(db.update("INSERT INTO t_payment_refund_events(id,refund_id,actor_id,action,detail) VALUES(?,?,?,?,?)",UUID.randomUUID().toString(),id,actor,action,detail)!=1)throw new IllegalStateException("REFUND_AUDIT_UNAVAILABLE");}
}
