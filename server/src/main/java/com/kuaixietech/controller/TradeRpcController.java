package com.kuaixietech.controller;

import com.kuaixietech.service.trading.impl.TradeRpcMigrationService;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
/** Compatibility request/response wrapper for RPC paths.
 * ApiSecurityConfig intentionally denies these routes until a trusted auth provider is installed.
 * Client-supplied user IDs are NEVER accepted.
 */
@RestController
@RequestMapping("/api/rpc")
public class TradeRpcController {
  private final TradeRpcMigrationService svc;
  public TradeRpcController(TradeRpcMigrationService svc){this.svc=svc;}
  public record AcceptOrderBody(String orderId,BigDecimal deposit) { }
  public record QuoteBody(String demandId,BigDecimal price,Integer promiseDays,String purchasePlace,String note) { }
  public record AwardBody(String demandId,String quoteId,String insuranceChoice,BigDecimal expectedBuyerEscrow,String insuranceQuoteId) { }
  public record AwardQuoteBody(String demandId,String quoteId,String insuranceChoice,String insuranceQuoteId) { }
  public record PublishBody(BigDecimal amount,String source,String orderId) { }
  public record DemandBody(String demandId) { }
  @PostMapping("/accept_order_atomic") public String acceptOrder(@RequestBody AcceptOrderBody r) {
    return svc.acceptOrderAtomic(r.orderId(),r.deposit());
  }
  @PostMapping("/submit_pk_quote") public String submitQuote(@RequestBody QuoteBody r) {
    return svc.submitPkQuote(r.demandId(),r.price(),r.promiseDays(),r.purchasePlace(),r.note());
  }
  @PostMapping("/close_pk") public String closePk(@RequestBody DemandBody r) {
    return svc.closePk(r.demandId());
  }
  @PostMapping("/award_pk") public String awardPk(@RequestBody AwardBody r) {
    return svc.awardPk(r.demandId(),r.quoteId(),r.insuranceChoice(),r.expectedBuyerEscrow(),r.insuranceQuoteId());
  }
  @PostMapping("/quote_pk_award") public Object quoteAward(@RequestBody AwardQuoteBody r){return svc.quotePkAward(r.demandId(),r.quoteId(),r.insuranceChoice(),r.insuranceQuoteId());}
  @PostMapping("/create_publish_deposit") public String createPublish(@RequestBody PublishBody r) {
    return svc.createPublishDeposit(r.amount(),r.source(),r.orderId());
  }
  @PostMapping("/refund_publish_deposit") public String refundPublish() {
    return svc.refundPublishDeposit();
  }
}
