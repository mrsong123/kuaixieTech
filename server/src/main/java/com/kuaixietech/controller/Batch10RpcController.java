package com.kuaixietech.controller;
import com.kuaixietech.service.finance.impl.InvoiceRpcService;
import com.kuaixietech.service.operations.impl.AdminReviewRpcService;
import com.kuaixietech.service.operations.impl.OperationsRpcService;
import com.kuaixietech.service.order_fulfillment.impl.ShippingNegotiationService;
import com.kuaixietech.service.order_fulfillment.impl.MeetupRpcService;
import com.kuaixietech.service.order_fulfillment.impl.EvidencePrivacyRpcService;
import com.kuaixietech.service.internationalization.impl.LocaleRpcWrapperService;
import java.math.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
/** HTTP-facing ports of user and admin RPCs; identity derives from Spring Security only. */
@RestController @RequestMapping("/api/rpc")
public class Batch10RpcController {
 private final LocaleRpcWrapperService locales;private final EvidencePrivacyRpcService evidence;private final MeetupRpcService meetup;private final ShippingNegotiationService shipping;private final InvoiceRpcService invoice;private final AdminReviewRpcService review;private final OperationsRpcService ops;
 public Batch10RpcController(InvoiceRpcService invoice,AdminReviewRpcService review,OperationsRpcService ops,ShippingNegotiationService shipping,MeetupRpcService meetup,EvidencePrivacyRpcService evidence,LocaleRpcWrapperService locales){this.locales=locales;this.evidence=evidence;this.meetup=meetup;this.shipping=shipping;this.invoice=invoice;this.review=review;this.ops=ops;}
 public record InvoiceRequest(String orderId,String titleType,String titleName,String taxNo,String kind,String email,String remark){}
 public record InvoiceDecision(String invoiceId,boolean approve,String invoiceNo,String fileUrl,String reason){}
 public record ViolationQuery(Integer limit){}
 public record RowId(String id){}
 public record EnterpriseDecision(String userId,boolean approve,String note){}
 public record GrantPoints(String userId,Integer points,String note){}
 public record LocaleUpsert(String code,String nativeName,String labelZh,Integer sort,String fallback){}
 public record LocaleToggle(String code,boolean active,String note){}
 public record LocaleRemove(String code,String note){}
 public record PrivacyCall(String orderId,String result){}
 public record EvidenceRead(String orderId){}
 public record IssueMeetup(String orderId){}
 public record ConfirmMeetup(String orderId,String code,BigDecimal lat,BigDecimal lng,String city){}
 public record ProposeFee(String orderId,BigDecimal fee,String note){}
 public record RespondFee(String orderId,boolean accept,BigDecimal counter,BigDecimal expectedFee){}
 public record ArrivalProof(String tripId,String snapshotUrl,BigDecimal lat,BigDecimal lng,String city){}
 @PostMapping("/request_invoice") public String requestInvoice(@RequestBody InvoiceRequest b){return invoice.requestInvoice(b.orderId(),b.titleType(),b.titleName(),b.taxNo(),b.kind(),b.email(),b.remark());}
 @PostMapping("/admin_issue_invoice") public String issueInvoice(@RequestBody InvoiceDecision b){return invoice.adminIssueInvoice(b.invoiceId(),b.approve(),b.invoiceNo(),b.fileUrl(),b.reason());}
 @PostMapping("/admin_list_violations") public List<Map<String,Object>> violations(@RequestBody ViolationQuery b){return review.violations(b.limit());}
 @PostMapping("/admin_review_violation") public String reviewViolation(@RequestBody RowId b){return review.reviewViolation(b.id());}
 @PostMapping("/admin_stats") public Map<String,Object> adminStats(){return review.stats();}
 @PostMapping("/admin_invite_tier_distribution") public List<Map<String,Object>> tierDistribution(){return review.inviteTierDistribution();}
 @PostMapping("/admin_review_enterprise") public String enterprise(@RequestBody EnterpriseDecision b){return review.reviewEnterprise(b.userId(),b.approve(),b.note());}
 @PostMapping("/admin_grant_points") public String grantPoints(@RequestBody GrantPoints b){return ops.adminGrantPoints(b.userId(),b.points(),b.note());}
 @PostMapping("/admin_locale_upsert_rpc") public String localeUpsert(@RequestBody LocaleUpsert b){return locales.upsert(b.code(),b.nativeName(),b.labelZh(),b.sort(),b.fallback());}
 @PostMapping("/admin_locale_set_active_rpc") public String localeToggle(@RequestBody LocaleToggle b){return locales.setActive(b.code(),b.active(),b.note());}
 @PostMapping("/admin_locale_remove_rpc") public String localeRemove(@RequestBody LocaleRemove b){return locales.remove(b.code(),b.note());}
 @PostMapping("/get_order_evidence") public Map<String,Object> orderEvidence(@RequestBody EvidenceRead b){return evidence.getOrderEvidence(b.orderId());}
 @PostMapping("/log_privacy_call") public Map<String,Object> callLog(@RequestBody PrivacyCall b){return evidence.logPrivacyCall(b.orderId(),b.result());}
 @PostMapping("/issue_meetup_code") public String issueMeetup(@RequestBody IssueMeetup b){return meetup.issueMeetupCode(b.orderId());}
 @PostMapping("/confirm_meetup") public String confirmMeetup(@RequestBody ConfirmMeetup b){return meetup.confirmMeetup(b.orderId(),b.code(),b.lat(),b.lng(),b.city());}
 @PostMapping("/propose_shipping_fee") public String propose(@RequestBody ProposeFee b){return shipping.proposeShippingFee(b.orderId(),b.fee(),b.note());}
 @PostMapping("/respond_shipping_fee") public String respond(@RequestBody RespondFee b){return shipping.respondShippingFee(b.orderId(),b.accept(),b.counter(),b.expectedFee());}
 @PostMapping("/record_arrival_proof") public String arrival(@RequestBody ArrivalProof b){return ops.recordArrivalProof(b.tripId(),b.snapshotUrl(),b.lat(),b.lng(),b.city());}
}
