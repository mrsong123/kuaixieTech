package com.kuaixietech.refunds;
import jakarta.validation.constraints.*;
import java.math.BigInteger;
public final class RefundModels {
 private RefundModels(){}
 public record Create(@NotBlank @Pattern(regexp="[a-f0-9]{32}") String paymentId,@NotBlank @Pattern(regexp="(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}") String requestId,@NotBlank @Size(max=200) String reason){}
 public record Review(boolean approve,@NotBlank @Size(max=200) String reason){}
 /** All values are bound to the original verified recharge and its original merchant snapshot. */
 public record Command(String refundId,String paymentId,String transactionId,String currency,BigInteger totalMinor,BigInteger refundMinor,String reason){}
 public record Receipt(String refundId,String paymentId,String transactionId,String providerRefundId,String currency,BigInteger totalMinor,BigInteger refundMinor,String status){}
}
