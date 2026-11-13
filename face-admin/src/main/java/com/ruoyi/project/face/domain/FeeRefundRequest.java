package com.ruoyi.project.face.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeRefundRequest {
    private Long feeId;
    /** 退费课时，不传则退剩余全部 */
    private Integer hours;
    /** 退费金额，不传则按课时单价折算 */
    private BigDecimal amount;
    private String remark;
}
