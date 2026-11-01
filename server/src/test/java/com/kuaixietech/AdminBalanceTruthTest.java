package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.math.BigDecimal;
import com.kuaixietech.mapper.operations.LegacyAdminRpcMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.growth.impl.LegacyScoreRpcService;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import org.junit.jupiter.api.Test;
class AdminBalanceTruthTest {
 @Test void missingWalletCannotProduceSuccessNoticeOrCompletedAudit(){
  var db=mock(LegacyAdminRpcMapper.class);var guard=mock(DomainAccessGuard.class);var money=mock(AtomicMoneyService.class);when(guard.requireAdmin()).thenReturn("operator");
  var service=new LegacyAdminRpcService(db,guard,mock(LegacyScoreRpcService.class),money);
  assertThrows(IllegalStateException.class,()->service.adjustBalance("missing",BigDecimal.ONE,"correction"));verifyNoInteractions(db);
 }
 @Test void selfAdjustmentIsCaseInsensitiveAndDoesNotWriteAudit(){
  var db=mock(LegacyAdminRpcMapper.class);var guard=mock(DomainAccessGuard.class);var money=mock(AtomicMoneyService.class);when(guard.requireAdmin()).thenReturn("ABC");
  var service=new LegacyAdminRpcService(db,guard,mock(LegacyScoreRpcService.class),money);
  assertEquals("ERR:CANNOT_SELF",service.adjustBalance("abc",BigDecimal.ONE,"correction"));verifyNoInteractions(db,money);
 }
 @Test void successfulAdjustmentReportsTheActualCommittedBalance(){
  var db=mock(LegacyAdminRpcMapper.class);var guard=mock(DomainAccessGuard.class);var money=mock(AtomicMoneyService.class);when(guard.requireAdmin()).thenReturn("operator");
  when(money.addBalance(eq("target"),eq(BigDecimal.ONE),eq("adjust"),isNull(),anyString())).thenReturn(new BigDecimal("41.20"));
  var service=new LegacyAdminRpcService(db,guard,mock(LegacyScoreRpcService.class),money);
  assertEquals("OK:BALANCE:41.20",service.adjustBalance("target",BigDecimal.ONE,"correction"));
  verify(db).notifyUser(anyString(),eq("target"),eq("balance_adjusted"),anyString(),contains("41.20"),isNull());
 }
}
