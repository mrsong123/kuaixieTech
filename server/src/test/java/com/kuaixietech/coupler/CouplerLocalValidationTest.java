package com.kuaixietech.coupler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.integration.EmailProtocols;
import com.kuaixietech.integrationsdk.PublicServiceAdapters;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static com.kuaixietech.coupler.CouplerModels.*;
class CouplerLocalValidationTest {
 final CouplerCatalog catalog=new CouplerCatalog(new ObjectMapper());final PublicServiceAdapters publicApis=mock(PublicServiceAdapters.class);final EmailProtocols email=mock(EmailProtocols.class);final CouplerAdapterRegistry registry=new CouplerAdapterRegistry(publicApis,email);
 @Test void aliyunOssAllowsOnlyOfficialRegionalEndpoint(){var p=catalog.provider("OSS","aliyun");assertDoesNotThrow(()->registry.validate(p,Mode.LIVE,1,Map.of("bucket","my-images","endpoint","https://oss-cn-hangzhou.aliyuncs.com/")));for(String bad:new String[]{"https://oss-cn-hangzhou.aliyuncs.com.attacker.test","https://example.com","http://oss-cn-hangzhou.aliyuncs.com"})assertThrows(IllegalArgumentException.class,()->registry.validate(p,Mode.LIVE,1,Map.of("bucket","my-images","endpoint",bad)));verifyNoInteractions(publicApis,email);}
 @Test void unsupportedSandboxFailsBeforeConsumerValidation(){assertThrows(IllegalArgumentException.class,()->registry.validate(catalog.provider("EMAIL","aliyun"),Mode.SANDBOX,1,Map.of()));verifyNoInteractions(email);}
 @Test void emailNativeLocalValidationUsesActualProtocol(){var values=Map.of("access_key_id","id");registry.validate(catalog.provider("EMAIL","aliyun"),Mode.LIVE,1,values);verify(email).validateConfiguration(values);}
 @Test void featureValuesAreStrictBooleans(){registry.validate(catalog.provider("FEATURES","flags"),Mode.SANDBOX,1,Map.of("sms_enabled","false"));assertThrows(IllegalArgumentException.class,()->registry.validate(catalog.provider("FEATURES","flags"),Mode.LIVE,1,Map.of("sms_enabled","1")));}
}
