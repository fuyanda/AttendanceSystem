package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CfStudentFeeConsume {
    private Long id;
    private Long feeId;
    private Integer hours;
    private String source;
    private Long checkinId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date consumeTime;
    private String remark;
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
