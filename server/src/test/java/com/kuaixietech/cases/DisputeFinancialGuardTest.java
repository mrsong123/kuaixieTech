package com.kuaixietech.cases;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.mapper.order_fulfillment.*;
import com.kuaixietech.service.finance.impl.*;
import com.kuaixietech.service.order_fulfillment.impl.*;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
class DisputeFinancialGuardTest {
 static final String ACTOR="822de014-ae8d-485f-bd06-cd972ac00970";
 @BeforeEach void login(){SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(ACTOR,"unit",List.of()));}
 @AfterEach void logout(){SecurityContextHolder.clearContext();}
 @Test void administratorCannotCancelThroughAnOpenCaseAndLocksOrderFirst(){var db=mock(CancellationRpcMapper.class);var money=mock(AtomicMoneyService.class);var admin=mock(LegacyAdminRpcService.class);var rewards=mock(RewardSettlementService.class);var s=new CancellationRpcService(db,money,mock(CancellationTierMigrationService.class),rewards,admin,new ObjectMapper());when(db.lockOrder("order")).thenReturn(Map.of("id","order"));when(db.activeCases("order")).thenReturn(List.of("case"));assertEquals("ERR:DISPUTE_OPEN",s.adminResolveCancel("order",true,"approve"));var sequence=inOrder(db);sequence.verify(db).lockOrder("order");sequence.verify(db).activeCases("order");verify(db,never()).latestPending(any());verifyNoInteractions(money,admin,rewards);}
 @Test void buyerCannotCancelThroughOpenAftersales(){var db=mock(CancellationRpcMapper.class);var money=mock(AtomicMoneyService.class);var tiers=mock(CancellationTierMigrationService.class);var s=new CancellationRpcService(db,money,tiers,mock(RewardSettlementService.class),mock(LegacyAdminRpcService.class),new ObjectMapper());when(db.lockOrder("order")).thenReturn(Map.of("buyer_id",ACTOR,"vendor_id","merchant","status","purchased"));when(db.activeAfterSales("order")).thenReturn(List.of("after"));assertEquals("ERR:DISPUTE_OPEN",s.requestCancelV2("order","other",false,"note","[]"));verifyNoInteractions(money,tiers);verify(db,never()).markDepositRows(any(),any());}
 @Test void shippingCannotProgressThroughOpenCase(){var db=mock(OrderProofMigrationMapper.class);var money=mock(AtomicMoneyService.class);var s=new OrderProofMigrationService(db,money,new ObjectMapper());when(db.lockOrder("order")).thenReturn(Map.of("vendor_id",ACTOR,"status","purchased"));when(db.activeCases("order")).thenReturn(List.of("case"));assertEquals("ERR:DISPUTE_OPEN",s.confirmShip("order","carrier","tracking","[]",null,null,null,null));verify(db,never()).saveShipment(any(),any(),any(),any(),any(),any(),any(),any(),any());verifyNoInteractions(money);}
 @Test void noBoardingRefundCannotBypassAftersales(){var db=mock(OrderProofMigrationMapper.class);var money=mock(AtomicMoneyService.class);var s=new OrderProofMigrationService(db,money,new ObjectMapper());when(db.lockOrder("order")).thenReturn(Map.of("buyer_id",ACTOR,"status","locked"));when(db.activeAfterSales("order")).thenReturn(List.of("after"));assertEquals("ERR:DISPUTE_OPEN",s.requestNoBoardingRefund("order"));verify(db,never()).cancelNoBoarding(any());verifyNoInteractions(money);}
}
