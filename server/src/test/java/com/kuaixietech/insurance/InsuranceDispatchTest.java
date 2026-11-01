package com.kuaixietech.insurance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.security.DomainAccessGuard;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static com.kuaixietech.insurance.InsuranceModels.*;
class InsuranceDispatchTest {
 @Test void ambiguousProviderDispatchIsNeverRepeatedOnRetry() throws Exception {
  var db=mock(JdbcTemplate.class);var json=new ObjectMapper();var store=new InsuranceStore(db,json);var catalog=mock(InsuranceCatalog.class);var transactions=mock(InsuranceTransactions.class);var guard=mock(DomainAccessGuard.class);var adapter=mock(Adapter.class);var product=new InsuranceSafetyTest().product();var service=new InsuranceService(store,catalog,transactions,guard);
  var app=new HashMap<String,Object>(Map.of("id","app","order_id","order","quote_id","quote","status","reserved"));
  var q=new HashMap<String,Object>(Map.of("product_id","cargo","product_revision",1,"declared_value",new BigDecimal("1000"),"premium",new BigDecimal("8.50"),"currency","CNY","provider_ref","provider-quote","terms_version","terms-v1","snapshot_json",json.writeValueAsString(product),"expires_at",Timestamp.from(Instant.now().plusSeconds(300))));
  when(guard.requireVerifiedBuyer()).thenReturn("buyer");when(transactions.reserve(eq("buyer"),any())).thenReturn(app);when(catalog.available("cargo")).thenReturn(product);when(catalog.adapter(any())).thenReturn(adapter);
  when(db.queryForList(contains("FROM t_goods_insurance_quotes"),eq("quote"))).thenReturn(List.of(q));when(db.queryForList(contains("FROM t_goods_insurance_applications"),eq("app"))).thenReturn(List.of(app));
  when(db.queryForList(contains("FROM t_orders"),eq("order"),eq("buyer"))).thenReturn(List.of(Map.of("id","order","status","locked","item_name","Camera","quantity",1,"unit","piece","goods_amount",new BigDecimal("1000"))));
  when(db.update(contains("SET status='dispatching'"),eq("app"))).thenAnswer(call->{app.put("status","dispatching");return 1;});when(adapter.apply(any(),any(),any(),any())).thenThrow(new IllegalStateException("timeout after send"));
  var input=new ApplyInput("order","quote",UUID.randomUUID().toString(),true,true);
  assertThrows(IllegalStateException.class,()->service.apply(input));assertEquals("dispatching",service.apply(input).status());verify(adapter,times(1)).apply(any(),eq("app"),any(),any());
 }
}
