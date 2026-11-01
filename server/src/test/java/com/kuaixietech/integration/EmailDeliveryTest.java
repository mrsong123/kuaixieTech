package com.kuaixietech.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.scheduler.ContactCodeDeliveryScheduler;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class EmailDeliveryTest {
    private final ObjectMapper json = new ObjectMapper();
    private Map<String, String> fields() {
        return Map.of("access_key_id", "fake-id", "access_key_secret", "fake-secret", "region", "cn-hangzhou",
            "account_name", "no-reply@example.com", "from_alias", "Kuaixie", "subject", "Your login code",
            "text_template", "Your code is {code}. It expires in {minutes} minutes.");
    }
    private EmailProtocols protocols() {
        var http = mock(IntegrationHttp.class);
        when(http.endpoint(anyString(), eq(true))).thenAnswer(c -> URI.create(c.getArgument(0)));
        return new EmailProtocols(http, json);
    }
    private String body(HttpRequest request) throws Exception {
        var bytes = new ByteArrayOutputStream();
        var done = new CompletableFuture<Void>();
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<ByteBuffer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
            public void onNext(ByteBuffer b) { byte[] chunk = new byte[b.remaining()]; b.get(chunk); bytes.writeBytes(chunk); }
            public void onError(Throwable error) { done.completeExceptionally(error); }
            public void onComplete() { done.complete(null); }
        });
        done.get(2, TimeUnit.SECONDS);
        return bytes.toString(StandardCharsets.UTF_8);
    }
    @Test void matchesIndependentPublicRpcSigningFixture() throws Exception {
        var request = protocols().request(fields(), "buyer@example.net", "123456", Instant.parse("2025-01-01T00:00:00Z"), "nonce");
        assertEquals("dm.aliyuncs.com", request.uri().getHost());
        assertEquals("POST", request.method());
        assertNull(request.uri().getQuery(), "Code and recipient must not enter a URL query");
        String body = body(request);
        assertTrue(body.contains("Version=2015-11-23"));
        assertTrue(body.contains("Action=SingleSendMail"));
        assertTrue(body.contains("AddressType=1"));
        assertTrue(body.contains("ReplyToAddress=false"));
        assertTrue(body.contains("TextBody=Your%20code%20is%20123456.%20It%20expires%20in%2010%20minutes."));
        assertTrue(body.contains("Signature=cd%2FLO%2FvYg04tJPmEe%2F%2BwrQyisaM%3D"));
    }
    @Test void templateValidityReflectsRemainingQueueLifetimeRatherThanRestartingTenMinutes() throws Exception {
        var request = protocols().request(fields(), "buyer@example.net", "123456", 2, Instant.parse("2025-01-01T00:00:00Z"), "nonce");
        assertTrue(body(request).contains("expires%20in%202%20minutes."));
    }
    @Test void regionalEndpointsAreFixedRatherThanAnArbitraryProxy() {
        var fields = new java.util.HashMap<>(fields());
        fields.put("region", "eu-central-1");
        assertEquals("dm.eu-central-1.aliyuncs.com", protocols().request(fields, "buyer@example.net", "123456", Instant.now(), "n").uri().getHost());
        fields.put("region", "https://attacker.example");
        assertThrows(IllegalArgumentException.class, () -> protocols().validateConfiguration(fields));
    }
    @Test void invalidTemplateIsRejectedBeforeClaimingAnyCode() {
        var config = mock(IntegrationConfiguration.class);
        var store = mock(EmailOutboxStore.class);
        var fields = new java.util.HashMap<>(fields());
        fields.put("text_template", "Code {unimplemented_token}");
        when(config.runtime("aliyun", "directmail")).thenReturn(fields);
        var dispatcher = new EmailOutboxDispatcher(config, store, protocols());
        assertThrows(IllegalArgumentException.class, dispatcher::run);
        verifyNoInteractions(store);
    }
    @Test void headerInjectionAndMultipleRecipientsAreRejectedBeforeNetwork() {
        var http = mock(IntegrationHttp.class);
        var protocol = new EmailProtocols(http, json);
        assertThrows(IllegalArgumentException.class, () -> protocol.send(fields(), "one@example.com,two@example.com", "123456", 10));
        assertThrows(IllegalArgumentException.class, () -> protocol.send(fields(), "victim@example.com\r\nBcc:x@example.com", "123456", 10));
        var fields = new java.util.HashMap<>(fields());
        fields.put("subject", "Code\r\nBcc:attacker@example.com");
        assertThrows(IllegalArgumentException.class, () -> protocol.send(fields, "buyer@example.net", "123456", 10));
        verifyNoInteractions(http);
    }
    @Test void missingOrErrorReceiptsNeverInventAcceptance() throws Exception {
        var p = protocols();
        assertThrows(IllegalStateException.class, () -> p.parse(json.readTree("{}")));
        assertThrows(IllegalStateException.class, () -> p.parse(json.readTree("{\"RequestId\":123}")));
        assertThrows(IllegalStateException.class, () -> p.parse(json.readTree("{\"RequestId\":\"request\",\"Error\":{\"Message\":\"bad\"}}")));
        assertThrows(IllegalStateException.class, () -> p.parse(json.readTree("{\"RequestId\":\"request\",\"EnvId\":123}")));
        assertFalse(p.parse(json.readTree("{\"RequestId\":\"request\",\"Code\":\"InvalidAccountName\"}")).accepted());
    }
    @Test void officialRequestOnlySuccessMeansAcceptanceNotDelivery() throws Exception {
        var receipt = protocols().parse(json.readTree("{\"RequestId\":\"request\"}"));
        assertTrue(receipt.accepted());
        assertEquals("request", receipt.providerReference());
        assertEquals("envelope", protocols().parse(json.readTree("{\"RequestId\":\"request\",\"EnvId\":\"envelope\"}")).providerReference());
    }
    @Test void malformedUpstreamDoesNotLeakRecipientCodeOrSecret() {
        var http = mock(IntegrationHttp.class);
        when(http.endpoint(anyString(), eq(true))).thenAnswer(c -> URI.create(c.getArgument(0)));
        when(http.send(any())).thenThrow(new IllegalStateException("buyer@example.net 123456 fake-secret"));
        var error = assertThrows(IllegalStateException.class, () -> new EmailProtocols(http, json).send(fields(), "buyer@example.net", "123456", 10));
        assertEquals("EMAIL_DISPATCH_UNCONFIRMED", error.getMessage());
        assertNull(error.getCause());
    }
    @Test void ambiguousDispatchKeepsBlockedAttemptAndNeverResendsAutomatically() {
        var config = mock(IntegrationConfiguration.class);
        var store = mock(EmailOutboxStore.class);
        var protocols = mock(EmailProtocols.class);
        when(config.runtime("aliyun", "directmail")).thenReturn(fields());
        when(store.claim()).thenReturn(Map.of("id", 9L, "target", "buyer@example.net", "code", "123456", "remaining_minutes", 2));
        when(protocols.send(anyMap(), any(), any(), anyInt())).thenThrow(new IllegalStateException("timeout"));
        assertEquals("EMAIL_DISPATCH_UNCONFIRMED",assertThrows(IllegalStateException.class,()->new EmailOutboxDispatcher(config, store, protocols).run()).getMessage());
        verify(store, times(1)).claim();
        verify(store, never()).result(any(), any(), any());
        verify(store, never()).invalid(any());
        verify(protocols, times(1)).send(anyMap(), any(), any(), eq(2));
    }
    @Test void providerAcceptedDispatchClearsCodeThroughOriginalClaimOnly() {
        var config = mock(IntegrationConfiguration.class);
        var store = mock(EmailOutboxStore.class);
        var protocols = mock(EmailProtocols.class);
        when(config.runtime("aliyun", "directmail")).thenReturn(fields());
        when(store.claim()).thenReturn(Map.of("id", 9L, "target", "buyer@example.net", "code", "123456", "remaining_minutes", 2)).thenReturn(null);
        when(protocols.send(anyMap(), any(), any(), anyInt())).thenReturn(new EmailProtocols.Receipt(true, "request-id"));
        new EmailOutboxDispatcher(config, store, protocols).run();
        verify(store).result(9L, "sent", "request-id");
    }
    @Test void claimIsEmailScopedAndCommitsBlockedBeforeProviderCall() {
        var db = mock(JdbcTemplate.class);
        when(db.queryForList(anyString())).thenReturn(java.util.List.of(Map.of("id", 9L, "target", "buyer@example.net", "code", "123456", "remaining_minutes", 2)));
        when(db.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals(9L, new EmailOutboxStore(db).claim().get("id"));
        verify(db).queryForList(argThat(sql -> sql.contains("channel='email'") && sql.contains("FOR UPDATE SKIP LOCKED")));
        verify(db).update(argThat(sql -> sql.contains("status='blocked'") && sql.contains("EMAIL_DISPATCH_UNCONFIRMED")), eq(9L));
    }
    @Test void receiptUpdateCannotOverwriteOtherChannelOrAttempt() {
        var db = mock(JdbcTemplate.class);
        when(db.update(anyString(), any(Object[].class))).thenReturn(1);
        new EmailOutboxStore(db).result(9L, "sent", "request-id");
        verify(db).update(argThat(sql -> sql.contains("channel='email'") && sql.contains("error='EMAIL_DISPATCH_UNCONFIRMED'") && sql.contains("code=''")), eq("sent"), eq("PROVIDER_ACCEPTED:request-id"), eq(9L));
    }
    @Test void nativeSmsCannotStarveNativeEmailAndLegacyCannotDuplicateEither() {
        var sms = mock(SmsOutboxDispatcher.class);
        var email = mock(EmailOutboxDispatcher.class);
        var legacy = mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenReturn(true);
        when(email.configured()).thenReturn(true);
        new ContactCodeDeliveryScheduler(legacy, sms, email, true).run();
        verify(sms).run();
        verify(email).run();
        verifyNoInteractions(legacy);
    }
    @Test void failedSmsStillAllowsEmailButPreventsLegacyFallback() {
        var sms = mock(SmsOutboxDispatcher.class);
        var email = mock(EmailOutboxDispatcher.class);
        var legacy = mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenThrow(new IllegalStateException("unreadable secret"));
        when(email.configured()).thenReturn(true);
        var scheduler=new ContactCodeDeliveryScheduler(legacy,sms,email,true);
        assertDoesNotThrow(scheduler::run);
        assertEquals("degraded",scheduler.snapshot().state());
        assertEquals("configuration",scheduler.snapshot().sms().stage());
        verify(email).run();
        verifyNoInteractions(legacy);
    }
    @Test void disabledNativeConfigurationMustNotReenableLegacyQueueBridge() {
        var sms = mock(SmsOutboxDispatcher.class);
        var email = mock(EmailOutboxDispatcher.class);
        var legacy = mock(LegacyCloudFunctionBridge.class);
        when(email.nativeManaged()).thenReturn(true);
        new ContactCodeDeliveryScheduler(legacy, sms, email, true).run();
        verify(sms, never()).run();
        verify(email, never()).run();
        verifyNoInteractions(legacy);
    }
    @Test void failureDuringSmsDeliveryStillAllowsEmail() {
        var sms = mock(SmsOutboxDispatcher.class);
        var email = mock(EmailOutboxDispatcher.class);
        var legacy = mock(LegacyCloudFunctionBridge.class);
        when(sms.configured()).thenReturn(true);
        when(email.configured()).thenReturn(true);
        doThrow(new IllegalStateException("provider unavailable")).when(sms).run();
        var scheduler=new ContactCodeDeliveryScheduler(legacy,sms,email,true);
        assertDoesNotThrow(scheduler::run);
        assertEquals("degraded",scheduler.snapshot().state());
        assertEquals("dispatch",scheduler.snapshot().sms().stage());
        verify(email).run();
        verifyNoInteractions(legacy);
    }
}
