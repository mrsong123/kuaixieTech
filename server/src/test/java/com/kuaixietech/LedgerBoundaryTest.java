package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.mapper.finance.AtomicMoneyMapper;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
class LedgerBoundaryTest {
 @Test void overlongLedgerKindCannotMutateTheWallet(){var db=mock(AtomicMoneyMapper.class);assertThrows(IllegalArgumentException.class,()->new AtomicMoneyService(db).addBalance("user",BigDecimal.ONE,"recharge_refund_reserve",null,"note"));verifyNoInteractions(db);}
 @Test void exactRefundReserveIsRecordedWithTheSchemaSizedCategory(){var db=mock(AtomicMoneyMapper.class);when(db.lockBalance("user")).thenReturn(new BigDecimal("100.00"));when(db.setBalance("user",new BigDecimal("79.99"))).thenReturn(1);when(db.addLedger(any(),any(),any(),any(),any(),any(),any())).thenReturn(1);assertEquals(new BigDecimal("79.99"),new AtomicMoneyService(db).addBalance("user",new BigDecimal("-20.01"),"refund_reserve",null,"refund reference"));verify(db).addLedger(any(),eq("user"),eq("refund_reserve"),eq(new BigDecimal("-20.01")),eq(new BigDecimal("79.99")),isNull(),eq("refund reference"));}
}
