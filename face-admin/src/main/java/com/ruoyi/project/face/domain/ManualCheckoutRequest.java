package com.ruoyi.project.face.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ManualCheckoutRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;
}
