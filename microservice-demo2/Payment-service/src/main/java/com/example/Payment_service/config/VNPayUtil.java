package com.example.Payment_service.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC-SHA512", e);
        }
    }

    public static String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public static String buildPaymentUrl(String baseUrl, String hashSecret, Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        String query = buildQueryString(sorted);
        String secureHash = hmacSHA512(hashSecret, query);
        return baseUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public static boolean verifySignature(String hashSecret, Map<String, String> params, String expectedHash) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        String signData = buildQueryString(sorted);
        String computed = hmacSHA512(hashSecret, signData);
        return computed.equals(expectedHash);
    }
}
