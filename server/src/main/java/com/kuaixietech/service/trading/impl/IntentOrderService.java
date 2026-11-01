package com.kuaixietech.service.trading.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.entity.portal.IntentInput;
import com.kuaixietech.mapper.trading.IntentOrderMapper;
import com.kuaixietech.security.DomainAccessGuard;
import com.kuaixietech.service.finance.impl.AtomicMoneyService;
import com.kuaixietech.service.trading.support.IntentPricing;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IntentOrderService {
  private com.kuaixietech.commerce.OrderProfileLocks profileLocks;
  @org.springframework.beans.factory.annotation.Autowired public void orderProfileLocks(com.kuaixietech.commerce.OrderProfileLocks locks){this.profileLocks=locks;}
  private final IntentOrderMapper db;
  private final DomainAccessGuard guard;
  private final AtomicMoneyService money;
  private final ObjectMapper json=new ObjectMapper();
  private final int intentHours;
  private final com.kuaixietech.insurance.InsuranceSelections insurance;
  public IntentOrderService(IntentOrderMapper db,DomainAccessGuard guard,AtomicMoneyService money,
      @Value("${app.orders.intent-hours:24}") int intentHours,com.kuaixietech.insurance.InsuranceSelections insurance){
    if(intentHours<1||intentHours>168)throw new IllegalArgumentException("intent-hours must be 1..168");
    this.db=db;this.guard=guard;this.money=money;this.intentHours=intentHours;this.insurance=insurance;
  }
  @Transactional(readOnly=true)
  public IntentPricing.Quote quote(IntentInput input){
    String buyer=guard.requireVerifiedBuyer();
    Map<String,Object> trip=available(db.trip(input.tripId()),buyer);
    validateTripInput(input,trip,db.reservedQuantityTotal(input.tripId()));
    validateInsurance(input,buyer);
    return price(input);
  }
  @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public Map<String,String> create(IntentInput input){
    String buyer=guard.requireVerifiedBuyer();
    if(input.requestId()==null||input.expectedBuyerEscrow()==null)
      throw fail(HttpStatus.BAD_REQUEST,"QUOTE_CONFIRMATION_AND_REQUEST_ID_REQUIRED");
    try{UUID.fromString(input.requestId());}catch(IllegalArgumentException e){throw fail(HttpStatus.BAD_REQUEST,"INVALID_REQUEST_ID");}
    // Serializes concurrent retries for this buyer before any order or balance mutation.
    if(profileLocks!=null)profileLocks.forTrip(buyer,input.tripId());
    BigDecimal balance=money.lockBalance(buyer);
    if(balance==null)throw fail(HttpStatus.NOT_FOUND,"ACCOUNT_UNAVAILABLE");
    if(!buyer.equals(guard.requireVerifiedBuyer()))throw fail(HttpStatus.FORBIDDEN,"ORDER_IDENTITY_CHANGED");
    String id=UUID.nameUUIDFromBytes(("intent:"+buyer+":"+input.requestId().toLowerCase(Locale.ROOT)).getBytes(StandardCharsets.UTF_8)).toString();
    String fingerprint=fingerprint(input);
    Map<String,Object> existing=db.lockExisting(id);
    if(existing!=null){
      try{
        if(!buyer.equalsIgnoreCase(Objects.toString(existing.get("buyer_id"),""))||!fingerprint.equals(json.readTree(String.valueOf(existing.get("agreed_json"))).path("request_hash").asText()))
          throw fail(HttpStatus.CONFLICT,"IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_INPUT");
      }catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException("Invalid existing order snapshot",e);}
      return Map.of("id",id,"status",String.valueOf(existing.get("status")));
    }
    Map<String,Object> trip=available(db.lockTrip(input.tripId()),buyer);
    int reserved=db.reservedQuantityTotal(input.tripId());
    validateTripInput(input,trip,reserved);
    validateInsurance(input,buyer);
    IntentPricing.Quote price=price(input);
    if(price.buyerEscrow().compareTo(input.expectedBuyerEscrow())!=0)
      throw fail(HttpStatus.CONFLICT,"PRICE_CHANGED_RECONFIRM_QUOTE");
    if(balance.compareTo(price.buyerEscrow())<0)throw fail(HttpStatus.CONFLICT,"INSUFFICIENT_BALANCE");
    String snapshot;
    try{snapshot=json.writeValueAsString(Map.of("pricing_version",IntentPricing.EXTERNAL_VERSION,"request_hash",fingerprint,"insurance_choice",input.insuranceChoice(),"insurance_quote_id",input.insuranceQuoteId()==null?"":input.insuranceQuoteId(),
      "insurance_escrow",BigDecimal.ZERO,"buyer_escrow",price.buyerEscrow()));}
    catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException(e);}
    changed(db.insertOrder(id,"DD"+id.replace("-",""),buyer,String.valueOf(trip.get("owner_id")),input,price,snapshot,intentHours));
    if(money.addBalance(buyer,price.buyerEscrow().negate(),"deposit_freeze",id,"下单托管：货款及5%服务费；保险由保险机构另行报价收款")==null)
      throw new IllegalStateException("Account disappeared while freezing escrow");
    changed(db.insertDeposit(UUID.randomUUID().toString(),id,buyer,price.buyerEscrow()));
    return Map.of("id",id,"status","requesting");
  }
  private Map<String,Object> available(Map<String,Object> trip,String buyer){
    if(trip==null)throw fail(HttpStatus.NOT_FOUND,"TRIP_UNAVAILABLE");
    if(buyer.equalsIgnoreCase(Objects.toString(trip.get("owner_id"),"")))throw fail(HttpStatus.BAD_REQUEST,"CANNOT_ORDER_OWN_TRIP");
    var normalized=new HashMap<String,Object>(trip);normalized.put("owner_id",Objects.toString(trip.get("owner_id"),"").toLowerCase(Locale.ROOT));return normalized;
  }
  private void validateTripInput(IntentInput input,Map<String,Object> trip,int reserved){
    int capacity=((Number)trip.get("capacity_qty")).intValue();
    int remaining=Math.min(((Number)trip.get("remaining_slots")).intValue(),capacity-reserved);
    if(input.quantity()==null||input.quantity()<1||input.quantity()>remaining)
      throw fail(HttpStatus.CONFLICT,"TRIP_CAPACITY_UNAVAILABLE");
    if(input.expectDate()!=null&&input.expectDate().isBefore(LocalDate.now()))
      throw fail(HttpStatus.BAD_REQUEST,"EXPECTED_DATE_IN_PAST");
    if(input.categoryId()!=null&&db.categoryAllowed(input.tripId(),input.categoryId())==0)
      throw fail(HttpStatus.BAD_REQUEST,"CATEGORY_NOT_ALLOWED_FOR_TRIP");
  }
  private IntentPricing.Quote price(IntentInput input){
    try{return IntentPricing.externalQuote(input.expectUnitPrice().multiply(BigDecimal.valueOf(input.quantity())),input.insuranceChoice());}
    catch(IllegalArgumentException e){throw fail(HttpStatus.BAD_REQUEST,e.getMessage());}
  }
  private void validateInsurance(IntentInput input,String buyer){
    if("yes".equals(input.insuranceChoice()))try{insurance.validate(buyer,input.insuranceQuoteId(),"trip",input.tripId(),input.expectUnitPrice().multiply(BigDecimal.valueOf(input.quantity())),input.itemName());}catch(IllegalArgumentException e){throw fail(HttpStatus.BAD_REQUEST,e.getMessage());}
    else if(input.insuranceQuoteId()!=null)throw fail(HttpStatus.BAD_REQUEST,"INSURANCE_SELECTION_CONFLICT");
  }
  private String fingerprint(IntentInput input){
    try{
      // Date is converted explicitly; no accidental dependence on Jackson's optional time module.
      java.util.List<Object> canonical=new java.util.ArrayList<>(Arrays.asList(input.tripId(),input.itemName(),input.itemSpec(),input.quantity(),input.unit(),
        input.categoryId(),input.expectUnitPrice().stripTrailingZeros(),input.note(),input.contactName(),input.contactPhone(),input.address(),
        input.expectDate()==null?null:input.expectDate().toString(),input.insuranceChoice(),input.expectedBuyerEscrow().stripTrailingZeros()));
      if(input.insuranceQuoteId()!=null)canonical.add(input.insuranceQuoteId());
      byte[] bytes=json.writeValueAsBytes(canonical);
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }catch(Exception e){throw new IllegalStateException("Cannot hash order request",e);}
  }
  private static void changed(int count){if(count!=1)throw new IllegalStateException("Unexpected order write count");}
  private static ResponseStatusException fail(HttpStatus status,String reason){return new ResponseStatusException(status,reason);}
}
