package com.kuaixietech.payments;

import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.kuaixietech.payments.PaymentModels.*;

@Service
public class PaymentAdminService {
  private final PaymentStore store;private final DomainAccessGuard guard;private final PaymentService payments;private final PaymentHttp http;
  public PaymentAdminService(PaymentStore store,DomainAccessGuard guard,PaymentService payments,PaymentHttp http){this.store=store;this.guard=guard;this.payments=payments;this.http=http;}
  public Map<String,Object> settings(){
    guard.requireAdmin();
    return Map.of("settlementCurrency","CNY","merchants",store.merchants(),"fxSources",store.sources(),"currencies",CurrencyAmounts.catalog(),
      "adapters",payments.adapterInfo());
  }
  @Transactional
  public Map<String,String> merchant(MerchantInput input){
    String actor=guard.requireAdmin();String id=id(input.id());
    if(input.currencies().size()>250||input.settings()!=null&&input.settings().size()>10)throw new IllegalArgumentException("CONFIGURATION_TOO_LARGE");
    List<String> currencies=input.currencies().stream().map(c->CurrencyAmounts.currency(c).getCurrencyCode()).distinct().sorted().toList();
    Map<String,String> settings=input.settings()==null?Map.of():Map.copyOf(input.settings());
    Set<String> allowed=Set.of("notifyUrl","returnUrl","gatewayUrl","checkoutHosts");
    for(var entry:settings.entrySet())if(!allowed.contains(entry.getKey())||entry.getValue().length()>1500)throw new IllegalArgumentException("SECRET_OR_UNKNOWN_SETTING_REJECTED");
    for(String key:List.of("notifyUrl","returnUrl","gatewayUrl"))if(settings.containsKey(key))http.endpoint(settings.get(key),key.equals("gatewayUrl"));
    Merchant merchant=new Merchant(id,input.name().trim(),input.adapter(),input.method(),input.platform(),currencies,input.credentialPrefix(),input.spreadBps(),input.quoteTtlSeconds(),input.enabled(),1,settings);
    Adapter adapter=payments.adapter(merchant);
    if(!adapter.methods().contains(input.method())||!adapter.platforms().contains(input.platform()))throw new IllegalArgumentException("ADAPTER_CAPABILITY_MISMATCH");
    if(!adapter.currencies().isEmpty()&&!adapter.currencies().containsAll(currencies))throw new IllegalArgumentException("ADAPTER_CURRENCY_MISMATCH");
    if(input.enabled()&&!adapter.configured(merchant))throw new IllegalArgumentException("CREDENTIALS_REQUIRED_BEFORE_ENABLING");
    store.db.update("INSERT INTO t_payment_merchants(id,name,adapter,method,platform,currencies,credential_prefix,spread_bps,quote_ttl_seconds,enabled,settings,updated_by) VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),adapter=VALUES(adapter),method=VALUES(method),platform=VALUES(platform),currencies=VALUES(currencies),credential_prefix=VALUES(credential_prefix),spread_bps=VALUES(spread_bps),quote_ttl_seconds=VALUES(quote_ttl_seconds),enabled=VALUES(enabled),settings=VALUES(settings),updated_by=VALUES(updated_by),revision=revision+1",
      id,merchant.name(),merchant.adapter(),merchant.method(),merchant.platform(),store.json(currencies),merchant.credentialPrefix(),merchant.spreadBps(),merchant.quoteTtlSeconds(),merchant.enabled(),store.json(settings),actor);
    audit(actor,"payment_merchant_configured",id);return Map.of("id",id);
  }
  @Transactional
  public Map<String,String> source(FxInput input){
    String actor=guard.requireAdmin();String id=id(input.id());
    if(!Set.of("open_er_api","rates_json_v1").contains(input.kind()))throw new IllegalArgumentException("UNSUPPORTED_FX_SOURCE");
    if(input.kind().equals("rates_json_v1"))http.endpoint(input.endpoint(),true);
    store.db.update("INSERT INTO t_fx_sources(id,name,kind,endpoint,credential_prefix,max_age_seconds,enabled,transaction_enabled,updated_by) VALUES(?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),kind=VALUES(kind),endpoint=VALUES(endpoint),credential_prefix=VALUES(credential_prefix),max_age_seconds=VALUES(max_age_seconds),enabled=VALUES(enabled),transaction_enabled=VALUES(transaction_enabled),updated_by=VALUES(updated_by)",
      id,input.name().trim(),input.kind(),input.kind().equals("open_er_api")?null:input.endpoint(),input.credentialPrefix(),input.maxAgeSeconds(),input.enabled(),input.transactionEnabled(),actor);
    audit(actor,"fx_source_configured",id);return Map.of("id",id);
  }
  private String id(String id){if(id==null||id.isBlank())return UUID.randomUUID().toString();return UUID.fromString(id).toString();}
  private void audit(String actor,String action,String target){store.db.update("INSERT INTO t_admin_logs(id,operator_id,action,target_type,target_id,detail) VALUES(?,?,?,?,?,?)",UUID.randomUUID().toString(),actor,action,"payment_config",target,"Configuration updated; credentials excluded");}
}
