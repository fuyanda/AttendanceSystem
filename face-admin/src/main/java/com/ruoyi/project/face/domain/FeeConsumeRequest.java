package com.ruoyi.project.face.domain;

import lombok.Data;

@Data
public class FeeConsumeRequest {
    private Long feeId;
    private Integer hours;
    private String remark;
}
