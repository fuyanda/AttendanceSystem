package com.ruoyi.project.face.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 人脸相关日志辅助：记录诊断数据，脱敏大字段与密钥。
 */
public final class FaceLogSupport {

    private static final int MAX_LOG_LEN = 2000;
    private static final Set<String> SENSITIVE_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "password", "oldpassword", "newpassword", "passwd",
            "secret", "secretkey", "apikey", "accesstoken",
            "token", "authorization", "imagebase64", "image", "file", "avatarbase64"
    )));

    private FaceLogSupport() {
    }

    /** 图片字段摘要，不落完整 base64。 */
    public static String imageMeta(String imageBase64) {
        if (StringUtils.isEmpty(imageBase64)) {
            return "empty";
        }
        int len = imageBase64.length();
        long approxBytes = len * 3L / 4L;
        String prefix = "";
        int comma = imageBase64.indexOf(',');
        if (comma > 0 && imageBase64.substring(0, Math.min(comma, 64)).toLowerCase(Locale.ROOT).contains("base64")) {
            prefix = imageBase64.substring(0, comma);
        }
        return "len=" + len + ",≈" + (approxBytes / 1024) + "KB"
                + (StringUtils.isEmpty(prefix) ? "" : ",prefix=" + prefix);
    }

    public static String truncate(String value) {
        return truncate(value, MAX_LOG_LEN);
    }

    public static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...(truncated,len=" + value.length() + ")";
    }

    /** 脱敏 JSON / 普通文本请求体，便于异常日志附带业务数据。 */
    public static String sanitizePayload(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return "";
        }
        String trimmed = raw.trim();
        try {
            if (trimmed.startsWith("{")) {
                return truncate(JSON.toJSONString(sanitizeNode(JSON.parseObject(trimmed))));
            }
            if (trimmed.startsWith("[")) {
                return truncate(JSON.toJSONString(sanitizeNode(JSON.parseArray(trimmed))));
            }
        } catch (Exception ignored) {
            // fall through
        }
        return truncate(raw);
    }

    public static String sanitizeMap(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        try {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, ?> e : map.entrySet()) {
                obj.put(e.getKey(), e.getValue());
            }
            return truncate(JSON.toJSONString(sanitizeNode(obj)));
        } catch (Exception ex) {
            return truncate(String.valueOf(map));
        }
    }

    /** 百度请求体摘要：去掉 image，保留业务字段。 */
    public static String baiduBodyMeta(JSONObject body) {
        if (body == null) {
            return "";
        }
        JSONObject copy = new JSONObject(body);
        if (copy.containsKey("image")) {
            Object image = copy.get("image");
            copy.put("image", imageMeta(image == null ? null : String.valueOf(image)));
        }
        return truncate(copy.toJSONString());
    }

    private static Object sanitizeNode(Object node) {
        if (node instanceof JSONObject) {
            JSONObject obj = (JSONObject) node;
            JSONObject out = new JSONObject();
            for (String key : obj.keySet()) {
                Object val = obj.get(key);
                if (isSensitiveKey(key)) {
                    out.put(key, summarizeSensitive(key, val));
                } else {
                    out.put(key, sanitizeNode(val));
                }
            }
            return out;
        }
        if (node instanceof JSONArray) {
            JSONArray arr = (JSONArray) node;
            JSONArray out = new JSONArray();
            for (int i = 0; i < arr.size(); i++) {
                out.add(sanitizeNode(arr.get(i)));
            }
            return out;
        }
        if (node instanceof String) {
            String s = (String) node;
            if (s.length() > 200 && looksLikeBase64(s)) {
                return imageMeta(s);
            }
            return s;
        }
        return node;
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String k = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        if (SENSITIVE_KEYS.contains(k)) {
            return true;
        }
        return k.contains("password") || k.contains("secret") || k.endsWith("base64")
                || "image".equals(k) || k.contains("accesstoken");
    }

    private static String summarizeSensitive(String key, Object val) {
        if (val == null) {
            return "null";
        }
        String s = String.valueOf(val);
        String k = key == null ? "" : key.toLowerCase(Locale.ROOT);
        if (k.contains("image") || k.contains("base64") || k.equals("file") || looksLikeBase64(s)) {
            return imageMeta(s);
        }
        return "****(len=" + s.length() + ")";
    }

    private static boolean looksLikeBase64(String s) {
        if (s == null || s.length() < 80) {
            return false;
        }
        String sample = s.substring(0, Math.min(120, s.length())).replaceAll("\\s+", "");
        if (sample.toLowerCase(Locale.ROOT).contains("base64,")) {
            return true;
        }
        return sample.matches("^[A-Za-z0-9+/=]+$");
    }
}
