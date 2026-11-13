package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FeeLedgerItemVo {
    private String bizType;
    private String bizTypeLabel;
    private Long feeId;
    private String studentName;
    private String subject;
    private String subjectLabel;
    private String teacherName;
    private Integer hours;
    private BigDecimal amount;
    private String remark;
    private String operator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bizTime;
}
