package com.kuaixietech.insurance;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
public final class InsuranceModels {
 private InsuranceModels(){}
 public record Product(String id,String insurerCode,String insurerName,String name,String kind,String adapter,
   String providerProductCode,String credentialPrefix,String termsUrl,String exclusionsUrl,String claimsUrl,
   Set<String> serviceHosts,Set<String> checkoutHosts,boolean enabled,long revision){}
 public record ProductInput(@Pattern(regexp="[a-zA-Z0-9_-]{1,64}") String id,@NotBlank @Size(max=64) String insurerCode,
   @NotBlank @Size(max=200) String insurerName,@NotBlank @Size(max=200) String name,@NotBlank @Size(max=64) String kind,
   @NotBlank @Size(max=64) String adapter,@NotBlank @Size(max=128) String providerProductCode,
   @NotBlank @Pattern(regexp="[A-Z][A-Z0-9_]{2,39}") String credentialPrefix,
   @NotBlank @Size(max=1000) String termsUrl,@NotBlank @Size(max=1000) String exclusionsUrl,@NotBlank @Size(max=1000) String claimsUrl,
   @NotNull @Size(min=1,max=10) Set<@Pattern(regexp="[a-z0-9.-]{1,253}") String> serviceHosts,
   @NotNull @Size(min=1,max=10) Set<@Pattern(regexp="[a-z0-9.-]{1,253}") String> checkoutHosts,boolean enabled){}
 public record QuoteInput(@NotBlank String productId,@NotBlank @Pattern(regexp="trip|pk_quote|order") String subjectType,@NotBlank String subjectId,@NotBlank @Size(max=255) String itemName,
   @NotBlank @Pattern(regexp="[0-9]{1,7}(\\.[0-9]{1,2})?") String declaredValue,@Pattern(regexp="web|h5|app|mp_weixin") String platform){}
 public record Quote(String id,String productId,String insurerName,String productName,String kind,String declaredValue,
   String premium,String currency,String termsUrl,String exclusionsUrl,String claimsUrl,String termsVersion,Instant expiresAt,
   String paymentMode,String protectionStatus){}
 public record ApplyInput(@NotBlank String orderId,@NotBlank String quoteId,
   @NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") String requestId,@AssertTrue boolean termsAccepted,@AssertTrue boolean disclosuresAccepted){}
 /** Native adapters MUST authenticate and validate their provider response before constructing these records. */
 public record ProviderQuote(String reference,BigDecimal declaredValue,BigDecimal premium,String currency,String termsVersion,Instant expiresAt){}
 public record OrderRisk(String orderId,String itemName,String itemSpec,int quantity,String unit,String declaredValue,
   String currency,String contactName,String contactPhone,String address,String tripId,String platform){}
 public record Checkout(String applicationRef,String url){}
 public record PolicyReceipt(String applicationRef,String applicationId,String orderId,String insurerCode,String providerProductCode,String state,
   String policyNumber,String coverageRef,BigDecimal declaredValue,BigDecimal premium,String currency,boolean premiumPaidDirectly,
   String termsVersion,Instant startsAt,Instant endsAt,String certificateUrl){}
 public record Application(String id,String orderId,String status,String insurerName,String productName,String premium,String currency,
   String checkoutUrl,String policyNumber,String certificateUrl,String claimsUrl,String coverageRef,String termsVersion,
   Instant startsAt,Instant endsAt,String platform,String paymentMode,String protectionStatus){}
 public interface Adapter {
  String id();String insurerCode();
  /** Local credential/config checks only; never call the provider while a trade lock is held. */
  boolean configured(Product product);
  /** Explicitly certified terminal capabilities; absence means no sale on that terminal. */
  default Set<String> supportedPlatforms(Product product){return Set.of();}
  ProviderQuote quote(Product product,QuoteInput input,String quoteReference);
  Checkout apply(Product product,String applicationId,OrderRisk risk,ProviderQuote quote);
  PolicyReceipt query(Product product,String applicationId,String providerApplicationRef);
  // Termination and claims links are hosted by the insurer; native API methods can be supplied per signed contract.
 }
}
