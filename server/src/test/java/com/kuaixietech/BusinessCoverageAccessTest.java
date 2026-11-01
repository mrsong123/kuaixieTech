package com.kuaixietech;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.controller.BusinessCoverageController;
import com.kuaixietech.security.DomainAccessGuard;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
class BusinessCoverageAccessTest {
 @Test void nonAdminCannotReadInternalDeliveryMetadata(){var guard=mock(DomainAccessGuard.class);var json=mock(ObjectMapper.class);when(guard.requireAdmin()).thenThrow(new AccessDeniedException("admin required"));assertThrows(AccessDeniedException.class,()->new BusinessCoverageController(guard,json).coverage());verifyNoInteractions(json);}
 @Test void metadataIsExplicitlyUnverifiedRatherThanClaimingCompletion() throws Exception {var guard=mock(DomainAccessGuard.class);var result=new BusinessCoverageController(guard,new ObjectMapper()).coverage();assertEquals(82,result.path("suppliedTableCount").asInt());assertTrue(result.path("unverifiedFieldCount").asInt()>0);assertEquals(result.path("fieldCount").asInt(),result.path("fields").size());verify(guard).requireAdmin();}
}
