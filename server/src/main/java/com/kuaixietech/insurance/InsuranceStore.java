package com.kuaixietech.insurance;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static com.kuaixietech.insurance.InsuranceModels.*;
@Repository
public class InsuranceStore {
 final JdbcTemplate db;private final ObjectMapper json;
 public InsuranceStore(JdbcTemplate db,ObjectMapper json){this.db=db;this.json=json;}
 String json(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("INSURANCE_INVALID_SNAPSHOT");}}
 <T>T parse(Object value,Class<T> type){try{return json.readValue(String.valueOf(value),type);}catch(Exception e){throw new IllegalStateException("INSURANCE_INVALID_SNAPSHOT");}}
 Map<String,Object> row(String sql,Object...args){return db.queryForList(sql,args).stream().findFirst().orElse(null);}
 Product product(Map<String,Object> row){if(row==null)return null;Product p=parse(row.get("config_json"),Product.class);return new Product(p.id(),p.insurerCode(),p.insurerName(),p.name(),p.kind(),p.adapter(),p.providerProductCode(),p.credentialPrefix(),p.termsUrl(),p.exclusionsUrl(),p.claimsUrl(),p.serviceHosts(),p.checkoutHosts(),truth(row.get("enabled")),((Number)row.get("revision")).longValue());}
 Product product(String id){return product(row("SELECT * FROM t_goods_insurance_products WHERE id=?",id));}
 List<Product> products(){return db.queryForList("SELECT * FROM t_goods_insurance_products ORDER BY id").stream().map(this::product).toList();}
 Map<String,Object> quote(String id){return row("SELECT * FROM t_goods_insurance_quotes WHERE id=?",id);}
 Map<String,Object> application(String id){return row("SELECT * FROM t_goods_insurance_applications WHERE id=?",id);}
 static String s(Map<String,Object> row,String key){Object value=row.get(key);return value==null?null:value.toString();}
 static boolean truth(Object value){return Boolean.TRUE.equals(value)||"1".equals(String.valueOf(value));}
 static Instant instant(Object value){if(value instanceof Timestamp t)return t.toInstant();if(value instanceof Instant i)return i;if(value instanceof LocalDateTime d)return d.toInstant(ZoneOffset.UTC);throw new IllegalStateException("INSURANCE_INVALID_TIMESTAMP");}
}
