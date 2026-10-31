package com.kuaixietech.entity.portal;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Portal transport DTOs. Never trust caller-supplied user identities. */
public final class PortalDtos {
 private PortalDtos(){}
 public record DemandCreate(@NotBlank @Size(max=200) String title,@Size(max=4000) String description,
   @Size(max=64) String city,@DecimalMin("0.0") BigDecimal expectPrice,@Pattern(regexp="any|domestic|international") String tripType){}
 public record TripCreate(@NotBlank @Size(max=128) String destination,@NotBlank @Size(max=64) String originCity,
   @NotBlank String departDate,@NotBlank String returnDate,
   @Pattern(regexp="domestic|international") String tripType,
   @Pattern(regexp="road|rail|air|other") String transportMode,
   @Min(1) @Max(100) Integer capacityQty){}
 public record SocialPostCreate(@NotBlank @Size(max=4000) String caption,
   @Pattern(regexp="text|image|video") String mediaKind,@Size(max=800) String mediaUrl,@Size(max=800) String posterUrl){}
 public record ReviewPost(@NotBlank String postId,boolean approved){}
 public record ReviewDemand(@NotBlank String demandId,boolean approved){}
 public record ReviewTrip(@NotBlank String tripId,boolean approved){}
}
