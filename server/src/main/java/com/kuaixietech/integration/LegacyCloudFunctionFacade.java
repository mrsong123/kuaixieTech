package com.kuaixietech.integration;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.stereotype.Service;
/** Internal migration seam. Business-specific request/response validation still depends on missing original cloud sources. */
@Service
public class LegacyCloudFunctionFacade {
  private final LegacyCloudFunctionBridge bridge;
  public LegacyCloudFunctionFacade(LegacyCloudFunctionBridge bridge){this.bridge=bridge;}
  public JsonNode inviteQr(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.INVITE_QR,request);}
  public JsonNode contactCodeLogin(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.CONTACT_CODE_LOGIN,request);}
  public JsonNode rtcCall(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.RTC_CALL,request);}
  public JsonNode notifyPush(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.NOTIFY_PUSH,request);}
  public JsonNode sendContactCode(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.SEND_CONTACT_CODE,request);}
  public JsonNode wechatPay(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.WECHAT_PAY,request);}
  public JsonNode translateMsg(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.TRANSLATE_MSG,request);}
  public JsonNode logisticsQuery(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.LOGISTICS_QUERY,request);}
  public JsonNode privacyCall(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.PRIVACY_CALL,request);}
  public JsonNode supportAiReply(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.SUPPORT_AI_REPLY,request);}
  public JsonNode channelTest(Map<String,?> request){return bridge.invoke(LegacyCloudFunction.CHANNEL_TEST,request);}
}
