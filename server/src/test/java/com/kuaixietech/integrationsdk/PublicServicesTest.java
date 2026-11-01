package com.kuaixietech.integrationsdk;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuaixietech.integration.IntegrationHttp;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class PublicServicesTest {
    final ObjectMapper json = new ObjectMapper();
    final IntegrationHttp http = mock(IntegrationHttp.class);
    PublicServiceAdapters adapter() {
        when(http.endpoint(anyString(), anyBoolean())).thenAnswer(c -> URI.create(c.getArgument(0)));
        return new PublicServiceAdapters(http, json);
    }
    Map<String, String> fields(String... pairs) {
        var values = new HashMap<String, String>(); values.put("_runtime_mode", "LIVE");
        for (int i = 0; i < pairs.length; i += 2) values.put(pairs[i], pairs[i + 1]); return values;
    }
    JsonNode in(String source) throws Exception { return json.readTree(source); }
    String body(HttpRequest request) throws Exception {
        var bytes = new ByteArrayOutputStream(); var done = new CompletableFuture<Void>();
        request.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<ByteBuffer>() {
            public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
            public void onNext(ByteBuffer b) { byte[] chunk = new byte[b.remaining()]; b.get(chunk); bytes.writeBytes(chunk); }
            public void onError(Throwable t) { done.completeExceptionally(t); }
            public void onComplete() { done.complete(null); }
        });
        done.get(2, TimeUnit.SECONDS); return bytes.toString(StandardCharsets.UTF_8);
    }
    @Test void sandboxAndMissingModeNeverFallBackToLive() throws Exception {
        var a = adapter(); var f = fields("api_key", "live-secret", "model", "configured-model");
        f.put("_runtime_mode", "SANDBOX");
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "chatgpt", "chat", f, in("{}")));
        f.remove("_runtime_mode");
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "chatgpt", "chat", f, in("{}")));
        verify(http, never()).send(any());
    }
    @Test void modeIsCheckedOnCopiedSnapshotRatherThanMutableOriginalMap() throws Exception {
        var a = adapter(); var f = new HashMap<String, String>() { @Override public String get(Object key) { return key.equals("_runtime_mode") ? "LIVE" : super.get(key); } };
        f.put("_runtime_mode", "SANDBOX"); f.put("api_key", "live-secret"); f.put("model", "model");
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "chatgpt", "chat", f, in("{}")));
        verify(http, never()).send(any());
    }
    @Test void localReadinessValidationDoesNotSpendOrProbe() {
        var a = adapter(); a.validate("AI", "chatgpt", fields("api_key", "secret", "model", "m"));
        a.validate("TRANSLATE", "deepl", fields("api_key", "secret", "plan", "free"));
        a.validate("MAP", "amap", fields("rest_key", "key")); verify(http, never()).send(any());
    }
    @Test void cursorIsNotAdvertisedAsOpenAiChat() {
        var a = adapter(); assertFalse(a.supports("AI", "cursor", "chat")); assertTrue(a.supports("AI", "cursor", "agent_create"));
        assertEquals(java.util.Set.of("LIVE"), a.supportedModes());
    }
    @Test void openAiUsesConfiguredModelAndBoundedOutputWithoutLeakingKey() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest request = c.getArgument(0); JsonNode body = in(body(request));
            assertEquals("api.openai.com", request.uri().getHost()); assertEquals("Bearer secret", request.headers().firstValue("Authorization").orElseThrow());
            assertEquals("chosen-model", body.path("model").asText()); assertFalse(body.path("stream").asBoolean()); assertEquals(25, body.path("max_tokens").asInt());
            return "{\"id\":\"reply\",\"model\":\"resolved-model\",\"choices\":[{\"message\":{\"content\":\"Hello\"}}],\"usage\":{\"total_tokens\":6}}";
        });
        var result = a.execute("AI", "chatgpt", "chat", fields("api_key", "secret", "model", "chosen-model"), in("{\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}],\"maxTokens\":25,\"model\":\"untrusted-expensive-model\"}"));
        assertEquals("Hello", result.path("text").asText()); assertEquals(6, result.path("totalTokens").asInt()); assertFalse(result.toString().contains("secret"));
    }
    @Test void malformedOrToolMessagesCannotTriggerPaidRequests() throws Exception {
        var a = adapter(); var f = fields("api_key", "secret", "model", "m");
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "chatgpt", "chat", f, in("{\"messages\":[{\"role\":\"tool\",\"content\":\"x\"}]}")));
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "chatgpt", "chat", f, in("{\"messages\":[{\"role\":\"user\",\"content\":\"x\"}],\"maxTokens\":99999}")));
        verify(http, never()).send(any());
    }
    @Test void deepseekHasItsOwnFixedEndpointAndCompatiblePayload() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest r = c.getArgument(0); assertEquals("https://api.deepseek.com/chat/completions", r.uri().toString());
            assertFalse(in(body(r)).has("n")); return "{\"choices\":[{\"message\":{\"content\":\"Answer\"}}]}";
        });
        assertEquals("Answer", a.execute("AI", "deepseek", "chat", fields("api_key", "secret", "model", "deepseek-chat"), in("{\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}" )).path("text").asText());
    }
    @Test void customAiBaseMustPassDeploymentHostPolicy() throws Exception {
        var a = adapter(); when(http.endpoint(contains("attacker.example"), eq(false))).thenThrow(new IllegalArgumentException("denied"));
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "chatgpt", "chat", fields("api_key", "secret", "model", "m", "base_url", "https://attacker.example/v1"), in("{\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}")));
        verify(http, never()).send(any());
    }
    @Test void malformedAiResponseNeverInventsSuccess() throws Exception {
        var a = adapter(); when(http.send(any())).thenReturn("{\"choices\":[]}");
        assertThrows(IllegalStateException.class, () -> a.execute("AI", "chatgpt", "chat", fields("api_key", "secret", "model", "m"), in("{\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}")));
    }
    @Test void cursorCreateUsesBasicAuthAndConfiguredRepositoryWithNoImplicitPr() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest r = c.getArgument(0); assertEquals("https://api.cursor.com/v0/agents", r.uri().toString());
            assertEquals("Basic c2VjcmV0Og==", r.headers().firstValue("Authorization").orElseThrow()); var b = in(body(r));
            assertEquals("https://github.com/example/repo", b.path("source").path("repository").asText()); assertFalse(b.path("target").path("autoCreatePr").asBoolean());
            return "{\"id\":\"agent-1\",\"status\":\"CREATING\"}";
        });
        var result = a.execute("AI", "cursor", "agent_create", fields("api_key", "secret", "repository", "https://github.com/example/repo"), in("{\"prompt\":\"Fix issue\"}")); assertEquals("agent-1", result.path("agentId").asText());
    }
    @Test void cursorCannotLaunchAnotherRepositoryOrClaimDifferentAgentReceipt() throws Exception {
        var a = adapter(); var f = fields("api_key", "secret", "repository", "https://github.com/example/repo");
        assertThrows(IllegalArgumentException.class, () -> a.execute("AI", "cursor", "agent_create", f, in("{\"prompt\":\"x\",\"repository\":\"https://github.com/other/private\"}")));
        verify(http, never()).send(any()); when(http.send(any())).thenReturn("{\"id\":\"another-agent\",\"status\":\"RUNNING\"}");
        assertThrows(IllegalStateException.class, () -> a.execute("AI", "cursor", "agent_status", f, in("{\"agentId\":\"agent-1\"}")));
    }
    @Test void tencentTranslationUsesTc3AndProjectIdentity() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest r = c.getArgument(0); assertEquals("TextTranslate", r.headers().firstValue("X-TC-Action").orElseThrow());
            assertTrue(r.headers().firstValue("Authorization").orElseThrow().contains("/tmt/tc3_request")); assertEquals(0, in(body(r)).path("ProjectId").asInt());
            return "{\"Response\":{\"TargetText\":\"你好\",\"RequestId\":\"r\"}}";
        });
        assertEquals("你好", a.execute("TRANSLATE", "tencent", "translate", fields("secret_id", "id", "secret_key", "key", "region", "ap-guangzhou", "project_id", "0"), in("{\"text\":\"Hello\",\"target\":\"zh\"}")).path("text").asText());
    }
    @Test void aliyunTranslationUsesRpcGeneralAndRealResponseCode() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest r = c.getArgument(0); assertEquals("mt.cn-hangzhou.aliyuncs.com", r.uri().getHost()); assertTrue(body(r).contains("Action=TranslateGeneral")); assertNull(r.uri().getQuery());
            return "{\"Code\":200,\"Data\":{\"Translated\":\"你好\"}}";
        });
        assertEquals("你好", a.execute("TRANSLATE", "aliyun", "translate", fields("access_key_id", "id", "access_key_secret", "key", "region", "cn-hangzhou"), in("{\"text\":\"Hello\",\"target\":\"zh\"}")).path("text").asText());
    }
    @Test void deeplFreeIsStillLiveAndUsesDeepLKeyHeader() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest r = c.getArgument(0); assertEquals("api-free.deepl.com", r.uri().getHost()); assertEquals("DeepL-Auth-Key secret", r.headers().firstValue("Authorization").orElseThrow());
            assertEquals("DE", in(body(r)).path("target_lang").asText()); return "{\"translations\":[{\"text\":\"Hallo\"}]}";
        });
        assertEquals("Hallo", a.execute("TRANSLATE", "deepl", "translate", fields("api_key", "secret", "plan", "free"), in("{\"text\":\"Hello\",\"target\":\"de\"}")).path("text").asText());
    }
    @Test void googleUsesKeyHeaderAndReturnsPlainUnescapedText() throws Exception {
        var a = adapter(); when(http.send(any())).thenAnswer(c -> {
            HttpRequest r = c.getArgument(0); assertEquals("secret", r.headers().firstValue("X-Goog-Api-Key").orElseThrow()); assertNull(r.uri().getQuery());
            return "{\"data\":{\"translations\":[{\"translatedText\":\"Tom &amp; Jerry&#39;s\"}]}}";
        });
        assertEquals("Tom & Jerry's", a.execute("TRANSLATE", "google", "translate", fields("api_key", "secret"), in("{\"text\":\"Hello\",\"target\":\"de\"}")).path("text").asText());
    }
    @Test void amapGeocodePreservesCoordinateSystemAndEmptySearchIsNotInvented() throws Exception {
        var a = adapter(); when(http.send(any())).thenReturn("{\"status\":\"1\",\"infocode\":\"10000\",\"geocodes\":[{\"formatted_address\":\"Address\",\"location\":\"116.39,39.90\"}]}");
        var result = a.execute("MAP", "amap", "geocode", fields("rest_key", "secret"), in("{\"address\":\"Address\"}"));
        assertEquals("GCJ-02", result.path("coordinateSystem").asText()); assertEquals("116.39", result.path("results").get(0).path("longitude").asText());
        when(http.send(any())).thenReturn("{\"status\":\"1\",\"infocode\":\"10000\",\"geocodes\":[]}");
        assertFalse(a.execute("MAP", "amap", "geocode", fields("rest_key", "secret"), in("{\"address\":\"Unknown\"}")).path("found").asBoolean());
    }
    @Test void invalidTencentCoordinatesOrProviderErrorsNeverBecomeLocations() throws Exception {
        var a = adapter(); when(http.send(any())).thenReturn("{\"status\":0,\"result\":{\"location\":{\"lng\":999,\"lat\":40}}}");
        assertThrows(IllegalStateException.class, () -> a.execute("MAP", "tencent", "geocode", fields("api_key", "secret"), in("{\"address\":\"Address\"}")));
        when(http.send(any())).thenReturn("{\"status\":403,\"message\":\"bad key\"}");
        assertThrows(IllegalStateException.class, () -> a.execute("MAP", "tencent", "geocode", fields("api_key", "secret"), in("{\"address\":\"Address\"}")));
    }
    @Test void upstreamSecretsAndSourceTextNeverEscapeExceptionMessages() throws Exception {
        var a = adapter(); when(http.send(any())).thenThrow(new IllegalStateException("secret user-private-address"));
        var error = assertThrows(IllegalStateException.class, () -> a.execute("MAP", "amap", "geocode", fields("rest_key", "secret"), in("{\"address\":\"user-private-address\"}")));
        assertEquals("PUBLIC_API_RESULT_UNCONFIRMED", error.getMessage()); assertNull(error.getCause());
    }
    @Test void tc3SignatureMatchesIndependentFixture() {
        assertEquals("TC3-HMAC-SHA256 Credential=fake-id/2025-01-01/tmt/tc3_request, SignedHeaders=content-type;host, Signature=d3027f91c56ce3562c08ed031249ee25846c5b08284b4b8f998d7d36a4e6cc62", PublicRequestSigning.tc3("tmt", "tmt.tencentcloudapi.com", "{\"ProjectId\":0,\"Source\":\"auto\",\"SourceText\":\"Hello\",\"Target\":\"zh\"}", fields("secret_id", "fake-id", "secret_key", "fake-secret"), Instant.parse("2025-01-01T00:00:00Z")));
    }
    @Test void rpcSignatureMatchesIndependentFixture() {
        var values = PublicRequestSigning.rpc("TranslateGeneral", "2018-10-12", fields("access_key_id", "fake-id", "access_key_secret", "fake-secret", "region", "cn-hangzhou"), Map.of("FormatType", "text", "SourceLanguage", "auto", "TargetLanguage", "zh", "SourceText", "Hello", "Scene", "general"), Instant.parse("2025-01-01T00:00:00Z"), "nonce");
        assertEquals("xN/SLuqL8WmCZ3kMhEkpQi11gIk=", values.get("Signature"));
    }
}
