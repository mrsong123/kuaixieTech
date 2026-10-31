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
  admin.adminGuard(approve?"invoice_issued":"invoice_rejected","invoice",invoiceId,!trim(invoiceNo).isEmpty()?trim(invoiceNo):!trim(reason).isEmpty()?trim(reason):"-");
  Map<String,Object> inv=mapper.lockInvoice(invoiceId);
  if(inv==null)return "ERR:NO_TARGET";
  if(!"pending".equals(inv.get("status")))return "ERR:ALREADY_SETTLED";
  if(approve&&trim(invoiceNo).isEmpty())return "ERR:NO_NOTE";
  if(!approve&&trim(reason).isEmpty())return "ERR:NO_NOTE";
  if(mapper.issue(invoiceId,approve,trim(invoiceNo),trim(fileUrl).isEmpty()?null:trim(fileUrl),trim(reason))!=1)throw new IllegalStateException("invoice write conflict");
  mapper.notifyUser(UUID.randomUUID().toString(),String.valueOf(inv.get("user_id")),approve?"invoice_issued":"invoice_rejected",approve?"电子发票已开具":"开票申请被驳回",approve?"发票号 "+trim(invoiceNo)+" 已开具并将送达邮箱 "+inv.get("email")+"，请在「我的发票」查看与重发。":trim(reason)+"。修改信息后可重新提交。");
  return approve?"OK:ISSUED":"OK:REJECTED";
 }
}
