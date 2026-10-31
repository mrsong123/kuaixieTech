package com.kuaixietech.service.account;

/** Mirrors the four production auth RPCs, with a verified IdP as the credential authority. */
public interface IdentitySettingsService {
    String changePassword(String oldPassword,String newPassword);
    String resetOwnPasswordByCode(String channel,String target,String code,String newPassword);
    String rebindContact(String channel,String newTarget,String code);
    /** Server-only replacement for the service_role reset RPC. Requires OTP verification here. */
    boolean resetContactPasswordAfterVerifiedCode(String target,String channel,String code,String newPassword);
}
