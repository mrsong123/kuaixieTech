package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaixietech.mapper.account.AccountCentreMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.account.impl.AccountCentreService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AccountCentreTest {
  AccountCentreMapper db;DomainAccessGuard guard;AccountCentreService service;
  @BeforeEach void init(){
    db=mock(AccountCentreMapper.class);guard=mock(DomainAccessGuard.class);
    when(guard.requireActiveProfile()).thenReturn("signed-in-user");
    service=new AccountCentreService(db,guard);
  }
  @Test void personalOrderQueriesAlwaysUseSessionIdentity(){
    service.orders("buyer",20,0);
    verify(db).orders("signed-in-user","buyer",20,0);
    when(db.order("signed-in-user","someone-elses-order")).thenReturn(null);
    assertEquals(404,assertThrows(ResponseStatusException.class,()->service.order("someone-elses-order")).getStatusCode().value());
  }
  @Test void invalidPaginationAndRoleCannotReachQueries(){
    assertThrows(ResponseStatusException.class,()->service.orders("all",101,0));
    assertThrows(ResponseStatusException.class,()->service.orders("admin",20,0));
    assertThrows(ResponseStatusException.class,()->service.ledger(20,-1));
    verifyNoInteractions(db);
  }
  @Test void alreadyReadNotificationSucceedsButSomeoneElsesNotificationDoesNot(){
    when(db.ownsNotification("signed-in-user","own-read")).thenReturn(1);
    assertEquals("OK",service.readNotification("own-read").get("status"));
    assertEquals(404,assertThrows(ResponseStatusException.class,()->service.readNotification("foreign")).getStatusCode().value());
  }
  @Test void profileUpdateCannotEraseUnspecifiedFields(){
    when(db.profile("signed-in-user")).thenReturn(Map.of("id","signed-in-user","nickname","name"));
    service.updateProfile(" name ",null);
    verify(db).updateProfile("signed-in-user","name",null);
    assertThrows(ResponseStatusException.class,()->service.updateProfile("   ",null));
  }
}
