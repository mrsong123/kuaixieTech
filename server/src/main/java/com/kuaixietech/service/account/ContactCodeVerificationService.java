package com.kuaixietech.service.account;
public interface ContactCodeVerificationService {
 boolean verifyPrelogin(String target, String channel, String code);
}
