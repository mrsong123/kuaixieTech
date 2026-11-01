package com.kuaixietech.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Caller identity is exclusively derived from a verified Spring Security context. */
public final class TrustedActor {
  private TrustedActor() { }
  public static String currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
      throw new AccessDeniedException("Verified login required");
    // Until an authenticated identity provider is configured, only UUID-valued verified names are accepted.
    String id = auth.getName();
    if (id == null || !id.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
      throw new AccessDeniedException("Invalid authenticated subject");
    return java.util.UUID.fromString(id).toString();
  }
}
