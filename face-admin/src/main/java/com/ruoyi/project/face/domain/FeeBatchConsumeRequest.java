package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FeeBatchConsumeRequest {
    private Long teacherUserId;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date consumeTime;
    private Integer hours;
    private String remark;
    private List<Long> feeIds;
}
