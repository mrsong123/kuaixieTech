package com.kuaixietech.commerce;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public final class ProductCheckoutModels {
 private ProductCheckoutModels(){}
 public record Input(
  @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String productId,
  @NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String tripId,
  @NotNull @Min(1) @Max(100) Integer quantity,
  @NotBlank @Size(max=64) String contactName,@NotBlank @Pattern(regexp="\\+?[0-9 -]{6,24}") String contactPhone,
  @NotBlank @Size(max=512) String address,LocalDate expectDate,@Size(max=1024) String note,
  @NotBlank @Pattern(regexp="yes|waived") String insuranceChoice,
  @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String insuranceQuoteId,
  @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String requestId,
  @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal expectedUnitPrice,
  @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal expectedBuyerEscrow,
  @Pattern(regexp="[a-f0-9]{64}") String expectedProductFingerprint){}
 public record Quote(String productId,String tripId,String storeId,String itemName,String itemSpec,String unitPrice,int quantity,
  String goodsAmount,String serviceFee,String insurancePremium,String buyerEscrow,String vendorDeposit,String currency,String pricingVersion,int intentHours,String productFingerprint){}
}
