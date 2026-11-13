package com.ruoyi.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "baidu.face")
public class BaiduFaceProperties {

    private boolean enabled = false;

    private String appId = "";

    private String apiKey = "";

    private String secretKey = "";

    private String groupId = "chengfeng_students";

    /** 匹配分数阈值 0-100 */
    private double scoreThreshold = 80d;
}
