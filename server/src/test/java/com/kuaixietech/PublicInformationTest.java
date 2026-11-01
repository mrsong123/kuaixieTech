package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.service.portal.impl.PublicInformationService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
class PublicInformationTest {
 @Test void missingLegalDocumentDoesNotInventTerms(){var db=mock(JdbcTemplate.class);var service=new PublicInformationService(db);when(db.queryForList(anyString(),eq("privacy"))).thenReturn(List.of());assertEquals(404,assertThrows(ResponseStatusException.class,()->service.legal("privacy")).getStatusCode().value());}
 @Test void invalidKeysTargetsAndLimitsNeverReachDatabase(){var db=mock(JdbcTemplate.class);var service=new PublicInformationService(db);assertThrows(ResponseStatusException.class,()->service.legal("../private"));assertThrows(ResponseStatusException.class,()->service.reviews("not-a-user-id",10));assertThrows(ResponseStatusException.class,()->service.announcements(101));verifyNoInteractions(db);}
 @Test void reviewProjectionExcludesIdentityAndFiltersHiddenAndInactiveAccounts(){var db=mock(JdbcTemplate.class);var service=new PublicInformationService(db);String id=UUID.randomUUID().toString();service.reviews(id,20);var sql=org.mockito.ArgumentCaptor.forClass(String.class);verify(db).queryForList(sql.capture(),eq(id),eq(20));String text=sql.getValue();assertTrue(text.contains("r.hidden=0"));assertTrue(text.contains("author.deleted_at IS NULL"));assertTrue(text.contains("target.deleted_at IS NULL"));String projection=text.split(" FROM ")[0];assertFalse(projection.contains("reviewer_id"));assertFalse(projection.contains("order_id"));}
 @Test void announcementReadsOnlyActiveRowsAndReturnsBooleanPin(){var db=mock(JdbcTemplate.class);var service=new PublicInformationService(db);Map<String,Object> row=new HashMap<>(Map.of("id","notice","pinned",1));when(db.queryForList(contains("WHERE active=1"),eq(20))).thenReturn(List.of(row));assertEquals(true,service.announcements(20).getFirst().get("pinned"));}
}
