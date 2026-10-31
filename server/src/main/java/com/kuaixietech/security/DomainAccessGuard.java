package com.kuaixietech.security;

import com.kuaixietech.mapper.account.ReadAccessMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Replaces a subset of PostgreSQL admin/party read guards. Apply to every sensitive API. */
@Service
public class DomainAccessGuard {
  private final ReadAccessMapper mapper;
  public DomainAccessGuard(ReadAccessMapper mapper) { this.mapper=mapper; }
  public String requireActiveProfile() {
    String uid=TrustedActor.currentUserId();
    if (!mapper.isActiveProfile(uid)) throw new AccessDeniedException("Account is not active");
    return uid;
  }
  public String requireAdmin() {
    String uid=requireActiveProfile();
    if (!mapper.isAdmin(uid)) throw new AccessDeniedException("Administrator required");
    return uid;
  }
  public String requireVerifiedCarrier() {
    String uid=requireActiveProfile();
    if (!mapper.isVerifiedCarrier(uid)) throw new AccessDeniedException("Verified carrier required");
    return uid;
  }
  public String requireOrderParticipant(String orderId) {
    String uid=requireActiveProfile();
    if (!mapper.isAdmin(uid) && !mapper.canReadOrder(orderId,uid))
      throw new AccessDeniedException("Not an order participant");
    return uid;
  }
}
