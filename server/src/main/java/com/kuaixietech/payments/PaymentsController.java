package com.kuaixietech.payments;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import static com.kuaixietech.payments.PaymentModels.*;

@RestController
public class PaymentsController {
  private final PaymentService payments;private final PaymentAdminService admin;private final FxService fx;
  public PaymentsController(PaymentService payments,PaymentAdminService admin,FxService fx){this.payments=payments;this.admin=admin;this.fx=fx;}
  @GetMapping("/api/public/payments/currencies") public List<Map<String,Object>> currencies(){return CurrencyAmounts.catalog();}
  @GetMapping("/api/public/payments/methods") public List<Map<String,Object>> methods(@RequestParam(defaultValue="web") String platform){return payments.capabilities(platform);}
  @GetMapping("/api/public/payments/rate") public Map<String,Object> rate(@RequestParam(defaultValue="CNY") String base,@RequestParam String currency){
    Rate rate=fx.reference(base,currency);return Map.of("base",rate.base(),"currency",rate.currency(),"rate",rate.value().toPlainString(),"source",rate.source(),"asOf",rate.asOf(),"validUntil",rate.validUntil(),"referenceOnly",true);
  }
  @PostMapping("/api/me/payments/quotes") public Quote quote(@Valid @RequestBody QuoteInput input){return payments.quote(input);}
  @PostMapping("/api/me/payments") public Payment create(@Valid @RequestBody ConfirmInput input){return payments.create(input);}
  @GetMapping("/api/me/payments") public List<Payment> history(){return payments.history();}
  @GetMapping("/api/me/payments/{id}") public Payment status(@PathVariable String id){return payments.status(id);}
  @GetMapping("/api/portal/admin/payments/settings") public Map<String,Object> settings(){return admin.settings();}
  @PutMapping("/api/portal/admin/payments/merchants") public Map<String,String> merchant(@Valid @RequestBody MerchantInput input){return admin.merchant(input);}
  @PutMapping("/api/portal/admin/payments/fx-sources") public Map<String,String> source(@Valid @RequestBody FxInput input){return admin.source(input);}
  @PostMapping("/api/payment-callback/{merchant}/{id}") public ResponseEntity<String> callback(@PathVariable String merchant,@PathVariable String id){
    String adapter=payments.callback(merchant,id);
    if(adapter==null)return ResponseEntity.status(503).body("failure");
    return adapter.equals("alipay_rsa2")?ResponseEntity.ok("success"):ResponseEntity.noContent().build();
  }
}
