package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kuaixietech.mapper.account.AuthLoginMapper;
import com.kuaixietech.security.LocalHmacJwtService;
import com.kuaixietech.service.account.impl.AuthLoginService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AuthWorkflowTest {
  AuthLoginMapper mapper=mock(AuthLoginMapper.class);
  LocalHmacJwtService jwt=new LocalHmacJwtService("unit-test-only-signing-key-32-bytes-minimum");
  AuthLoginService service=new AuthLoginService(mapper,jwt,"","production","","");
  @Test void constructingAuthenticationDoesNotTouchDatabase(){verifyNoInteractions(mapper);}
  @Test void productionLoginCannotBootstrapUnknownAdmin(){
    when(mapper.findProfile("phone","13800138000","+86")).thenReturn(null);
    assertThrows(ResponseStatusException.class,()->service.login("phone","13800138000","test-password","+86"));
    verify(mapper,never()).insertAdmin(any(),any(),any(),any(),any(),any());
    verify(mapper,never()).savePassword(any(),any());
  }
  @Test void consumedCodeCannotIssueToken(){
    when(mapper.findProfile("phone","13800138000","+86")).thenReturn(Map.of("id","3f97f450-97d0-4686-a216-ef2a97de1bc6","is_admin",0));
    when(mapper.lockCode(any(),eq("phone"),eq("123456"),eq("signin"))).thenReturn("code");
    when(mapper.consumeCode("code")).thenReturn(0);
    assertThrows(ResponseStatusException.class,()->service.login("phone","13800138000","123456","+86"));
    verify(mapper,never()).savePassword(any(),any());
  }
}
