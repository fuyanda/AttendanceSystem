package com.ruoyi.project.face.domain;

import lombok.Data;

import java.util.Date;

@Data
public class CfScheduleStudent {
    private Long id;
    private Long sessionId;
    private Long studentId;
    private String createBy;
    private Date createTime;

    private String studentName;
    private String studentAvatar;
}
