package com.kuaixietech.commerce;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.security.DomainAccessGuard;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
class StoreOrderAttributionTest {
 @Test void ownerReceivesOnlyOwnAttributionWithoutBuyerContacts(){var db=mock(JdbcTemplate.class);var guard=mock(DomainAccessGuard.class);when(guard.requireActiveProfile()).thenReturn("owner");new StoreOrderAttributionController(db,guard).mine(20,0);verify(db).queryForList(argThat(sql->sql.contains("s.owner_id=?")&&sql.contains("s.owner_id=r.vendor_id")&&sql.contains("r.stock_state")&&!sql.contains("contact_phone")&&!sql.contains("buyer_id")&&!sql.contains("snapshot")),eq("owner"),eq(20),eq(0));}
 @Test void adminPermissionIsRequiredBeforeAnyCrossStoreRead(){var db=mock(JdbcTemplate.class);var guard=mock(DomainAccessGuard.class);when(guard.requireAdmin()).thenThrow(new SecurityException());assertThrows(SecurityException.class,()->new StoreOrderAttributionController(db,guard).admin("store",20,0));verifyNoInteractions(db);}
 @Test void invalidPaginationDoesNotReadBusinessData(){var db=mock(JdbcTemplate.class);var guard=mock(DomainAccessGuard.class);assertThrows(ResponseStatusException.class,()->new StoreOrderAttributionController(db,guard).mine(101,0));verifyNoInteractions(db);}
}
