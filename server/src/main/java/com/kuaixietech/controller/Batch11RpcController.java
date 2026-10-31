package com.kuaixietech.controller;
import com.kuaixietech.service.order_fulfillment.impl.CancellationRpcService;
import com.kuaixietech.service.order_fulfillment.impl.PeerRpcService;
import com.kuaixietech.service.account.impl.IdentityCodesRpcService;
import java.util.Map;
import com.kuaixietech.service.order_fulfillment.impl.OrderProofMigrationService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.*;
/** Protected endpoints: /api/rpc/* remains fail-closed until verified Spring Security identity provider is configured. */
@RestController @RequestMapping("/api/rpc")
public class Batch11RpcController {
 private final OrderProofMigrationService proofs;private final CancellationRpcService cancellations;private final PeerRpcService peers;private final IdentityCodesRpcService codes;
 public Batch11RpcController(OrderProofMigrationService proofs,CancellationRpcService cancellations,PeerRpcService peers,IdentityCodesRpcService codes){this.proofs=proofs;this.cancellations=cancellations;this.peers=peers;this.codes=codes;}
 public record Purchase(String orderId,String evidence,BigDecimal lat,BigDecimal lng,String city,String address,String meta){}
 public record Shipment(String orderId,String carrier,String tracking,String labelUrls,BigDecimal lat,BigDecimal lng,String city,String address){}
 public record Boarding(String tripId,String snapshotUrl,BigDecimal lat,BigDecimal lng,String city){}
 public record Delivery(String orderId,String faceId,BigDecimal lat,BigDecimal lng,String city){}
 public record OrderId(String orderId){}
 public record Cancel(String orderId,String reasonType,boolean forceMajeure,String note,String proofUrls){}
 public record Decision(String orderId,boolean approve,String reason){}
 public record PeerId(String target){}
 public record VerifyContact(String target,String channel,String code){}
 public record InviteCode(String code){}
 @PostMapping("/confirm_purchase") public String purchase(@RequestBody Purchase x){return proofs.confirmPurchase(x.orderId(),x.evidence(),x.lat(),x.lng(),x.city(),x.address(),x.meta());}
 @PostMapping("/confirm_ship") public String ship(@RequestBody Shipment x){return proofs.confirmShip(x.orderId(),x.carrier(),x.tracking(),x.labelUrls(),x.lat(),x.lng(),x.city(),x.address());}
 @PostMapping("/record_boarding_proof") public String boarding(@RequestBody Boarding x){return proofs.recordBoardingProof(x.tripId(),x.snapshotUrl(),x.lat(),x.lng(),x.city());}
 @PostMapping("/record_delivery_proof") public String delivery(@RequestBody Delivery x){return proofs.recordDeliveryProof(x.orderId(),x.faceId(),x.lat(),x.lng(),x.city());}
 @PostMapping("/request_no_boarding_refund") public String refund(@RequestBody OrderId x){return proofs.requestNoBoardingRefund(x.orderId());}
 @PostMapping("/request_cancel_v2") public String cancel(@RequestBody Cancel x){return cancellations.requestCancelV2(x.orderId(),x.reasonType(),x.forceMajeure(),x.note(),x.proofUrls());}
 @PostMapping("/admin_resolve_cancel") public String resolve(@RequestBody Decision x){return cancellations.adminResolveCancel(x.orderId(),x.approve(),x.reason());}
 @PostMapping("/open_peer_thread") public Map<String,Object> openThread(@RequestBody OrderId x){return peers.openPeerThread(x.orderId());}
 @PostMapping("/get_peer_profile") public Map<String,Object> getPeer(@RequestBody PeerId x){return peers.getPeerProfile(x.target());}
 @PostMapping("/verify_contact_code") public boolean verifyContact(@RequestBody VerifyContact x){return codes.verifyContactCode(x.target(),x.channel(),x.code());}
 @PostMapping("/consume_invite_code") public boolean consumeInvite(@RequestBody InviteCode x){return codes.consumeInviteCode(x.code());}
}
