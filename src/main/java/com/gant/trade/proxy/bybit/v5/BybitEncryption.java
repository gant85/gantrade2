package com.gant.trade.proxy.bybit.v5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

@Getter
@Setter
public class BybitEncryption {

    private String apiKey;
    private String apiSecret;
    private String recvWindow = "5000";
    private String signType = "2";

    public BybitEncryption(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public BybitEncryption(String apiKey, String apiSecret, String recvWindow, String signType) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.recvWindow = recvWindow;
        this.signType = signType;
    }

    public String genPostSign(Object params, String TIMESTAMP) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        String paramJson = new ObjectMapper().writeValueAsString(params);
        String sb = TIMESTAMP + apiKey + recvWindow + paramJson;
        return bytesToHex(sha256_HMAC.doFinal(sb.getBytes()));
    }

    public String genGetSign(LinkedHashMap<String, Object> params, String TIMESTAMP) throws NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder sb = genQueryStr(params);
        String queryStr = TIMESTAMP + apiKey + recvWindow + sb;

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return bytesToHex(sha256_HMAC.doFinal(queryStr.getBytes()));
    }

    public String genWebSocketSign(long expires) throws NoSuchAlgorithmException, InvalidKeyException {
        String _val = "GET/realtime" + expires;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        byte[] hash = sha256_HMAC.doFinal(_val.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private StringBuilder genQueryStr(LinkedHashMap<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return new StringBuilder("");
        }
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            String key = iter.next();
            sb.append(key)
                    .append("=")
                    .append(map.get(key))
                    .append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb;
    }
}
