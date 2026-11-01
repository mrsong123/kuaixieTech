package com.kuaixietech.payments;

import java.math.*;
import java.time.Instant;
import java.util.*;
import jakarta.validation.constraints.*;

public final class PaymentModels {
  private PaymentModels(){}
  public record MerchantInput(String id,@NotBlank @Size(max=80) String name,@NotBlank String adapter,
    @NotBlank String method,@NotBlank String platform,@NotEmpty List<String> currencies,
    @Pattern(regexp="[A-Z][A-Z0-9_]{2,39}") String credentialPrefix,
    @Min(0) @Max(2000) int spreadBps,@Min(30) @Max(900) int quoteTtlSeconds,boolean enabled,
    Map<String,String> settings){}
  public record Merchant(String id,String name,String adapter,String method,String platform,List<String> currencies,
    String credentialPrefix,int spreadBps,int quoteTtlSeconds,boolean enabled,long revision,Map<String,String> settings){}
  public record FxInput(String id,@NotBlank @Size(max=80) String name,@NotBlank String kind,
    String endpoint,@Pattern(regexp="[A-Z][A-Z0-9_]{2,39}") String credentialPrefix,
    @Min(60) @Max(172800) int maxAgeSeconds,boolean enabled,boolean transactionEnabled){}
  public record FxSource(String id,String name,String kind,String endpoint,String credentialPrefix,int maxAgeSeconds,boolean enabled,boolean transactionEnabled){}
  public record Rate(String base,String currency,BigDecimal value,String source,Instant asOf,Instant validUntil){}
  public record QuoteInput(@NotBlank String merchantId,@NotBlank String paymentCurrency,
    @NotBlank @Pattern(regexp="[0-9]{1,7}(\\.[0-9]{1,2})?") String amountCny){}
  public record ConfirmInput(@NotBlank String quoteId,@NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String requestId){}
  public record Quote(String id,String merchantId,String merchantName,String method,String platform,String baseCurrency,
    String baseAmount,String paymentCurrency,String paymentAmount,String rate,int spreadBps,String rateSource,
    Instant rateAsOf,Instant expiresAt,String settlementCurrency){}
  public record Checkout(String kind,String value){}
  public record Payment(String id,String status,String baseCurrency,String baseAmount,String paymentCurrency,
    String paymentAmount,String merchantName,Checkout checkout){}
  public record ProviderPayment(String reference,String transactionId,String currency,BigInteger amount,boolean paid){}
  public interface Adapter {
    String id();
    Set<String> methods();
    Set<String> platforms();
    /** Empty means currency support is supplied by the verified gateway contract, not unrestricted. */
    Set<String> currencies();
    boolean configured(Merchant merchant);
    default void validatePayer(Merchant merchant,String user){}
    default Set<String> availableCurrencies(Merchant merchant){return currencies();}
    Checkout create(Merchant merchant,String reference,String currency,BigInteger amount,BigInteger baseCnyMinor,Instant expiresAt);
    ProviderPayment query(Merchant merchant,String reference);
  }
}
