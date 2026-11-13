package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ScheduleSaveRequest {
    private Long id;
    private Long teacherUserId;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    private String remark;
    private List<Long> studentIds;
}
