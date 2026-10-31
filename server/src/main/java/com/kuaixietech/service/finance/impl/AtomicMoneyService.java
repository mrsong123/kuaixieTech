package com.kuaixietech.service.finance.impl;
import com.kuaixietech.mapper.finance.AtomicMoneyMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Port of add_balance. Internal-only trusted accounting primitive; never expose as a client-controlled API. */
@Service
public class AtomicMoneyService {
  private final AtomicMoneyMapper mapper;
  public AtomicMoneyService(AtomicMoneyMapper mapper) {this.mapper=mapper;}
  public BigDecimal lockBalance(String uid) {return uid==null?null:mapper.lockBalance(uid);}
  @Transactional
  public BigDecimal addBalance(String uid,BigDecimal delta,String kind,String orderId,String note) {
    if(uid==null)return null;
    BigDecimal balance=mapper.lockBalance(uid);
    if(balance==null)return null;
    BigDecimal amount=delta==null?BigDecimal.ZERO:delta;
    BigDecimal after=balance.add(amount).setScale(2,RoundingMode.HALF_UP);
    if(mapper.setBalance(uid,after)!=1)throw new IllegalStateException("Balance update was not unique");
    String category=kind==null||kind.isEmpty()?"adjust":kind;
    if(mapper.addLedger(UUID.randomUUID().toString(),uid,category,amount,after,orderId,note)!=1)
      throw new IllegalStateException("Ledger insert failed");
    return after;
  }
}
