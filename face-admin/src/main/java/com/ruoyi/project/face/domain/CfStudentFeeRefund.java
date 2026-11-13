package com.ruoyi.project.face.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CfStudentFeeRefund {
    private Long id;
    private Long feeId;
    private Integer hours;
    private BigDecimal amount;
    private Date refundTime;
    private String remark;
    private String createBy;
    private Date createTime;
}
