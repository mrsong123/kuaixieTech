package com.kuaixietech.entity.portal;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Price, fees, ownership and order status are never accepted from the caller. */
public record IntentInput(
  @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String tripId,
  @NotBlank @Size(max=120) String itemName,
  @Size(max=160) String itemSpec,
  @NotNull @Min(1) @Max(100) Integer quantity,
  @NotBlank @Pattern(regexp="piece|box|bottle|kg") String unit,
  @Pattern(regexp="[0-9]{1,18}") String categoryId,
  @NotNull @DecimalMin("0.01") @DecimalMax("1000000") @Digits(integer=7,fraction=2) BigDecimal expectUnitPrice,
  @Size(max=1024) String note,
  @NotBlank @Size(max=64) String contactName,
  @NotBlank @Pattern(regexp="\\+?[0-9 -]{6,24}") String contactPhone,
  @NotBlank @Size(max=512) String address,
  LocalDate expectDate,
  @NotBlank @Pattern(regexp="yes|waived") String insuranceChoice,
  @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String requestId,
  @DecimalMin("0.01") @Digits(integer=8,fraction=2) BigDecimal expectedBuyerEscrow,
  @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String insuranceQuoteId
){
 public IntentInput(String tripId,String itemName,String itemSpec,Integer quantity,String unit,String categoryId,BigDecimal expectUnitPrice,String note,String contactName,String contactPhone,String address,LocalDate expectDate,String insuranceChoice,String requestId,BigDecimal expectedBuyerEscrow){this(tripId,itemName,itemSpec,quantity,unit,categoryId,expectUnitPrice,note,contactName,contactPhone,address,expectDate,insuranceChoice,requestId,expectedBuyerEscrow,null);}
}
