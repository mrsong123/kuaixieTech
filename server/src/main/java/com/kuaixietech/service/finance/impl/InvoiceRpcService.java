package com.kuaixietech.service.finance.impl;

import com.kuaixietech.service.operations.impl.LegacyAdminRpcService;
import com.kuaixietech.mapper.finance.InvoiceRpcMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.security.TrustedActor;
import java.util.*;
import java.math.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Implements request_invoice and admin_issue_invoice, final migration 20261021_184844. */
@Service public class InvoiceRpcService {
 private final InvoiceRpcMapper mapper; private final LegacyAdminRpcService admin; private final DomainAccessGuard guard;
 public InvoiceRpcService(InvoiceRpcMapper mapper, LegacyAdminRpcService admin, DomainAccessGuard guard){this.mapper=mapper;this.admin=admin;this.guard=guard;}
 private static String trim(String x){return x==null?"":x.trim();}
 @Transactional public String requestInvoice(String orderId,String titleType,String title,String taxNo,String kind,String email,String remark){
  String uid=TrustedActor.currentUserId();
  Map<String,Object> o=mapper.lockOrder(orderId);
  if(o==null)return "ERR:NO_ORDER";
  if(!uid.equals(o.get("buyer_id")))return "ERR:NOT_BUYER";
  if(!"completed".equals(o.get("status")))return "ERR:BAD_STATUS";
  if(trim(title).isEmpty())return "ERR:NO_TITLE";
  if("company".equals(titleType)&&trim(taxNo).length()<10)return "ERR:NO_TAXNO";
  if(!trim(email).matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$"))return "ERR:BAD_EMAIL";
  if(mapper.invoiceExists(orderId)>0)return "ERR:ALREADY";
  if(!Set.of("personal","company").contains(titleType)||!Set.of("electronic_general","vat_special").contains(kind))return "ERR:BAD_KIND";
  BigDecimal price=o.get("final_price")==null?(BigDecimal)o.get("goods_amount"):(BigDecimal)o.get("final_price");
  BigDecimal fee=(BigDecimal)o.get("service_fee");
  BigDecimal amount=(price==null?BigDecimal.ZERO:price).add(fee==null?BigDecimal.ZERO:fee).setScale(2,RoundingMode.HALF_UP);
  if(amount.signum()<=0)return "ERR:BAD_AMOUNT";
  String e=trim(email).toLowerCase(Locale.ROOT);
  mapper.createInvoice(UUID.randomUUID().toString(),orderId,uid,titleType,trim(title),trim(taxNo).isEmpty()?null:trim(taxNo),kind,amount,e,trim(remark).isEmpty()?null:trim(remark));
  mapper.recordEmail(uid,e);
  return "OK:INVOICE:"+amount.toPlainString();
 }
 @Transactional public String adminIssueInvoice(String invoiceId,boolean approve,String invoiceNo,String fileUrl,String reason){
  guard.requireAdmin();
  Map<String,Object> inv=mapper.lockInvoice(invoiceId);
  if(inv==null)return "ERR:NO_TARGET";
  if(!"pending".equals(inv.get("status")))return "ERR:ALREADY_SETTLED";
  // A number and a caller-supplied URL do not establish a genuine tax invoice.
  // Neither issuer integration nor external invoice verification/email delivery
  // exists here, so never label it issued or promise that an email was sent.
  if(approve)return "ERR:INVOICE_ISSUER_UNAVAILABLE";
  if(trim(reason).isEmpty())return "ERR:NO_NOTE";
  if(trim(reason).length()>255)return "ERR:BAD_NOTE";
  admin.adminGuard("invoice_rejected","invoice",invoiceId,trim(reason));
  if(mapper.reject(invoiceId,trim(reason))!=1)throw new IllegalStateException("invoice write conflict");
  mapper.notifyUser(UUID.randomUUID().toString(),String.valueOf(inv.get("user_id")),"invoice_rejected","开票申请被驳回",trim(reason));
  return "OK:REJECTED";
 }
}
