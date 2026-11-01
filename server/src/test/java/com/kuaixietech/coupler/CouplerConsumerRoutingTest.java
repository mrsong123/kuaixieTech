package com.kuaixietech.coupler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.kuaixietech.integration.*;
import com.kuaixietech.security.DomainAccessGuard;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static com.kuaixietech.coupler.CouplerModels.*;
class CouplerConsumerRoutingTest {
 final JdbcTemplate db=mock(JdbcTemplate.class);final CouplerService coupler=mock(CouplerService.class);
 IntegrationConfiguration config(){var c=new IntegrationConfiguration(db,mock(DomainAccessGuard.class),mock(IntegrationCipher.class),mock(IntegrationHttp.class));c.couplerRouting(coupler);return c;}
 @Test void selectedNewCredentialsDriveExistingSmsConsumer(){var values=Map.of("access_key_id","new-id","access_key_secret","new-secret","_coupler_config_revision","8");when(coupler.legacyNewCall("aliyun","sms")).thenReturn(new LegacyRoute(true,values));assertSame(values,config().runtime("aliyun","sms"));verifyNoInteractions(db);}
 @Test void managedDisabledDoesNotReadOldKeys(){when(coupler.legacyNewCall("aliyun","sms")).thenReturn(new LegacyRoute(true,null));assertNull(config().runtime("aliyun","sms"));verifyNoInteractions(db);}
 @Test void managedExistsSuppressesEnvironmentFallback(){when(coupler.managed("wechat","miniprogram")).thenReturn(true);assertTrue(config().exists("wechat","miniprogram"));verifyNoInteractions(db);}
 @Test void exactHistoricalRevisionIgnoresCurrentSelection(){var values=Map.of("access_key_secret","old-secret","_coupler_config_revision","3");when(coupler.historicalLive("aliyun","oss",3)).thenReturn(new LegacyRoute(true,values));assertSame(values,config().historicalLiveRuntime("aliyun","oss",3));verify(coupler,never()).legacyNewCall(anyString(),anyString());verifyNoInteractions(db);}
 @Test void missingHistoricalRevisionNeverFallsBack(){when(coupler.historicalLive("aliyun","oss",9)).thenReturn(new LegacyRoute(true,null));assertThrows(IllegalStateException.class,()->config().historicalLiveRuntime("aliyun","oss",9));verifyNoInteractions(db);}
 @Test void oldHistoricalReadDoesNotUseGlobalMode(){var configuration=spy(config());doReturn(Map.of("bucket","original")).when(configuration).legacyLiveRuntime("aliyun","oss");assertEquals("original",configuration.historicalLiveRuntime("aliyun","oss",0).get("bucket"));verifyNoInteractions(coupler);}
 @Test void professionalMerchantCredentialsRemainIndependent(){var configuration=spy(config());doReturn(Map.of("private_key_pem","merchant-original")).when(configuration).legacyLiveRuntime("credentials","merchant-1");assertEquals("merchant-original",configuration.runtime("credentials","merchant-1").get("private_key_pem"));verifyNoInteractions(coupler);}
 @Test void sandboxRejectsAllNewRealExternalEffects(){when(coupler.current()).thenReturn(new CouplerModels.Runtime(Mode.SANDBOX,1));var gate=new CouplerModeGate(coupler);for(String operation:List.of("private_upload","wechat_login","sms_dispatch","email_dispatch","verification_code","payment_quote","payment_create","payment_refund_dispatch","insurance_apply","fx_reference"))assertThrows(ResponseStatusException.class,()->gate.requireLiveNewCall(operation),operation);verify(coupler,never()).requireLegacyFeature(anyString());}
 @Test void liveDeliveryChecksFeatureSwitches(){when(coupler.current()).thenReturn(new CouplerModels.Runtime(Mode.LIVE,2));var gate=new CouplerModeGate(coupler);gate.requireLiveNewCall("sms_dispatch");gate.requireLiveNewCall("email_dispatch");gate.requireLiveNewCall("private_upload");verify(coupler).requireLegacyFeature("SMS");verify(coupler).requireLegacyFeature("EMAIL");verify(coupler).requireLegacyFeature("OSS");}
}
