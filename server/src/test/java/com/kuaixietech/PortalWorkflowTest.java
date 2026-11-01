package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kuaixietech.entity.portal.PortalDtos;
import com.kuaixietech.mapper.portal.PortalMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.portal.impl.PortalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

class PortalWorkflowTest {
  DomainAccessGuard guard;JdbcTemplate db;PortalMapper mapper;PortalServiceImpl service;
  @BeforeEach void init(){
    guard=mock(DomainAccessGuard.class);db=mock(JdbcTemplate.class);mapper=mock(PortalMapper.class);
    service=new PortalServiceImpl(mapper,guard,db,"uploads.example.com");
  }
  @Test void unverifiedCarrierCannotPublishTrip(){
    when(guard.requireVerifiedCarrier()).thenThrow(new AccessDeniedException("verification required"));
    assertThrows(AccessDeniedException.class,()->service.createTrip(new PortalDtos.TripCreate("Tokyo","Shanghai","2099-01-01","2099-01-02","international","air",2)));
    verifyNoInteractions(db);
  }
  @Test void returnBeforeDepartureCannotWriteTrip(){
    when(guard.requireVerifiedCarrier()).thenReturn("carrier");
    assertThrows(ResponseStatusException.class,()->service.createTrip(new PortalDtos.TripCreate("Tokyo","Shanghai",LocalDate.now().plusDays(3).toString(),LocalDate.now().plusDays(2).toString(),"international","air",2)));
    verifyNoInteractions(db);
  }
  @Test void submittedDemandIsHiddenUntilReviewAndUsesCurrentReviewTable(){
    when(guard.requireActiveProfile()).thenReturn("buyer");
    var result=service.createDemand(new PortalDtos.DemandCreate("Camera","Sealed box","Shanghai",new BigDecimal("1000"),"international"));
    assertEquals("pending_review",result.get("status"));
    var order=inOrder(guard,db);
    order.verify(guard).requireActiveProfile();
    order.verify(db).update(contains("'open','hidden'"),eq(result.get("id")),eq("Camera"),eq("Sealed box"),eq("buyer"),eq("international"),eq("Shanghai"),eq(new BigDecimal("1000")));
    order.verify(db).update(contains("INSERT INTO t_portal_demand_reviews"),eq(result.get("id")),eq("buyer"));
  }
  @Test void duplicateDemandReviewCannotPublishDemand(){
    when(guard.requireAdmin()).thenReturn("admin");
    when(db.update(anyString(),any(Object[].class))).thenReturn(0);
    var error=assertThrows(ResponseStatusException.class,()->service.reviewDemand(new PortalDtos.ReviewDemand("demand",true)));
    assertEquals(409,error.getStatusCode().value());
    verify(db,never()).update(startsWith("UPDATE t_demands"),any(Object[].class));
  }
  @Test void nonAdminCannotReviewAndUntrustedMediaCannotBePublished(){
    when(guard.requireAdmin()).thenThrow(new AccessDeniedException("admin required"));
    assertThrows(AccessDeniedException.class,()->service.reviewTrip(new PortalDtos.ReviewTrip("trip",true)));
    when(guard.requireActiveProfile()).thenReturn("author");
    assertThrows(ResponseStatusException.class,()->service.createSocialPost(new PortalDtos.SocialPostCreate("post","image","https://other.example.com/file.jpg",null)));
    verifyNoInteractions(db);
  }
}
