package com.kuaixietech.insurance;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.insurance.reconcile-enabled",havingValue="true")
public class InsuranceReconciliation {
 private final InsuranceStore store;private final InsuranceService service;
 public InsuranceReconciliation(InsuranceStore store,InsuranceService service){this.store=store;this.service=service;}
 @Scheduled(fixedDelayString="${app.insurance.reconcile-delay-ms:60000}") public void refresh(){
  for(var app:store.db.queryForList("SELECT * FROM t_goods_insurance_applications WHERE status IN('dispatching','awaiting_provider','scheduled','active') ORDER BY COALESCE(last_checked_at,created_at),id LIMIT 100"))try{service.refresh(app);}catch(Exception ignored){/* Stored sanitized error; never pretend underwriting or claims succeeded. */}
 }
}
