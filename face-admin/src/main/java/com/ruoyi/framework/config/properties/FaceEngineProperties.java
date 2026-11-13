package com.ruoyi.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Face engine routing: search is single-select, register can dual-write.
 */
@Data
@Component
@ConfigurationProperties(prefix = "face.engine")
public class FaceEngineProperties {

    /** Search provider: baidu | local */
    private String searchProvider = "baidu";

    /** Whether register writes to Baidu */
    private boolean registerBaidu = true;

    /** Whether register writes to local face-service */
    private boolean registerLocal = false;
}
