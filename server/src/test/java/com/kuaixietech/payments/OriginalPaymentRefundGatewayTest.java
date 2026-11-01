package com.kuaixietech.payments;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.integration.IntegrationCredentials;
import com.kuaixietech.refunds.RefundModels.*;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static com.kuaixietech.payments.PaymentModels.*;
class OriginalPaymentRefundGatewayTest {
 static final String ID="b".repeat(32),PAYMENT="a".repeat(32);final ObjectMapper json=new ObjectMapper();
 OriginalPaymentRefundGateway gateway(){return new OriginalPaymentRefundGateway(mock(WechatNativeAdapter.class),mock(AlipayPageAdapter.class),mock(PaymentSecrets.class),mock(PaymentHttp.class));}
 Command command(){return new Command(ID,PAYMENT,"transaction","CNY",new BigInteger("10100"),new BigInteger("10100"),"Original recharge refund");}
 String wx(String status){return "{\"out_refund_no\":\""+ID+"\",\"out_trade_no\":\""+PAYMENT+"\",\"transaction_id\":\"transaction\",\"refund_id\":\"provider-refund\",\"status\":\""+status+"\",\"amount\":{\"currency\":\"CNY\",\"total\":10100,\"refund\":10100}}";}
 @Test void nativeReceiptMustBindOriginalPaymentAndWholeChargedAmount()throws Exception{var g=gateway();assertEquals("succeeded",g.wechatReceipt(command(),json.readTree(wx("SUCCESS"))).status());assertThrows(IllegalStateException.class,()->g.wechatReceipt(command(),json.readTree(wx("SUCCESS").replace("10100","10000"))));assertThrows(IllegalStateException.class,()->g.wechatReceipt(command(),json.readTree(wx("SUCCESS").replace("transaction\"","other\""))));}
 @Test void abnormalIsStillUnconfirmedFundsRatherThanReleased()throws Exception{assertEquals("processing",gateway().wechatReceipt(command(),json.readTree(wx("ABNORMAL"))).status());}
 @Test void unknownWechatStatusCannotClaimSuccess()throws Exception{assertThrows(IllegalStateException.class,()->gateway().wechatReceipt(command(),json.readTree(wx("UNKNOWN"))));}
 @Test void alipayRequiresExplicitRefundSuccess()throws Exception{String valid="{\"out_request_no\":\""+ID+"\",\"out_trade_no\":\""+PAYMENT+"\",\"trade_no\":\"transaction\",\"refund_amount\":\"101.00\",\"refund_status\":\"REFUND_SUCCESS\"}";var g=gateway();assertEquals("succeeded",g.alipayReceipt(command(),json.readTree(valid)).status());assertThrows(IllegalStateException.class,()->g.alipayReceipt(command(),json.readTree(valid.replace("REFUND_SUCCESS",""))));assertThrows(IllegalStateException.class,()->g.alipayReceipt(command(),json.readTree(valid.replace("REFUND_SUCCESS","REFUND_PROCESSING"))));assertThrows(IllegalStateException.class,()->g.alipayReceipt(command(),json.readTree(valid.substring(0,valid.length()-1)+",\"total_amount\":\"100.00\"}")));assertThrows(IllegalStateException.class,()->g.alipayReceipt(command(),json.readTree(valid.replace("101.00","100.00"))));}
 @Test void badPemIsDetectedBeforeDispatchClaim(){var credentials=mock(IntegrationCredentials.class);when(credentials.required(any(),any())).thenReturn("identifier");when(credentials.optional(any(),eq("PRIVATE_KEY_PEM"))).thenReturn("invalid-pem");var wechat=mock(WechatNativeAdapter.class);when(wechat.configured(any())).thenReturn(true);var http=mock(PaymentHttp.class);var g=new OriginalPaymentRefundGateway(wechat,mock(AlipayPageAdapter.class),new PaymentSecrets(credentials),http);var merchant=new Merchant("merchant","WeChat","wechat_native_v3","wechat","web",List.of("CNY"),"WECHAT_MAIN",0,300,true,1,Map.of());assertFalse(g.configured(merchant));assertThrows(IllegalStateException.class,()->g.prepare(merchant,command()));verifyNoInteractions(http);}
 @Test void longChineseReasonUsesWholeCodepointsWithinProviderLimit(){String full="申请退回未使用的充值金额。".repeat(15);String provider=OriginalPaymentRefundGateway.wechatReason(full);assertTrue(provider.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=80);assertTrue(full.startsWith(provider));assertFalse(provider.isBlank());assertFalse(Character.isHighSurrogate(provider.charAt(provider.length()-1)));}
}
