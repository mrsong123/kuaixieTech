package com.kuaixietech.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/** Alibaba DirectMail public RPC protocol. Provider acceptance is not mailbox delivery. */
@Component
public class EmailProtocols {
    private static final Map<String, String> ENDPOINTS = Map.of(
        "cn-hangzhou", "dm.aliyuncs.com",
        "ap-southeast-1", "dm.ap-southeast-1.aliyuncs.com",
        "ap-southeast-2", "dm.ap-southeast-2.aliyuncs.com",
        "us-east-1", "dm.us-east-1.aliyuncs.com",
        "eu-central-1", "dm.eu-central-1.aliyuncs.com");
    private static final String MAILBOX = "[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+";
    private final IntegrationHttp http;
    private final ObjectMapper json;

    public EmailProtocols(IntegrationHttp http, ObjectMapper json) {
        this.http = http;
        this.json = json;
    }

    public record Receipt(boolean accepted, String providerReference) { }

    /** Local checks run before queue claim; invalid operator configuration must not consume a code. */
    public void validateConfiguration(Map<String, String> fields) {
        if (fields == null || fields.get("region") == null || !ENDPOINTS.containsKey(fields.get("region"))
            || !mailbox(fields.get("account_name"))) {
            throw new IllegalArgumentException("EMAIL_CONFIGURATION_REJECTED");
        }
        for (String name : new String[]{"access_key_id", "access_key_secret", "from_alias", "subject", "text_template"}) {
            if (fields.get(name) == null || fields.get(name).isBlank()) {
                throw new IllegalArgumentException("EMAIL_CONFIGURATION_REJECTED");
            }
        }
        if (!header(fields.get("from_alias"), 100) || !header(fields.get("subject"), 120)) {
            throw new IllegalArgumentException("EMAIL_CONFIGURATION_REJECTED");
        }
        String text = fields.get("text_template");
        String literals = text.replace("{code}", "").replace("{minutes}", "");
        if (text.length() > 4000 || !text.contains("{code}")
            || literals.contains("{") || literals.contains("}") || text.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("EMAIL_TEMPLATE_REJECTED");
        }
    }

    public Receipt send(Map<String, String> fields, String target, String code, int remainingMinutes) {
        validateConfiguration(fields);
        if (!mailbox(target) || code == null || !code.matches("[0-9]{6}") || remainingMinutes < 1 || remainingMinutes > 10) {
            throw new IllegalArgumentException("EMAIL_INPUT_REJECTED");
        }
        HttpRequest request = request(fields, target, code, remainingMinutes, Instant.now(), UUID.randomUUID().toString());
        try {
            return parse(json.readTree(http.send(request)));
        } catch (Exception e) {
            // No provider response body, recipient, code or configured secret escapes through errors.
            throw new IllegalStateException("EMAIL_DISPATCH_UNCONFIRMED");
        }
    }

    HttpRequest request(Map<String, String> fields, String target, String code, Instant now, String nonce) {
        return request(fields, target, code, 10, now, nonce);
    }

    HttpRequest request(Map<String, String> fields, String target, String code, int remainingMinutes, Instant now, String nonce) {
        validateConfiguration(fields);
        if (!mailbox(target) || code == null || !code.matches("[0-9]{6}") || remainingMinutes < 1 || remainingMinutes > 10) {
            throw new IllegalArgumentException("EMAIL_INPUT_REJECTED");
        }
        var params = new TreeMap<String, String>();
        params.put("AccessKeyId", fields.get("access_key_id"));
        params.put("Action", "SingleSendMail");
        params.put("Format", "JSON");
        params.put("Version", "2015-11-23");
        params.put("RegionId", fields.get("region"));
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", nonce);
        params.put("Timestamp", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC).format(now));
        params.put("AccountName", fields.get("account_name"));
        params.put("AddressType", "1");
        params.put("ReplyToAddress", "false");
        params.put("FromAlias", fields.get("from_alias"));
        params.put("ToAddress", target);
        params.put("Subject", fields.get("subject"));
        params.put("TextBody", fields.get("text_template").replace("{code}", code).replace("{minutes}", Integer.toString(remainingMinutes)));
        String canonical = SmsProtocols.form(params);
        try {
            var mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((fields.get("access_key_secret") + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            params.put("Signature", Base64.getEncoder().encodeToString(mac.doFinal(
                ("POST&%2F&" + SmsProtocols.encode(canonical)).getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new IllegalStateException("EMAIL_SIGNING_UNAVAILABLE");
        }
        URI endpoint = http.endpoint("https://" + ENDPOINTS.get(fields.get("region")) + "/", true);
        return HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(SmsProtocols.form(params))).build();
    }

    Receipt parse(JsonNode response) {
        if (response == null || !response.isObject() || !response.path("RequestId").isTextual() || !safeReference(response.path("RequestId").asText())) {
            throw new IllegalStateException("EMAIL_RESPONSE_UNCONFIRMED");
        }
        if (response.has("Error") || response.has("error")) throw new IllegalStateException("EMAIL_RESPONSE_UNCONFIRMED");
        if (response.has("Code")) {
            if (!response.path("Code").isTextual() || response.path("Code").asText().isBlank()) {
                throw new IllegalStateException("EMAIL_RESPONSE_UNCONFIRMED");
            }
            // SingleSendMail's success shape has RequestId/optional EnvId, not an invented OK field.
            return new Receipt(false, response.path("RequestId").asText());
        }
        if (response.hasNonNull("EnvId") && !response.path("EnvId").isTextual()) throw new IllegalStateException("EMAIL_RESPONSE_UNCONFIRMED");
        String reference = response.hasNonNull("EnvId") ? response.path("EnvId").asText()
            : response.path("RequestId").asText();
        if (!safeReference(reference)) {
            throw new IllegalStateException("EMAIL_RESPONSE_UNCONFIRMED");
        }
        return new Receipt(true, reference);
    }

    static boolean safeReference(String value) {
        return value != null && value.matches("[A-Za-z0-9_.:-]{1,160}");
    }

    private static boolean mailbox(String value) {
        return value != null && value.length() <= 128 && value.matches(MAILBOX);
    }

    private static boolean header(String value, int limit) {
        return value != null && !value.isBlank() && value.length() <= limit
            && value.chars().noneMatch(c -> c == '\r' || c == '\n' || c == 0);
    }
}
