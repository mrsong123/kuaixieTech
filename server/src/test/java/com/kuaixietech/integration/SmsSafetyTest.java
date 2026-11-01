package com.kuaixietech.integration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
class SmsSafetyTest {
 final ObjectMapper json=new ObjectMapper();
 SmsProtocols protocols(){var http=mock(IntegrationHttp.class);when(http.endpoint(anyString(),eq(true))).thenAnswer(c->URI.create(c.getArgument(0)));return new SmsProtocols(http,json);}
 @Test void malformedOrMismatchedReceiptsNeverConfirm()throws Exception{var s=protocols();assertThrows(IllegalStateException.class,()->s.parse("aliyun","+8613800138000","vc-1",json.readTree("{}")));assertThrows(IllegalStateException.class,()->s.parse("aliyun","+8613800138000","vc-1",json.readTree("{\"Code\":\"OK\",\"RequestId\":\"r\"}")));assertThrows(IllegalStateException.class,()->s.parse("tencent","+8613800138000","vc-1",json.readTree("{\"Response\":{\"RequestId\":\"r\",\"SendStatusSet\":[{\"Code\":\"Ok\",\"PhoneNumber\":\"+8613800138000\",\"SessionContext\":\"other\",\"SerialNo\":\"s\"}]}}")));}
 @Test void explicitProviderRejectionIsNotSuccess()throws Exception{assertFalse(protocols().parse("aliyun","+8613800138000","vc-1",json.readTree("{\"Code\":\"isv.BUSINESS_LIMIT_CONTROL\",\"RequestId\":\"r\"}")).accepted());}
 @Test void aliyunMainlandProductCannotPretendInternationalSupport(){assertThrows(IllegalArgumentException.class,()->protocols().aliyun(Map.of(),"+12025550123","123456","vc-1",Instant.now(),"nonce"));}
 @Test void providerAcceptanceDoesNotClaimHandsetDelivery()throws Exception{var receipt=protocols().parse("tencent","+8613800138000","vc-1",json.readTree("{\"Response\":{\"RequestId\":\"r\",\"SendStatusSet\":[{\"Code\":\"Ok\",\"PhoneNumber\":\"+8613800138000\",\"SessionContext\":\"vc-1\",\"SerialNo\":\"serial\"}]}}"));assertTrue(receipt.accepted());assertEquals("serial",receipt.providerReference());}
 @Test void ambiguousDispatchIsNeverRetriedAutomatically(){var config=mock(IntegrationConfiguration.class);var store=mock(SmsOutboxStore.class);var protocols=mock(SmsProtocols.class);when(config.runtime("aliyun","sms")).thenReturn(null);when(config.runtime("tencent","sms")).thenReturn(Map.of("secret_key","secret"));when(store.claim()).thenReturn(Map.of("id",7L,"target","+12025550123","code","123456"));when(protocols.send(any(),anyMap(),any(),any(),any())).thenThrow(new IllegalStateException("timeout"));assertEquals("SMS_DISPATCH_UNCONFIRMED",assertThrows(IllegalStateException.class,()->new SmsOutboxDispatcher(config,store,protocols).run()).getMessage());verify(store,times(1)).claim();verify(store,never()).result(any(),any(),any());verify(protocols,times(1)).send(any(),anyMap(),any(),any(),any());}
 @Test void multipleProvidersNeverDoubleSend(){var config=mock(IntegrationConfiguration.class);var store=mock(SmsOutboxStore.class);var protocols=mock(SmsProtocols.class);when(config.runtime(any(),eq("sms"))).thenReturn(Map.of("key","value"));assertThrows(IllegalStateException.class,()->new SmsOutboxDispatcher(config,store,protocols).run());verifyNoInteractions(store,protocols);}
 @Test void claimUsesCommittedBlockedStateAndSkipLocked(){var db=mock(JdbcTemplate.class);when(db.queryForList(anyString())).thenReturn(List.of(Map.of("id",7L,"target","+8613800138000","code","123456")));when(db.update(anyString(),any(Object[].class))).thenReturn(1);var row=new SmsOutboxStore(db).claim();assertEquals(7L,row.get("id"));verify(db).queryForList(contains("FOR UPDATE SKIP LOCKED"));verify(db).update(contains("SET status='blocked'"),eq(7L));}
 @Test void completedQueueEntryErasesCodeAndRequiresOriginalClaim(){var db=mock(JdbcTemplate.class);when(db.update(anyString(),any(Object[].class))).thenReturn(1);new SmsOutboxStore(db).result(7L,"sent","PROVIDER_ACCEPTED:serial");verify(db).update(argThat(s->s.contains("THEN '' ELSE code END")&&s.contains("error='SMS_DISPATCH_UNCONFIRMED'")),eq("sent"),eq("PROVIDER_ACCEPTED:serial"),eq("sent"),eq(7L));}
 @Test void invalidNumbersNeverReachTheNetwork(){var http=mock(IntegrationHttp.class);var s=new SmsProtocols(http,json);assertThrows(IllegalArgumentException.class,()->s.send("tencent",Map.of(),"13800138000","123456","vc-1"));verifyNoInteractions(http);}
}
