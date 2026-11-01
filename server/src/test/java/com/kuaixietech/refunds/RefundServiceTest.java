package com.kuaixietech.refunds;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.payments.*;
import com.kuaixietech.security.DomainAccessGuard;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
class RefundServiceTest {
 final RefundStore store=mock(RefundStore.class);final RefundTransactions transactions=mock(RefundTransactions.class);final DomainAccessGuard guard=mock(DomainAccessGuard.class);final OriginalPaymentRefundGateway gateway=mock(OriginalPaymentRefundGateway.class);
 final Map<String,Object> refund=new HashMap<>(Map.of("id","b".repeat(32),"payment_id","a".repeat(32),"user_id","buyer","status","approved","base_currency","CNY","base_minor",new BigDecimal("10000"),"payment_currency","CNY","payment_minor",new BigDecimal("10100"),"provider_transaction_id","transaction","reason","unused"));
 RefundService service(){when(guard.requireActiveProfile()).thenReturn("buyer");when(guard.requireAdmin()).thenReturn("admin");when(store.refund(any())).thenReturn(refund);return new RefundService(store,transactions,guard,gateway);}
 @Test void unconfiguredProviderDoesNotConsumeDispatchOwnership(){var s=service();when(gateway.configured(any())).thenReturn(false);assertThrows(ResponseStatusException.class,()->s.dispatch(refund.get("id").toString()));verifyNoInteractions(transactions);}
 @Test void ambiguousDispatchCannotBeSubmittedAgain(){var s=service();when(gateway.configured(any())).thenReturn(true);when(transactions.claim(any(),any())).thenAnswer(c->{refund.put("status","dispatching");return refund;});when(gateway.submit(any(),any())).thenThrow(new IllegalStateException("timeout"));s.dispatch(refund.get("id").toString());assertThrows(ResponseStatusException.class,()->s.dispatch(refund.get("id").toString()));verify(gateway,times(1)).submit(any(),any());verify(transactions).unconfirmed(any());}
 @Test void refreshIsQueryOnly(){var s=service();refund.put("status","dispatching");when(gateway.query(any(),any())).thenThrow(new IllegalStateException("unavailable"));s.refresh(refund.get("id").toString(),false);verify(gateway).query(any(),any());verify(gateway,never()).submit(any(),any());verify(transactions,never()).claim(any(),any());}
 @Test void nonOwnerCannotInspectOrRefreshAnotherRefund(){var s=service();when(guard.requireActiveProfile()).thenReturn("other");assertThrows(ResponseStatusException.class,()->s.detail(refund.get("id").toString(),false));assertThrows(ResponseStatusException.class,()->s.refresh(refund.get("id").toString(),false));verifyNoInteractions(gateway,transactions);}
 @Test void adminPermissionPrecedesAnyProviderOrDatabaseOperation(){var s=service();when(guard.requireAdmin()).thenThrow(new SecurityException());assertThrows(SecurityException.class,()->s.dispatch(refund.get("id").toString()));verifyNoInteractions(store,gateway,transactions);}
 @Test void eventTimelineIsOwnerScopedAndExcludesOperatorIdentity(){var db=mock(JdbcTemplate.class);when(guard.requireActiveProfile()).thenReturn("buyer");when(db.queryForList(startsWith("SELECT * FROM t_payment_refunds"),any(Object[].class))).thenReturn(List.of(refund));when(db.queryForList(startsWith("SELECT id,action,detail"),any(Object[].class))).thenReturn(List.of(Map.of("id","event","action","requested","detail","reserved","createdAt",java.sql.Timestamp.from(java.time.Instant.parse("2026-01-01T00:00:00Z")))));var s=new RefundService(new RefundStore(db,new ObjectMapper()),transactions,guard,gateway);var events=s.events(refund.get("id").toString(),false,20,0);assertFalse(events.getFirst().containsKey("actor_id"));assertEquals("2026-01-01T00:00:00Z",events.getFirst().get("createdAt"));when(guard.requireActiveProfile()).thenReturn("other");assertThrows(ResponseStatusException.class,()->s.events(refund.get("id").toString(),false,20,0));}
 @Test void publicProjectionNeverExposesMerchantSnapshotOrProviderTransaction(){var s=service();refund.put("merchant_snapshot","secret-reference");var view=s.detail(refund.get("id").toString(),false);assertFalse(view.containsKey("merchant_snapshot"));assertFalse(view.containsKey("provider_transaction_id"));assertEquals("100.00",view.get("baseAmount"));assertEquals("101.00",view.get("paymentAmount"));}
}
