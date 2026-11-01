package com.kuaixietech.integration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
class SmsProtocolSignatureTest {
 @Test void tencentMatchesIndependentTc3Fixture()throws Exception{var http=mock(IntegrationHttp.class);when(http.endpoint(anyString(),eq(true))).thenAnswer(c->URI.create(c.getArgument(0)));var p=new SmsProtocols(http,new ObjectMapper());var request=p.tencent(Map.of("secret_id","fake-id","secret_key","fake-secret","sdk_app_id","1400000000","sign_name","Kuaixie","template_id","1234","region","ap-guangzhou"),"+8613800138000","123456","vc-7",Instant.parse("2025-01-01T00:00:00Z"));assertEquals("TC3-HMAC-SHA256 Credential=fake-id/2025-01-01/sms/tc3_request, SignedHeaders=content-type;host, Signature=27c7e27aea6c68c49da90c6e174381af46b5de36f5f43d136672dd9744336d92",request.headers().firstValue("Authorization").orElseThrow());assertEquals("2021-01-11",request.headers().firstValue("X-TC-Version").orElseThrow());assertEquals("sms.tencentcloudapi.com",request.uri().getHost());}
 @Test void aliyunMatchesIndependentRpcFixture()throws Exception{var http=mock(IntegrationHttp.class);when(http.endpoint(anyString(),eq(true))).thenAnswer(c->URI.create(c.getArgument(0)));var p=new SmsProtocols(http,new ObjectMapper());var request=p.aliyun(Map.of("access_key_id","fake-id","access_key_secret","fake-secret","sign_name","Kuaixie","template_code","SMS_123","region","cn-hangzhou"),"+8613800138000","123456","vc-7",Instant.parse("2025-01-01T00:00:00Z"),"nonce");var bytes=new java.io.ByteArrayOutputStream();var complete=new java.util.concurrent.CompletableFuture<Void>();request.bodyPublisher().orElseThrow().subscribe(new java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer>(){public void onSubscribe(java.util.concurrent.Flow.Subscription subscription){subscription.request(Long.MAX_VALUE);}public void onNext(java.nio.ByteBuffer value){byte[] chunk=new byte[value.remaining()];value.get(chunk);bytes.writeBytes(chunk);}public void onError(Throwable error){complete.completeExceptionally(error);}public void onComplete(){complete.complete(null);}});complete.get(2,java.util.concurrent.TimeUnit.SECONDS);String body=bytes.toString(java.nio.charset.StandardCharsets.UTF_8);assertTrue(body.contains("Signature=1hB0695fn2NlGQD88qd1Z7i6x4o%3D"));assertNull(request.uri().getQuery());assertEquals("dysmsapi.aliyuncs.com",request.uri().getHost());}
 @Test void rpcEncodingUsesRfc3986(){assertEquals("a%20b%2B%2A~",SmsProtocols.encode("a b+*~"));}
}
