package com.ruoyi.project.face.domain;

import lombok.Data;

/**
 * App 版本检测响应
 */
@Data
public class AppVersionCheckVo {
    private boolean needUpdate;
    private boolean forceUpdate;
    private String versionName;
    private Integer versionCode;
    private String apkUrl;
    private String changelog;

    public static AppVersionCheckVo noUpdate() {
        AppVersionCheckVo vo = new AppVersionCheckVo();
        vo.setNeedUpdate(false);
        vo.setForceUpdate(false);
        return vo;
    }
}
