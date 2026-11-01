package com.kuaixietech;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.mapper.finance.InvoiceRpcMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.finance.impl.InvoiceRpcService;
import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UnverifiedInvoiceGuardTest {
 private final InvoiceRpcMapper db=mock(InvoiceRpcMapper.class);
 private final DomainAccessGuard guard=mock(DomainAccessGuard.class);
 private final LegacyAdminRpcService admin=mock(LegacyAdminRpcService.class);
 private final InvoiceRpcService service=new InvoiceRpcService(db,admin,guard);
 private final String id="invoice";
 private void pending(){when(guard.requireAdmin()).thenReturn("operator");when(db.lockInvoice(id)).thenReturn(Map.of("id",id,"user_id","buyer","status","pending","email","private@example.test"));}

 @Test void emptyFileCannotBeMarkedIssuedOrPromisedByEmail(){
  pending();assertEquals("ERR:INVOICE_ISSUER_UNAVAILABLE",service.adminIssueInvoice(id,true,"number","",""));
  verify(guard).requireAdmin();verify(db).lockInvoice(id);verifyNoMoreInteractions(db);verifyNoInteractions(admin);
 }
 @Test void arbitraryNumberAndHttpsUrlAreNotTaxInvoiceVerification(){
  pending();for(int i=0;i<2;i++)assertEquals("ERR:INVOICE_ISSUER_UNAVAILABLE",service.adminIssueInvoice(id,true,"123456789","https://example.test/some.pdf","manually checked"));
  verify(guard,times(2)).requireAdmin();verify(db,times(2)).lockInvoice(id);verifyNoMoreInteractions(db);verifyNoInteractions(admin);
 }
 @Test void genuineRejectionRemainsAvailableWithoutFalseIssuanceClaims(){
  pending();when(db.reject(id,"抬头资料有误")).thenReturn(1);
  assertEquals("OK:REJECTED",service.adminIssueInvoice(id,false,"","","  抬头资料有误  "));
  verify(guard).requireAdmin();verify(db).lockInvoice(id);verify(db).reject(id,"抬头资料有误");
  verify(admin).adminGuard("invoice_rejected","invoice",id,"抬头资料有误");
  verify(db).notifyUser(anyString(),eq("buyer"),eq("invoice_rejected"),eq("开票申请被驳回"),eq("抬头资料有误"));verifyNoMoreInteractions(db,admin);
 }
 @Test void invalidRejectionDoesNotWriteAnAuditSuccessOrSendMessages(){
  pending();assertEquals("ERR:NO_NOTE",service.adminIssueInvoice(id,false,"",""," "));
  assertEquals("ERR:BAD_NOTE",service.adminIssueInvoice(id,false,"","","x".repeat(256)));
  verify(guard,times(2)).requireAdmin();verify(db,times(2)).lockInvoice(id);verifyNoMoreInteractions(db);verifyNoInteractions(admin);
 }
 @Test void staleRejectionCannotNotifyAsIfItSucceeded(){
  pending();when(db.reject(id,"核对资料")).thenReturn(0);
  assertThrows(IllegalStateException.class,()->service.adminIssueInvoice(id,false,"","","核对资料"));
  verify(db,never()).notifyUser(anyString(),anyString(),anyString(),anyString(),anyString());
 }
 @Test void missingOrTerminalInvoiceDoesNotRecordIssuance(){
  pending();when(db.lockInvoice(id)).thenReturn(null);
  assertEquals("ERR:NO_TARGET",service.adminIssueInvoice(id,true,"n","",null));
  when(db.lockInvoice(id)).thenReturn(Map.of("status","issued"));
  assertEquals("ERR:ALREADY_SETTLED",service.adminIssueInvoice(id,true,"n","",null));
  verify(guard,times(2)).requireAdmin();verify(db,times(2)).lockInvoice(id);verifyNoMoreInteractions(db);verifyNoInteractions(admin);
 }
 @Test void untrustedActorCannotReadOrChangeInvoice(){
  when(guard.requireAdmin()).thenThrow(new SecurityException("denied"));
  assertThrows(SecurityException.class,()->service.adminIssueInvoice(id,true,"n","",null));verifyNoInteractions(db,admin);
 }
}
