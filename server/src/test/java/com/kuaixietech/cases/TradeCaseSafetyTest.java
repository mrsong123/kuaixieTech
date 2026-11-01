package com.kuaixietech.cases;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.controller.InviteSummaryController;
import com.kuaixietech.mapper.finance.RewardSettlementMapper;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.finance.impl.RewardSettlementService;
import com.kuaixietech.service.growth.InviteTierMigrationService;
import com.kuaixietech.service.growth.impl.LegacyScoreRpcService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static com.kuaixietech.cases.CaseModels.*;
class TradeCaseSafetyTest {
 static final String REQUEST="123e4567-e89b-42d3-a456-426614174000";
 final JdbcTemplate db=mock(JdbcTemplate.class);final DomainAccessGuard guard=mock(DomainAccessGuard.class);
 final ObjectMapper json=new ObjectMapper();final List<String> writes=new ArrayList<>();final List<Object[]> arguments=new ArrayList<>();
 Map<String,Object> order=new HashMap<>(Map.of("id","order","buyer_id","buyer","vendor_id","merchant","status","purchased","goods_amount",new BigDecimal("100.00")));
 Map<String,Object> entry=new HashMap<>(Map.of("id","case","order_id","order","initiator_id","buyer","respondent_id","merchant","stage","platform_review","round",2,"description","商品损坏","reopened_used",false,"evidence_urls","[]","respondent_evidence_urls","[]"));
 Map<String,Object> previous=null,replay=null;List<Map<String,Object>> otherRefunds=List.of();boolean active=false;String queried="";
 TradeCaseService service(){when(guard.requireActiveProfile()).thenReturn("buyer");when(guard.requireAdmin()).thenReturn("admin");
  when(db.queryForList(anyString(),any(Object[].class))).thenAnswer(call->{String sql=call.getArgument(0);queried=sql;
   if(sql.startsWith("SELECT id,buyer_id"))return List.of(order);
   if(sql.startsWith("SELECT order_id FROM"))return List.of(Map.of("order_id","order"));
   if(sql.startsWith("SELECT *,negotiation"))return List.of(entry);
   if(sql.startsWith("SELECT * FROM t_disputes"))return List.of();
   if(sql.startsWith("SELECT action,JSON_UNQUOTE"))return replay==null?List.of():List.of(replay);
   if(sql.startsWith("SELECT refund_status"))return previous==null?List.of():List.of(previous);
   if(sql.startsWith("SELECT id,refund_amount"))return otherRefunds;
   if(sql.startsWith("SELECT id FROM t_disputes"))return active?List.of(Map.of("id","other")):List.of();
   return List.of();});
  when(db.update(anyString(),any(Object[].class))).thenAnswer(call->{writes.add(call.getArgument(0));arguments.add((Object[])call.getRawArguments()[1]);return 1;});
  return new TradeCaseService(db,guard,json,"uploads.example.com",48);
 }
 Create create(){return new Create("order","damaged","商品损坏","refund_part",new BigDecimal("30.00"),List.of(),REQUEST);}
 Decision decision(){return new Decision("refund_part",new BigDecimal("30.00"),"核实损坏","return_original",2,REQUEST);}
 void code(String expected,Runnable action){var e=assertThrows(ResponseStatusException.class,action::run);assertEquals(expected,e.getReason());assertTrue(writes.isEmpty());}
 @Test void wrongBuyerCannotCreate(){var s=service();order.put("buyer_id","someone_else");code("DISPUTE_ORDER_UNAVAILABLE",()->s.create(create()));}
 @Test void cannotCreateBeforePurchase(){var s=service();order.put("status","paid");code("DISPUTE_ORDER_NOT_ELIGIBLE",()->s.create(create()));}
 @Test void oneOpenCasePerOrder(){var s=service();active=true;code("DISPUTE_ALREADY_OPEN",()->s.create(create()));}
 @Test void textOnlyClaimIsAllowedAndEventIsRecorded(){var s=service();assertEquals("negotiating",s.create(create()).get("stage"));assertEquals(3,writes.size());assertTrue(writes.stream().anyMatch(sql->sql.startsWith("INSERT INTO t_dispute_events")));assertFalse(writes.stream().anyMatch(sql->sql.contains("wallet")||sql.contains("money_ledger")));}
 @Test void arbitraryEvidenceHostIsRejected(){var s=service();code("DISPUTE_EVIDENCE_HOST_REJECTED",()->s.create(new Create("order","damaged","损坏","resend",null,List.of("https://attacker.example/item"),REQUEST)));}
 @Test void evidenceUserInfoIsRejected(){var s=service();code("DISPUTE_EVIDENCE_HOST_REJECTED",()->s.create(new Create("order","damaged","损坏","resend",null,List.of("https://fake@uploads.example.com/item"),REQUEST)));}
 @Test void refundCannotExceedGoods(){var s=service();code("DISPUTE_AMOUNT_INVALID",()->s.create(new Create("order","damaged","损坏","refund_part",new BigDecimal("100.01"),List.of(),REQUEST)));}
 @Test void staleRoundCannotOverwriteDecision(){var s=service();entry.put("round",3);code("DISPUTE_CHANGED_REFRESH",()->s.decide("case",decision()));}
 @Test void operatorPermissionIsRequiredBeforeReadingCase(){var s=service();when(guard.requireAdmin()).thenThrow(new SecurityException("denied"));assertThrows(SecurityException.class,()->s.decide("case",decision()));verifyNoInteractions(db);}
 @Test void outsiderCannotReply(){var s=service();when(guard.requireActiveProfile()).thenReturn("outsider");code("DISPUTE_UNAVAILABLE",()->s.respond("case",new Respond("note",List.of(),2,REQUEST)));}
 @Test void refundDecisionOnlyQueuesActualFinancialWork(){var s=service();assertEquals("awaiting_refund",s.decide("case",decision()).get("stage"));int index=0;for(;index<writes.size();index++)if(writes.get(index).startsWith("INSERT INTO t_after_sales_cases"))break;assertTrue(index<writes.size());assertEquals("provider_unavailable",arguments.get(index)[7]);assertEquals(new BigDecimal("30.00"),arguments.get(index)[8]);assertTrue(writes.stream().noneMatch(sql->sql.contains("t_wallet")||sql.contains("t_money_ledger")||sql.contains("t_goods_insurance")));}
 @Test void combinedRefundsCannotExceedGoodsValue(){var s=service();otherRefunds=List.of(Map.of("id","prior","refund_amount",new BigDecimal("80.00")));code("DISPUTE_REFUND_CAP_EXCEEDED",()->s.decide("case",decision()));}
 @Test void processingRefundCannotBeOverwritten(){var s=service();previous=Map.of("refund_status","processing");code("DISPUTE_REFUND_RECONCILIATION_REQUIRED",()->s.decide("case",decision()));}
 @Test void paidRefundCannotBeAppealed(){var s=service();entry.put("stage","awaiting_refund");previous=Map.of("refund_status","succeeded");code("DISPUTE_REFUND_RECONCILIATION_REQUIRED",()->s.appeal("case",new Appeal("复核",2,REQUEST)));}
 @Test void onlyOneAppealIsAllowed(){var s=service();entry.put("stage","resolved");entry.put("reopened_used",1);code("DISPUTE_APPEAL_UNAVAILABLE",()->s.appeal("case",new Appeal("复核",2,REQUEST)));}
 @Test void appealHoldsUnpaidRefund(){var s=service();entry.put("stage","awaiting_refund");previous=Map.of("refund_status","provider_unavailable");assertEquals("appeal",s.appeal("case",new Appeal("复核",2,REQUEST)).get("stage"));assertTrue(writes.stream().anyMatch(sql->sql.contains("SET status='on_hold'")));}
 @Test void merchantCannotConfirmOwnRemedy(){var s=service();when(guard.requireActiveProfile()).thenReturn("merchant");entry.put("stage","awaiting_fulfilment");code("DISPUTE_BUYER_REQUIRED",()->s.finish("case",new Finish("已完成",2,REQUEST)));}
 @Test void buyerCannotMarkPendingRefundCompleted(){var s=service();entry.put("stage","awaiting_refund");code("DISPUTE_STAGE_REJECTED",()->s.finish("case",new Finish("已完成",2,REQUEST)));}
 @Test void buyerCanConfirmActualReplacement(){var s=service();entry.put("stage","awaiting_fulfilment");assertEquals("resolved",s.finish("case",new Finish("已收到补发",2,REQUEST)).get("stage"));assertTrue(writes.stream().noneMatch(sql->sql.contains("refund_status='succeeded'")));}
 @Test void repeatResponseDoesNotDuplicateEvidenceOrNotification()throws Exception{var s=service();var input=new Respond("说明",List.of(),2,REQUEST);var node=json.valueToTree(input);var hash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(node)));replay=Map.of("action","responded","request_hash",hash);entry.put("round",3);assertEquals("case",s.respond("case",input).get("id"));assertTrue(writes.isEmpty());}
 @Test void reusedRequestCannotChangeBody(){var s=service();replay=Map.of("action","responded","request_hash","different");code("DISPUTE_REQUEST_REUSED",()->s.respond("case",new Respond("changed",List.of(),2,REQUEST)));}
 @Test void inviteSummaryNeverRecalculatesOrWrites(){var s=service();new InviteSummaryController(db,guard);when(db.queryForList(startsWith("SELECT p.invite_tier"),any(Object[].class))).thenReturn(List.of(Map.of("tier","base","valid_count",2)));assertEquals(2,new InviteSummaryController(db,guard).tier().get("valid_count"));verify(db).queryForList(contains("child.invited_by=p.id"),eq("buyer"));assertTrue(writes.isEmpty());}
 @Test void activeCaseBlocksSettlementBeforeAnyMoneyMovement(){var mapper=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);when(mapper.lockOrder("order")).thenReturn(Map.of("status","shipped","buyer_id","buyer","vendor_id","merchant"));when(mapper.activeCases("order")).thenReturn(List.of("case"));var s=new RewardSettlementService(mapper,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),json);assertThrows(CaseOpenException.class,()->s.settleOrderCompletion("order"));verifyNoInteractions(money);verify(mapper,never()).markOrderSettled(any());}
 @Test void standaloneAftersalesAlsoBlocksSettlement(){var mapper=mock(RewardSettlementMapper.class);var money=mock(AtomicMoneyService.class);when(mapper.lockOrder("order")).thenReturn(Map.of("status","shipped","buyer_id","buyer","vendor_id","merchant"));when(mapper.activeAfterSales("order")).thenReturn(List.of("after"));var s=new RewardSettlementService(mapper,money,mock(LegacyScoreRpcService.class),mock(InviteTierMigrationService.class),json);assertThrows(CaseOpenException.class,()->s.settleOrderCompletion("order"));verifyNoInteractions(money);verify(mapper,never()).markOrderSettled(any());}
}
