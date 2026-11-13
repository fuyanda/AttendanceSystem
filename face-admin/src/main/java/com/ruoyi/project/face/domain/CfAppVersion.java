package com.ruoyi.project.face.domain;

import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App版本对象 cf_app_version
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CfAppVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "平台")
    private String platform;

    @Excel(name = "版本名")
    private String versionName;

    @Excel(name = "版本号")
    private Integer versionCode;

    @Excel(name = "APK地址")
    private String apkUrl;

    /** 0可选 1强制 */
    @Excel(name = "强制更新", readConverterExp = "0=可选,1=强制")
    private String forceUpdate;

    @Excel(name = "更新说明")
    private String changelog;

    /** 0停用 1启用 */
    @Excel(name = "状态", readConverterExp = "0=停用,1=启用")
    private String status;
}
