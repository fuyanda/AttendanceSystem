package com.ruoyi.project.face.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class StudentParentBindVo {

    private Long userId;

    @NotBlank(message = "家长电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "家长电话格式不正确")
    private String phone;

    private String name;

    private String relationType;

    private Integer isPrimary;

    private Integer notifyEnabled;
}
