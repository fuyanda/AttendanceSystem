package com.ruoyi.project.face.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

/**
 * 家长微信服务号绑定请求
 */
@Data
public class ParentWechatBindVo {

    @NotBlank(message = "手机号不可为空！")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请输入家长绑定码！")
    private String bindCode;

    @NotBlank(message = "请关注服务号后在服务号链接界面中进行绑定")
    private String openId;

    private String name;

    /** 已注册家长绑定时必填 */
    private String password;
}
