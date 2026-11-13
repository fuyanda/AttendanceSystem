package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ClassReviewCreateRequest {
    private Long studentId;
    /** 科目字典值 cf_fee_subject，可空时默认 general */
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date classTime;
}
