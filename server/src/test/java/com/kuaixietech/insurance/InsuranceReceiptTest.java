package com.kuaixietech.insurance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import static com.kuaixietech.insurance.InsuranceModels.*;
class InsuranceReceiptTest {
 JdbcTemplate db;InsuranceTransactions tx;Map<String,Object> app,q;Product product;
 @BeforeEach void init() throws Exception{
  db=mock(JdbcTemplate.class);var json=new ObjectMapper();var store=new InsuranceStore(db,json);tx=new InsuranceTransactions(store,mock(InsuranceCatalog.class),json);
  product=new InsuranceSafetyTest().product();app=new HashMap<>(Map.of("id","app","order_id","order","quote_id","quote","provider_application_ref","provider-app","status","awaiting_provider"));
  q=new InsuranceSafetyTest().quote();q.put("snapshot_json",json.writeValueAsString(product));
  when(db.queryForList(contains("FROM t_goods_insurance_applications"),eq("app"))).thenReturn(List.of(app));
  when(db.queryForList(contains("FROM t_orders"),eq("order"))).thenReturn(List.of(Map.of("id","order","status","locked")));
  when(db.queryForList(contains("FROM t_goods_insurance_quotes"),eq("quote"))).thenReturn(List.of(q));
  when(db.update(anyString(),any(Object[].class))).thenReturn(1);
 }
 PolicyReceipt receipt(String number,Instant start,Instant end){return new PolicyReceipt("provider-app","app","order","picc","P01","active",number,"coverage",new BigDecimal("1000"),new BigDecimal("8.50"),"CNY",true,"terms-v1",start,end,"https://insurer.example/policy");}
 @Test void effectivePolicyUpdatesCoverageWithoutDebitingOrCreditingAnyWallet(){tx.receipt("app",receipt("policy",Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600)));verify(db).update(contains("SET status=?"),eq("active"),eq("provider-app"),eq("policy"),eq("coverage"),eq("https://insurer.example/policy"),any(Timestamp.class),any(Timestamp.class),eq("app"));verify(db).update(startsWith("UPDATE t_orders SET insured=?"),eq("yes"),eq("order"));verify(db,never()).update(contains("t_profiles"),any(Object[].class));verify(db,never()).update(contains("t_balance_ledger"),any(Object[].class));}
 @Test void missingPolicyNumberCannotMarkAnOrderInsured(){assertThrows(IllegalArgumentException.class,()->tx.receipt("app",receipt(null,Instant.now().minusSeconds(60),Instant.now().plusSeconds(3600))));verify(db,never()).update(anyString(),any(Object[].class));}
 @Test void futurePoliciesNeverMarkAnOrderInsured(){tx.receipt("app",receipt("policy",Instant.now().plusSeconds(60),Instant.now().plusSeconds(3600)));verify(db).update(startsWith("UPDATE t_orders SET insured=?"),eq("unselected"),eq("order"));verify(db,never()).update(startsWith("UPDATE t_orders SET insured=?"),eq("yes"),any());}
 @Test void expiredPoliciesNeverMarkAnOrderInsured(){tx.receipt("app",receipt("policy",Instant.now().minusSeconds(3600),Instant.now().minusSeconds(60)));verify(db).update(startsWith("UPDATE t_orders SET insured=?"),eq("unselected"),eq("order"));verify(db,never()).update(startsWith("UPDATE t_orders SET insured=?"),eq("yes"),any());}

}
