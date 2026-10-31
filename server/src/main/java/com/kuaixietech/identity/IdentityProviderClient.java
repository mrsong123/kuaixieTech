package com.kuaixietech.identity;

/**
 * Adapter contract for the REAL credential authority (IdP), not a local simulation.
 * Implementations MUST atomically verify the old secret, update its stored hash,
 * invalidate existing sessions as appropriate, and enforce contact uniqueness.
 * Password values must never be logged, returned, or persisted to t_profiles.
 */
public interface IdentityProviderClient {
    enum Outcome { OK, BAD_OLD, NO_PASSWORD_SET, TAKEN, NOT_FOUND }
    /** Resolves a canonical login identifier, never returns raw credentials. */
    String resolveUserByLoginIdentifier(String loginIdentifier);
    Outcome changePassword(String authenticatedUserId, String oldPassword, String newPassword);
    Outcome resetPassword(String resolvedUserId, String newPassword, String operationId);
    Outcome rebindContact(String authenticatedUserId, String channel, String canonicalTarget, String loginIdentifier, String operationId);
}
