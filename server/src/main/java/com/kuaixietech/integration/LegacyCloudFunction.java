package com.kuaixietech.integration;
/** Eleven cloud functions listed in 云函数.md; no original implementation source was provided. */
public enum LegacyCloudFunction {
  INVITE_QR("invite-qr"), CONTACT_CODE_LOGIN("contact-code-login"), RTC_CALL("rtc-call"),
  NOTIFY_PUSH("notify-push"), SEND_CONTACT_CODE("send-contact-code"), WECHAT_PAY("wechat-pay"),
  TRANSLATE_MSG("translate-msg"), LOGISTICS_QUERY("logistics-query"), PRIVACY_CALL("privacy-call"),
  SUPPORT_AI_REPLY("support-ai-reply"), CHANNEL_TEST("channel-test");
  private final String key;
  LegacyCloudFunction(String key){this.key=key;}
  public String key(){return key;}
}
