package com.kuaixietech.integrationsdk;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** JDK-only implementations of published Tencent TC3 and Alibaba RPC signing rules. */
final class PublicRequestSigning {
    private PublicRequestSigning() { }
    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }
    static String form(Map<String, String> values) {
        return new TreeMap<>(values).entrySet().stream().map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
    }
    static String raw(Map<String, String> values) {
        return new TreeMap<>(values).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }
    static String hash(String algorithm, String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("PUBLIC_API_SIGNING_UNAVAILABLE"); }
    }
    static byte[] hmac(String algorithm, byte[] key, String value) {
        try { var mac = Mac.getInstance(algorithm); mac.init(new SecretKeySpec(key, algorithm)); return mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception e) { throw new IllegalStateException("PUBLIC_API_SIGNING_UNAVAILABLE"); }
    }
    static String tc3(String service, String host, String body, Map<String, String> fields, Instant now) {
        String type = "application/json; charset=utf-8";
        String canonical = "POST\n/\n\ncontent-type:" + type + "\nhost:" + host + "\n\ncontent-type;host\n" + hash("SHA-256", body);
        String date = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(now);
        String scope = date + "/" + service + "/tc3_request";
        String value = "TC3-HMAC-SHA256\n" + now.getEpochSecond() + "\n" + scope + "\n" + hash("SHA-256", canonical);
        byte[] dateKey = hmac("HmacSHA256", ("TC3" + fields.get("secret_key")).getBytes(StandardCharsets.UTF_8), date);
        byte[] serviceKey = hmac("HmacSHA256", dateKey, service);
        byte[] signingKey = hmac("HmacSHA256", serviceKey, "tc3_request");
        return "TC3-HMAC-SHA256 Credential=" + fields.get("secret_id") + "/" + scope
            + ", SignedHeaders=content-type;host, Signature=" + HexFormat.of().formatHex(hmac("HmacSHA256", signingKey, value));
    }
    static Map<String, String> rpc(String action, String version, Map<String, String> fields,
        Map<String, String> input, Instant now, String nonce) {
        var values = new TreeMap<String, String>(input);
        values.put("Action", action); values.put("Version", version); values.put("Format", "JSON");
        values.put("AccessKeyId", fields.get("access_key_id")); values.put("RegionId", fields.get("region"));
        values.put("SignatureMethod", "HMAC-SHA1"); values.put("SignatureVersion", "1.0"); values.put("SignatureNonce", nonce);
        values.put("Timestamp", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC).format(now));
        String toSign = "POST&%2F&" + encode(form(values));
        values.put("Signature", Base64.getEncoder().encodeToString(hmac("HmacSHA1", (fields.get("access_key_secret") + "&").getBytes(StandardCharsets.UTF_8), toSign)));
        return values;
    }
}
