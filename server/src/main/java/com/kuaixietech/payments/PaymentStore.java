package com.kuaixietech.payments;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static com.kuaixietech.payments.PaymentModels.*;

@Repository
public class PaymentStore {
  final JdbcTemplate db;private final ObjectMapper json=new ObjectMapper();
  public PaymentStore(JdbcTemplate db){this.db=db;}
  String json(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalArgumentException("INVALID_CONFIGURATION");}}
  Merchant merchant(Map<String,Object> row){
    try{return new Merchant(s(row,"id"),s(row,"name"),s(row,"adapter"),s(row,"method"),s(row,"platform"),json.readValue(s(row,"currencies"),new TypeReference<List<String>>(){}),s(row,"credential_prefix"),((Number)row.get("spread_bps")).intValue(),((Number)row.get("quote_ttl_seconds")).intValue(),truth(row.get("enabled")),((Number)row.get("revision")).longValue(),json.readValue(s(row,"settings"),new TypeReference<Map<String,String>>(){}));}
    catch(Exception e){throw new IllegalStateException("INVALID_MERCHANT_CONFIGURATION");}
  }
  public List<Merchant> merchants(){return db.queryForList("SELECT * FROM t_payment_merchants ORDER BY name,id").stream().map(this::merchant).toList();}
  public Merchant merchant(String id){return db.queryForList("SELECT * FROM t_payment_merchants WHERE id=?",id).stream().findFirst().map(this::merchant).orElse(null);}
  public FxSource fx(Map<String,Object> row){return new FxSource(s(row,"id"),s(row,"name"),s(row,"kind"),s(row,"endpoint"),s(row,"credential_prefix"),((Number)row.get("max_age_seconds")).intValue(),truth(row.get("enabled")),truth(row.get("transaction_enabled")));}
  public List<FxSource> sources(){return db.queryForList("SELECT * FROM t_fx_sources ORDER BY name,id").stream().map(this::fx).toList();}
  public static String s(Map<String,Object> row,String key){Object value=row.get(key);return value==null?null:value.toString();}
  static boolean truth(Object value){return Boolean.TRUE.equals(value)||"1".equals(String.valueOf(value));}
  Map<String,Object> row(String sql,Object...args){return db.queryForList(sql,args).stream().findFirst().orElse(null);}
  Map<String,Object> quote(String id){return row("SELECT * FROM t_payment_quotes WHERE id=?",id);}
  Map<String,Object> payment(String id){return row("SELECT * FROM t_currency_payments WHERE id=?",id);}
  Merchant snapshot(String value){try{return json.readValue(value,Merchant.class);}catch(Exception e){throw new IllegalStateException("INVALID_PAYMENT_SNAPSHOT");}}
}
