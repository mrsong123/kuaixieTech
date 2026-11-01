package com.kuaixietech.integrationsdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kuaixietech.integration.IntegrationHttp;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/**
 * Public REST integrations for a trusted, authorized business caller. This class exposes no controller/proxy.
 * Config is one immutable environment snapshot; no environment lookup or fallback is performed here.
 */
@Service
public class PublicServiceAdapters {
    private static final Set<String> BINDINGS = Set.of("AI/chatgpt", "AI/deepseek", "AI/cursor",
        "TRANSLATE/tencent", "TRANSLATE/aliyun", "TRANSLATE/deepl", "TRANSLATE/google", "MAP/amap", "MAP/tencent");
    private static final Map<String, String> ALIMT_ENDPOINTS = Map.of("cn-hangzhou", "mt.cn-hangzhou.aliyuncs.com",
        "ap-southeast-1", "mt.ap-southeast-1.aliyuncs.com");
    private final IntegrationHttp http;
    private final ObjectMapper json;

    public PublicServiceAdapters(IntegrationHttp http, ObjectMapper json) { this.http = http; this.json = json; }
    public Set<String> supportedModes() { return Set.of("LIVE"); }
    public boolean supports(String category, String provider, String operation) {
        if (category == null || provider == null || operation == null) return false;
        if (!BINDINGS.contains(category + "/" + provider)) return false;
        return switch (category) {
            case "AI" -> provider.equals("cursor") ? Set.of("agent_create", "agent_status", "agent_stop").contains(operation) : operation.equals("chat");
            case "TRANSLATE" -> operation.equals("translate");
            case "MAP" -> operation.equals("geocode");
            default -> false;
        };
    }

    /** Local readiness validation only: it never probes a paid API, in either mode. */
    public void validate(String category, String provider, Map<String, String> fields) {
        if (!BINDINGS.contains(category + "/" + provider) || fields == null) throw invalid("PUBLIC_API_BINDING_UNSUPPORTED");
        switch (category + "/" + provider) {
            case "AI/chatgpt", "AI/deepseek" -> { required(fields, "api_key"); required(fields, "model"); if (fields.containsKey("base_url") && !fields.get("base_url").isBlank()) customBase(fields.get("base_url")); }
            case "AI/cursor" -> { required(fields, "api_key"); repository(required(fields, "repository")); }
            case "TRANSLATE/tencent" -> { required(fields, "secret_id"); required(fields, "secret_key"); required(fields, "region"); project(fields); }
            case "TRANSLATE/aliyun" -> { required(fields, "access_key_id"); required(fields, "access_key_secret"); if (!ALIMT_ENDPOINTS.containsKey(required(fields, "region"))) throw invalid("PUBLIC_API_REGION_UNSUPPORTED"); }
            case "TRANSLATE/deepl" -> { required(fields, "api_key"); if (!Set.of("free", "pro").contains(required(fields, "plan"))) throw invalid("PUBLIC_API_PLAN_UNSUPPORTED"); }
            case "TRANSLATE/google" -> required(fields, "api_key");
            case "MAP/amap" -> required(fields, "rest_key");
            case "MAP/tencent" -> required(fields, "api_key");
            default -> throw invalid("PUBLIC_API_BINDING_UNSUPPORTED");
        }
    }

    public JsonNode execute(String category, String provider, String operation, Map<String, String> configuration, JsonNode input) {
        if (!supports(category, provider, operation)) throw invalid("PUBLIC_API_OPERATION_UNSUPPORTED");
        // All these vendors use real accounts/endpoints. A testing label must not silently spend live funds.
        final Map<String, String> fields;
        try { fields = Map.copyOf(configuration); } catch (RuntimeException e) { throw invalid("PUBLIC_API_CONFIGURATION_REJECTED"); }
        if (!"LIVE".equals(fields.get("_runtime_mode"))) throw invalid("PUBLIC_API_LIVE_MODE_REQUIRED");
        validate(category, provider, fields);
        if (input == null || !input.isObject()) throw invalid("PUBLIC_API_INPUT_REJECTED");
        try {
            return switch (category) {
                case "AI" -> provider.equals("cursor") ? cursor(operation, fields, input) : chat(provider, fields, input);
                case "TRANSLATE" -> translate(provider, fields, input);
                case "MAP" -> geocode(provider, fields, input);
                default -> throw invalid("PUBLIC_API_BINDING_UNSUPPORTED");
            };
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalStateException("PUBLIC_API_RESULT_UNCONFIRMED"); }
    }

    private JsonNode chat(String provider, Map<String, String> fields, JsonNode input) throws Exception {
        JsonNode messages = input.get("messages");
        if (messages == null || !messages.isArray() || messages.isEmpty() || messages.size() > 20) throw invalid("PUBLIC_API_MESSAGES_REJECTED");
        int total = 0;
        for (JsonNode message : messages) {
            if (!message.isObject() || !Set.of("system", "user", "assistant").contains(text(message, "role", 20))) throw invalid("PUBLIC_API_MESSAGE_ROLE_REJECTED");
            total += text(message, "content", 4000).length();
            if (message.size() != 2 || total > 20000) throw invalid("PUBLIC_API_MESSAGES_REJECTED");
        }
        int max = input.has("maxTokens") ? integer(input, "maxTokens", 1, 4096) : 1024;
        var body = json.createObjectNode(); body.put("model", required(fields, "model")); body.set("messages", messages);
        body.put("stream", false); body.put("max_tokens", max); if (provider.equals("chatgpt")) body.put("n", 1);
        String defaultBase = provider.equals("chatgpt") ? "https://api.openai.com/v1" : "https://api.deepseek.com";
        String base = fields.getOrDefault("base_url", defaultBase); if (base.isBlank()) base = defaultBase;
        URI endpoint = base.equals(defaultBase) ? http.endpoint(base + "/chat/completions", true)
            : http.endpoint(customBase(base) + "/chat/completions", false);
        JsonNode response = call(post(endpoint, body).header("Authorization", "Bearer " + required(fields, "api_key")).build());
        rejectError(response);
        JsonNode choices = response.path("choices");
        if (!choices.isArray() || choices.size() != 1 || !choices.get(0).path("message").isObject()) throw unconfirmed();
        JsonNode message = choices.get(0).path("message");
        if (message.hasNonNull("tool_calls") && !message.path("tool_calls").isEmpty() || message.hasNonNull("function_call")) throw unconfirmed();
        ObjectNode result = json.createObjectNode(); result.put("provider", provider);
        if (message.path("content").isTextual()) result.put("text", responseText(message, "content", 100000));
        else if (message.path("refusal").isTextual() && !message.path("refusal").asText().isBlank()) { result.put("refused", true); result.put("refusal", responseText(message, "refusal", 10000)); }
        else throw unconfirmed();
        copyText(response, result, "id", "requestId", 256); copyText(response, result, "model", "model", 256);
        if (response.path("usage").path("total_tokens").canConvertToInt() && response.path("usage").path("total_tokens").asInt() >= 0) result.put("totalTokens", response.path("usage").path("total_tokens").asInt());
        return result;
    }

    private JsonNode cursor(String operation, Map<String, String> fields, JsonNode input) throws Exception {
        String authorization = "Basic " + Base64.getEncoder().encodeToString((required(fields, "api_key") + ":").getBytes(StandardCharsets.UTF_8));
        String path = "https://api.cursor.com/v0/agents";
        HttpRequest request;
        if (operation.equals("agent_create")) {
            String repository = repository(required(fields, "repository"));
            if (input.has("repository") && !repository.equals(text(input, "repository", 500))) throw invalid("CURSOR_REPOSITORY_REJECTED");
            var body = json.createObjectNode(); body.set("prompt", json.createObjectNode().put("text", text(input, "prompt", 10000)));
            var source = json.createObjectNode().put("repository", repository);
            if (input.has("ref")) source.put("ref", text(input, "ref", 200));
            body.set("source", source); body.set("target", json.createObjectNode().put("autoCreatePr", false));
            if (fields.containsKey("model") && !fields.get("model").isBlank()) body.put("model", required(fields, "model"));
            // Do not create PRs or choose user-supplied callbacks/URLs implicitly.
            request = post(http.endpoint(path, true), body).header("Authorization", authorization).build();
        } else {
            String id = text(input, "agentId", 128); if (!id.matches("[A-Za-z0-9_-]+")) throw invalid("CURSOR_AGENT_ID_REJECTED");
            var builder = HttpRequest.newBuilder(http.endpoint(path + "/" + id + (operation.equals("agent_stop") ? "/stop" : ""), true))
                .timeout(Duration.ofSeconds(30)).header("Authorization", authorization).header("Accept", "application/json");
            request = operation.equals("agent_stop") ? builder.POST(HttpRequest.BodyPublishers.noBody()).build() : builder.GET().build();
        }
        JsonNode response = call(request); rejectError(response);
        ObjectNode result = json.createObjectNode().put("provider", "cursor").put("agentId", responseText(response, "id", 128));
        if (!result.path("agentId").asText().matches("[A-Za-z0-9_-]+")) throw unconfirmed();
        if (response.path("status").isTextual()) result.put("status", responseText(response, "status", 100));
        if (!operation.equals("agent_create") && !result.path("agentId").asText().equals(input.path("agentId").asText())) throw unconfirmed();
        return result;
    }

    private JsonNode translate(String provider, Map<String, String> fields, JsonNode input) throws Exception {
        String value = text(input, "text", 4000), source = input.has("source") ? language(input, "source") : "auto", target = language(input, "target");
        if (source.equalsIgnoreCase("auto")) source = "auto"; if (target.equalsIgnoreCase("auto")) throw invalid("PUBLIC_API_LANGUAGE_REJECTED");
        JsonNode response;
        String translated;
        switch (provider) {
            case "tencent" -> {
                if (value.getBytes(StandardCharsets.UTF_8).length > 6000) throw invalid("PUBLIC_API_TEXT_TOO_LARGE");
                var body = new TreeMap<String, Object>(); body.put("SourceText", value); body.put("Source", source); body.put("Target", target); body.put("ProjectId", project(fields));
                String payload = json.writeValueAsString(body); Instant now = Instant.now(); String host = "tmt.tencentcloudapi.com";
                var request = HttpRequest.newBuilder(http.endpoint("https://" + host + "/", true)).timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json; charset=utf-8").header("X-TC-Action", "TextTranslate").header("X-TC-Version", "2018-03-21")
                    .header("X-TC-Region", required(fields, "region")).header("X-TC-Timestamp", Long.toString(now.getEpochSecond()))
                    .header("Authorization", PublicRequestSigning.tc3("tmt", host, payload, fields, now)).POST(HttpRequest.BodyPublishers.ofString(payload)).build();
                response = call(request).path("Response"); rejectError(response); translated = responseText(response, "TargetText", 30000);
            }
            case "aliyun" -> {
                var parameters = Map.of("FormatType", "text", "SourceLanguage", source, "TargetLanguage", target, "SourceText", value, "Scene", "general");
                var signed = PublicRequestSigning.rpc("TranslateGeneral", "2018-10-12", fields, parameters, Instant.now(), UUID.randomUUID().toString());
                response = call(HttpRequest.newBuilder(http.endpoint("https://" + ALIMT_ENDPOINTS.get(required(fields, "region")) + "/", true)).timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(PublicRequestSigning.form(signed))).build());
                if (!response.path("Code").asText().equals("200")) throw unconfirmed(); translated = responseText(response.path("Data"), "Translated", 30000);
            }
            case "deepl" -> {
                var body = json.createObjectNode(); body.set("text", json.createArrayNode().add(value)); body.put("target_lang", target.toUpperCase(java.util.Locale.ROOT));
                if (!source.equals("auto")) body.put("source_lang", source.toUpperCase(java.util.Locale.ROOT));
                String host = required(fields, "plan").equals("free") ? "api-free.deepl.com" : "api.deepl.com";
                response = call(post(http.endpoint("https://" + host + "/v2/translate", true), body).header("Authorization", "DeepL-Auth-Key " + required(fields, "api_key")).build());
                rejectError(response); var translations = response.path("translations"); if (!translations.isArray() || translations.size() != 1) throw unconfirmed(); translated = responseText(translations.get(0), "text", 30000);
            }
            case "google" -> {
                var body = json.createObjectNode(); body.set("q", json.createArrayNode().add(value)); body.put("target", target); body.put("format", "text"); if (!source.equals("auto")) body.put("source", source);
                response = call(post(http.endpoint("https://translation.googleapis.com/language/translate/v2", true), body).header("X-Goog-Api-Key", required(fields, "api_key")).build());
                rejectError(response); var translations = response.path("data").path("translations"); if (!translations.isArray() || translations.size() != 1) throw unconfirmed(); translated = HtmlUtils.htmlUnescape(responseText(translations.get(0), "translatedText", 30000));
            }
            default -> throw invalid("PUBLIC_API_BINDING_UNSUPPORTED");
        }
        return json.createObjectNode().put("provider", provider).put("text", translated).put("source", source).put("target", target);
    }

    private JsonNode geocode(String provider, Map<String, String> fields, JsonNode input) throws Exception {
        String address = text(input, "address", 500); var params = new TreeMap<String, String>(); params.put("address", address);
        String path;
        if (provider.equals("amap")) {
            path = "https://restapi.amap.com/v3/geocode/geo"; params.put("key", required(fields, "rest_key")); params.put("output", "JSON");
            if (input.has("city")) params.put("city", text(input, "city", 100));
            if (fields.containsKey("security_key") && !fields.get("security_key").isBlank()) params.put("sig", PublicRequestSigning.hash("MD5", PublicRequestSigning.raw(params) + required(fields, "security_key")));
        } else {
            path = "https://apis.map.qq.com/ws/geocoder/v1/"; params.put("key", required(fields, "api_key")); params.put("output", "json");
            if (input.has("city")) params.put("region", text(input, "city", 100));
            if (fields.containsKey("secret_key") && !fields.get("secret_key").isBlank()) params.put("sig", PublicRequestSigning.hash("MD5", "/ws/geocoder/v1/?" + PublicRequestSigning.raw(params) + required(fields, "secret_key")));
        }
        JsonNode response = call(HttpRequest.newBuilder(http.endpoint(path + "?" + PublicRequestSigning.form(params), true)).timeout(Duration.ofSeconds(15)).header("Accept", "application/json").GET().build());
        var results = json.createArrayNode();
        if (provider.equals("amap")) {
            if (!response.path("status").asText().equals("1") || !response.path("infocode").asText().equals("10000") || !response.path("geocodes").isArray()) throw unconfirmed();
            if (response.path("geocodes").size() > 20) throw unconfirmed();
            for (var entry : response.path("geocodes")) { String[] location = responseText(entry, "location", 100).split(",", -1); if (location.length != 2) throw unconfirmed(); results.add(location(location[0], location[1], responseText(entry, "formatted_address", 1000))); }
        } else {
            if (!response.path("status").asText().equals("0")) throw unconfirmed();
            JsonNode location = response.path("result").path("location");
            results.add(location(responseText(location, "lng", 100, true), responseText(location, "lat", 100, true), response.path("result").path("title").isTextual() ? responseText(response.path("result"), "title", 1000) : address));
        }
        var result = json.createObjectNode().put("provider", provider).put("coordinateSystem", "GCJ-02").put("found", !results.isEmpty()); result.set("results", results); return result;
    }

    private ObjectNode location(String lng, String lat, String address) {
        try { BigDecimal x = new BigDecimal(lng), y = new BigDecimal(lat); if (x.abs().compareTo(new BigDecimal("180")) > 0 || y.abs().compareTo(new BigDecimal("90")) > 0) throw unconfirmed(); return json.createObjectNode().put("longitude", x.toPlainString()).put("latitude", y.toPlainString()).put("formattedAddress", address); }
        catch (NumberFormatException e) { throw unconfirmed(); }
    }
    private HttpRequest.Builder post(URI uri, JsonNode body) throws Exception { return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json").header("Accept", "application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))); }
    private JsonNode call(HttpRequest request) throws Exception { JsonNode response = json.readTree(http.send(request)); if (response == null || !response.isObject()) throw unconfirmed(); return response; }
    private static void rejectError(JsonNode node) { if (node.has("error") || node.has("Error")) throw unconfirmed(); }
    private static int integer(JsonNode input, String key, int minimum, int maximum) { JsonNode value = input.get(key); if (value == null || !value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() < minimum || value.intValue() > maximum) throw invalid("PUBLIC_API_INTEGER_REJECTED"); return value.intValue(); }
    private static void copyText(JsonNode input, ObjectNode output, String source, String target, int limit) { if (input.hasNonNull(source)) output.put(target, responseText(input, source, limit)); }
    private static String responseText(JsonNode value, String key, int limit) { return responseText(value, key, limit, false); }
    private static String responseText(JsonNode value, String key, int limit, boolean number) { try { return text(value, key, limit, number); } catch (IllegalArgumentException e) { throw unconfirmed(); } }
    private static String language(JsonNode input, String key) { String value = text(input, key, 16); if (!value.matches("[A-Za-z][A-Za-z0-9-]{0,15}")) throw invalid("PUBLIC_API_LANGUAGE_REJECTED"); return value; }
    private static long project(Map<String, String> fields) { try { long value = Long.parseLong(required(fields, "project_id")); if (value < 0) throw invalid("PUBLIC_API_CONFIGURATION_REJECTED"); return value; } catch (NumberFormatException e) { throw invalid("PUBLIC_API_CONFIGURATION_REJECTED"); } }
    private String customBase(String value) { URI uri = http.endpoint(value, false); if (uri.getRawQuery() != null || value.endsWith("?")) throw invalid("PUBLIC_API_ENDPOINT_REJECTED"); return value.replaceAll("/+$", ""); }
    private static String repository(String value) { if (!value.matches("https://github\\.com/[A-Za-z0-9_-]+/[A-Za-z0-9_.-]+") || value.endsWith("/..") || value.endsWith("/.")) throw invalid("CURSOR_REPOSITORY_REJECTED"); return value; }
    private static String required(Map<String, String> values, String key) { String value = values.get(key); if (value == null || value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0 || value.length() > 16000) throw invalid("PUBLIC_API_CONFIGURATION_REJECTED"); return value; }
    private static String text(JsonNode value, String key, int limit) { return text(value, key, limit, false); }
    private static String text(JsonNode value, String key, int limit, boolean number) { JsonNode node = value.get(key); if (node == null || !node.isTextual() && !(number && node.isNumber())) throw invalid("PUBLIC_API_INPUT_REJECTED"); return bounded(node.asText(), limit); }
    private static String bounded(String value, int limit) { if (value.isBlank() || value.length() > limit || value.indexOf('\0') >= 0) throw invalid("PUBLIC_API_INPUT_REJECTED"); return value; }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
    private static IllegalStateException unconfirmed() { return new IllegalStateException("PUBLIC_API_RESPONSE_UNCONFIRMED"); }
}
