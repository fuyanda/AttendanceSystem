package com.ruoyi.project.face.domain;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 扫码绑定请求：回访家长仅需 openId+bindCode；新家长需 phone
 */
@Data
public class ParentQrBindVo {

    @NotBlank(message = "请关注服务号后扫码进入绑定")
    private String openId;

    @NotBlank(message = "绑定码不能为空")
    private String bindCode;

    /** 新家长必填 */
    private String phone;

    private String name;
}
