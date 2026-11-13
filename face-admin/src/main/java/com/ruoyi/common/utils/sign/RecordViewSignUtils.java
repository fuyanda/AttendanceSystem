package com.ruoyi.common.utils.sign;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HMAC signature for parent-facing record detail links (prevents ID enumeration).
 */
public final class RecordViewSignUtils {

    private static final int EXPIRE_SECONDS = 30 * 24 * 60 * 60;

    private RecordViewSignUtils() {
    }

    public static long defaultExpireEpochSeconds() {
        return System.currentTimeMillis() / 1000L + EXPIRE_SECONDS;
    }

    public static String sign(String secret, long recordId, String recordType, long expireEpochSeconds) {
        String payload = recordId + "|" + normalizeType(recordType) + "|" + expireEpochSeconds;
        return hmacSha256Hex(secret, payload);
    }

    public static boolean verify(String secret, long recordId, String recordType,
                                 long expireEpochSeconds, String sign) {
        if (StringUtils.isBlank(secret) || StringUtils.isBlank(sign)) {
            return false;
        }
        if (expireEpochSeconds < System.currentTimeMillis() / 1000L) {
            return false;
        }
        String expected = sign(secret, recordId, recordType, expireEpochSeconds);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                sign.getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizeType(String recordType) {
        if ("checkout".equalsIgnoreCase(recordType)) {
            return "checkout";
        }
        if ("review".equalsIgnoreCase(recordType)) {
            return "review";
        }
        return "checkin";
    }

    private static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("record view sign failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
