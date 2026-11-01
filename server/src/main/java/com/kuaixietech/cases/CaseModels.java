package com.kuaixietech.cases;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public final class CaseModels {
 private CaseModels(){}
 public record Create(@NotBlank String orderId,@NotBlank @Pattern(regexp="quality|wrong_item|missing|damaged") String type,
   @NotBlank @Size(max=4000) String description,@NotBlank @Pattern(regexp="refund_part|refund_full|resend|compensate") String expectResolution,
   @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal refundAmount,
   @NotNull @Size(max=4) List<@NotBlank @Size(max=1000) String> evidenceUrls,@NotBlank String requestId){}
 public record Respond(@NotBlank @Size(max=1024) String note,@NotNull @Size(max=4) List<@NotBlank @Size(max=1000) String> evidenceUrls,@Min(1) int expectedRound,@NotBlank String requestId){}
 public record Escalate(@NotBlank @Size(max=1000) String reason,@Min(1) int expectedRound,@NotBlank String requestId){}
 public record Decision(@NotBlank @Pattern(regexp="reject|refund_part|refund_full|resend|compensate") String outcome,
   @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal refundAmount,@NotBlank @Size(max=1000) String reason,
   @NotBlank @Pattern(regexp="return_original|buyer|vendor|none") String depositOwner,@Min(1) int expectedRound,@NotBlank String requestId){}
 public record Appeal(@NotBlank @Size(max=4000) String reason,@Min(1) int expectedRound,@NotBlank String requestId){}
 public record Finish(@NotBlank @Size(max=1000) String note,@Min(1) int expectedRound,@NotBlank String requestId){}
}
