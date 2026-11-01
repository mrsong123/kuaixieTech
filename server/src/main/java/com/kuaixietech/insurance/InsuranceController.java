package com.kuaixietech.insurance;
import com.kuaixietech.security.DomainAccessGuard;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import static com.kuaixietech.insurance.InsuranceModels.*;
@RestController
public class InsuranceController {
 private final InsuranceCatalog catalog;private final InsuranceService service;private final InsuranceStore store;private final DomainAccessGuard guard;
 public InsuranceController(InsuranceCatalog catalog,InsuranceService service,InsuranceStore store,DomainAccessGuard guard){this.catalog=catalog;this.service=service;this.store=store;this.guard=guard;}
 @GetMapping("/api/public/insurance/companies") public List<Map<String,String>> companies(){return InsurerDirectory.entries();}
 @GetMapping("/api/public/insurance/products") public List<Map<String,Object>> products(@RequestParam(defaultValue="web") String platform){return catalog.products(platform);}
 @GetMapping("/api/me/insurance/pk-subject") public Map<String,Object> subject(@RequestParam String demandId,@RequestParam String quoteId){return service.pkSubject(demandId,quoteId);}
 @PostMapping("/api/me/insurance/quotes") public Quote quote(@Valid @RequestBody QuoteInput input){return service.quote(input);}
 @GetMapping("/api/me/insurance/quotes/{id}") public Quote ownQuote(@PathVariable String id){return service.ownQuote(id);}
 @PostMapping("/api/me/insurance/applications") public Application apply(@Valid @RequestBody ApplyInput input){return service.apply(input);}
 @GetMapping("/api/me/insurance/consents") public List<Map<String,Object>> consents(){String user=guard.requireActiveProfile();return store.db.queryForList("SELECT id,consent_type,notice_version,recipient_name,purpose,information_types,rights_method,accepted,accepted_at FROM t_consent_records WHERE user_id=? AND source='goods_insurance' ORDER BY accepted_at DESC,id DESC LIMIT 100",user);}
 @GetMapping("/api/me/insurance/applications") public List<Application> mine(){return service.mine();}
 @GetMapping("/api/me/insurance/orders/{orderId}") public Map<String,Object> forOrder(@PathVariable String orderId){return service.forOrder(orderId);}
 @GetMapping("/api/me/insurance/applications/{id}/record") public Application record(@PathVariable String id){return service.record(id);}
 @GetMapping("/api/me/insurance/applications/{id}") public Application status(@PathVariable String id){return service.status(id);}
 @GetMapping("/api/portal/admin/insurance/settings") public Map<String,Object> settings(){return catalog.settings();}
 @PutMapping("/api/portal/admin/insurance/products") public Map<String,String> save(@Valid @RequestBody ProductInput input){return catalog.save(input);}
 @GetMapping("/api/portal/admin/insurance/applications") public List<Map<String,Object>> applications(){guard.requireAdmin();return store.db.queryForList("SELECT id,user_id,order_id,quote_id,insurer_code,status,provider_application_ref,policy_number,coverage_ref,coverage_starts_at,coverage_ends_at,last_checked_at,last_error_code,created_at FROM t_goods_insurance_applications ORDER BY created_at DESC,id DESC LIMIT 100");}
}
