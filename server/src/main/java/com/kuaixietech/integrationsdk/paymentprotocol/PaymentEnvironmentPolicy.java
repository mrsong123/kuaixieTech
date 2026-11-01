package com.kuaixietech.integrationsdk.paymentprotocol;
import java.net.URI;
/** Explicit provider environments. No DNS rewriting, live fallback, or sandbox result admission to the live wallet. */
public final class PaymentEnvironmentPolicy {
 private PaymentEnvironmentPolicy(){}
 public static URI alipayGateway(String mode){return switch(mode){case "LIVE"->URI.create("https://openapi.alipay.com/gateway.do");case "SANDBOX"->URI.create("https://openapi-sandbox.dl.alipaydev.com/gateway.do");default->throw new IllegalArgumentException("PAYMENT_MODE_INVALID");};}
 public static void requireWechatPayLive(String mode){if(!"LIVE".equals(mode))throw new IllegalStateException("WECHAT_PAY_OFFICIAL_SANDBOX_UNAVAILABLE");}
 public static void requireLiveAccounting(String mode){if(!"LIVE".equals(mode))throw new IllegalStateException("SANDBOX_RESULT_CANNOT_UPDATE_LIVE_LEDGER");}
 /** Historical merchant snapshots in this project predate mode and are exclusively real-production integrations. */
 public static String originalPaymentMode(java.util.Map<String,String> snapshotSettings){String mode=snapshotSettings.get("_api_mode");if(mode==null||mode.isBlank())return "LIVE";if(!mode.equals("LIVE")&&!mode.equals("SANDBOX"))throw new IllegalArgumentException("PAYMENT_SNAPSHOT_MODE_INVALID");return mode;}
}
