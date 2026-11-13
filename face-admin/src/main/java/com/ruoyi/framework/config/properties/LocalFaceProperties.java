package com.ruoyi.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Local face-service (InsightFace) connection settings.
 */
@Data
@Component
@ConfigurationProperties(prefix = "face.local")
public class LocalFaceProperties {

    /** Service root without /api/v1, e.g. http://127.0.0.1:8080 */
    private String baseUrl = "http://127.0.0.1:8080";

    /** API prefix */
    private String apiPrefix = "/api/v1";

    /** Cosine similarity threshold 0~1 */
    private double similarityThreshold = 0.58d;

    private int connectTimeoutMs = 5000;

    private int readTimeoutMs = 60000;

    public String apiBase() {
        String root = baseUrl == null ? "" : baseUrl.trim();
        if (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        String prefix = apiPrefix == null ? "/api/v1" : apiPrefix.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return root + prefix;
    }
}
