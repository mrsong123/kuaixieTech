package com.kuaixietech.insurance;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.kuaixietech.insurance.InsuranceModels.*;
@Service
public class InsuranceCatalog {
 private final InsuranceStore store;private final DomainAccessGuard guard;private final Map<String,Adapter> adapters;
 public InsuranceCatalog(InsuranceStore store,DomainAccessGuard guard,List<Adapter> providers){this.store=store;this.guard=guard;this.adapters=new HashMap<>();for(Adapter a:providers)if(adapters.put(a.id(),a)!=null)throw new IllegalStateException("DUPLICATE_INSURER_ADAPTER");}
 Adapter adapter(Product p){Adapter a=adapters.get(p.adapter());if(a==null||!a.insurerCode().equals(p.insurerCode())||!a.configured(p))throw new IllegalStateException("INSURER_NOT_CONNECTED");return a;}
 Product available(String id){Product p=store.product(id);if(p==null||!p.enabled())throw new IllegalArgumentException("INSURANCE_PRODUCT_UNAVAILABLE");adapter(p);return p;}
 public List<Map<String,Object>> products(){return products("web");}
 public List<Map<String,Object>> products(String platform){
  if(!Set.of("web","h5","app","mp_weixin").contains(platform))throw new IllegalArgumentException("INSURANCE_PLATFORM_UNSUPPORTED");
  List<Map<String,Object>> result=new ArrayList<>();
  for(Product p:store.products())if(p.enabled())try{if(!adapter(p).supportedPlatforms(p).contains(platform))continue;result.add(Map.of("id",p.id(),"insurerCode",p.insurerCode(),"insurerName",p.insurerName(),"name",p.name(),"kind",p.kind(),"termsUrl",p.termsUrl(),"exclusionsUrl",p.exclusionsUrl(),"claimsUrl",p.claimsUrl(),"paymentMode","insurer_direct","status","quote_required"));}catch(IllegalStateException ignored){}
  return result;
 }
 public Map<String,Object> settings(){guard.requireAdmin();return Map.of("directory",InsurerDirectory.entries(),"products",store.products(),"adapters",adapters.values().stream().map(a->Map.of("id",a.id(),"insurerCode",a.insurerCode())).toList(),"paymentMode","insurer_direct");}
 @Transactional public Map<String,String> save(ProductInput input){
  String user=guard.requireAdmin();String id=input.id()==null?UUID.randomUUID().toString():input.id();
  InsuranceUrlPolicy.require(input.termsUrl(),input.serviceHosts());InsuranceUrlPolicy.require(input.exclusionsUrl(),input.serviceHosts());InsuranceUrlPolicy.require(input.claimsUrl(),input.serviceHosts());
  Product p=new Product(id,input.insurerCode(),input.insurerName().trim(),input.name().trim(),input.kind(),input.adapter(),input.providerProductCode(),input.credentialPrefix(),input.termsUrl(),input.exclusionsUrl(),input.claimsUrl(),Set.copyOf(input.serviceHosts()),Set.copyOf(input.checkoutHosts()),input.enabled(),1);
  if(input.enabled()&&adapter(p).supportedPlatforms(p).isEmpty())throw new IllegalStateException("INSURER_PLATFORM_NOT_CERTIFIED");
  store.db.update("INSERT INTO t_goods_insurance_products(id,insurer_code,config_json,enabled,revision,updated_by) VALUES(?,?,?,?,1,?) ON DUPLICATE KEY UPDATE insurer_code=VALUES(insurer_code),config_json=VALUES(config_json),enabled=VALUES(enabled),revision=revision+1,updated_by=VALUES(updated_by)",id,p.insurerCode(),store.json(p),p.enabled(),user);
  store.db.update("INSERT INTO t_admin_logs(id,operator_id,action,target_type,target_id,detail) VALUES(?,?,?,'goods_insurance_product',?,?)",UUID.randomUUID().toString(),user,"insurance_product_save",id,"direct insurer payment; credentials referenced by environment prefix only");
  return Map.of("id",id);
 }
}
