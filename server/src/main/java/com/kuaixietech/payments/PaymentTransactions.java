package com.kuaixietech.payments;

import java.math.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import static com.kuaixietech.payments.PaymentModels.*;

@Service
public class PaymentTransactions {
  private final PaymentStore store;private final AtomicMoneyService money;
  public PaymentTransactions(PaymentStore store,AtomicMoneyService money){this.store=store;this.money=money;}
  @Transactional
  public Map<String,Object> reserve(String user,ConfirmInput input){
    Map<String,Object> existing=store.row("SELECT * FROM t_currency_payments WHERE user_id=? AND request_id=? FOR UPDATE",user,input.requestId().toLowerCase(Locale.ROOT));
    if(existing!=null){if(!input.quoteId().equals(PaymentStore.s(existing,"quote_id")))throw new IllegalArgumentException("IDEMPOTENCY_KEY_REUSED");return existing;}
    Map<String,Object> quote=store.row("SELECT * FROM t_payment_quotes WHERE id=? AND user_id=? FOR UPDATE",input.quoteId(),user);
    if(quote==null)throw new IllegalArgumentException("QUOTE_NOT_FOUND");
    existing=store.row("SELECT * FROM t_currency_payments WHERE quote_id=? FOR UPDATE",input.quoteId());
    if(existing!=null)return existing;
    if(!instant(quote.get("expires_at")).isAfter(Instant.now()))throw new IllegalArgumentException("QUOTE_EXPIRED");
    Merchant merchant=store.merchant(PaymentStore.s(quote,"merchant_id"));
    if(merchant==null||!merchant.enabled()||merchant.revision()!=((Number)quote.get("merchant_revision")).longValue())throw new IllegalArgumentException("MERCHANT_CHANGED_REQUOTE");
    String id=UUID.nameUUIDFromBytes(("payment:"+user+":"+input.requestId().toLowerCase(Locale.ROOT)).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString().replace("-","");
    store.db.update("INSERT INTO t_currency_payments(id,user_id,request_id,quote_id,merchant_id,merchant_snapshot,base_minor,payment_currency,payment_minor,payment_minor_digits,status) VALUES(?,?,?,?,?,?,?,?,?,?,'creating')",
      id,user,input.requestId().toLowerCase(Locale.ROOT),input.quoteId(),quote.get("merchant_id"),quote.get("merchant_snapshot"),quote.get("base_minor"),quote.get("payment_currency"),quote.get("payment_minor"),quote.get("payment_minor_digits"));
    return store.payment(id);
  }
  @Transactional
  public boolean settle(String id,ProviderPayment receipt){
    Map<String,Object> payment=store.row("SELECT * FROM t_currency_payments WHERE id=? FOR UPDATE",id);
    if(payment==null)throw new IllegalArgumentException("PAYMENT_NOT_FOUND");
    if(!"CNY".equals(payment.get("base_currency")))throw new IllegalArgumentException("NON_CNY_WALLET_CREDIT_REJECTED");
    if(!id.equals(receipt.reference())||!PaymentStore.s(payment,"payment_currency").equals(receipt.currency())||
      new BigDecimal(payment.get("payment_minor").toString()).toBigIntegerExact().compareTo(receipt.amount())!=0)throw new IllegalArgumentException("PAYMENT_RECEIPT_MISMATCH");
    if(!receipt.paid())return false;
    if(receipt.transactionId()==null||receipt.transactionId().isBlank()||receipt.transactionId().length()>128)throw new IllegalArgumentException("PROVIDER_TRANSACTION_REQUIRED");
    if("paid".equals(payment.get("status"))){
      if(!receipt.transactionId().equals(payment.get("provider_transaction_id")))throw new IllegalArgumentException("PROVIDER_TRANSACTION_MISMATCH");return true;
    }
    if(store.db.update("INSERT INTO t_currency_payment_receipts(id,payment_id,merchant_id,provider_transaction_id,payment_currency,payment_minor,payment_minor_digits,base_minor) VALUES(?,?,?,?,?,?,?,?)",
      UUID.randomUUID().toString(),id,payment.get("merchant_id"),receipt.transactionId(),receipt.currency(),receipt.amount(),payment.get("payment_minor_digits"),payment.get("base_minor"))!=1)throw new IllegalStateException("RECEIPT_NOT_PERSISTED");
    if(store.db.update("UPDATE t_currency_payments SET status='paid',provider_transaction_id=?,paid_at=CURRENT_TIMESTAMP(3) WHERE id=? AND status IN('creating','pending')",receipt.transactionId(),id)!=1)throw new IllegalStateException("PAYMENT_STATE_CONFLICT");
    BigDecimal cny=new BigDecimal(payment.get("base_minor").toString()).movePointLeft(2).setScale(2,RoundingMode.UNNECESSARY);
    if(money.addBalance(PaymentStore.s(payment,"user_id"),cny,"fx_recharge",null,"验证渠道收款后按锁定报价充值人民币；支付单 "+id)==null)throw new IllegalStateException("WALLET_UNAVAILABLE");
    return true;
  }
  static Instant instant(Object value){
    if(value instanceof Timestamp timestamp)return timestamp.toInstant();
    if(value instanceof LocalDateTime date)return date.toInstant(ZoneOffset.UTC);
    if(value instanceof Instant instant)return instant;
    throw new IllegalStateException("INVALID_DATABASE_TIMESTAMP");
  }
}
