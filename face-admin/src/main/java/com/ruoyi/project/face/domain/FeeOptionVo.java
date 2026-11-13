package com.ruoyi.project.face.domain;

import lombok.Data;

@Data
public class FeeOptionVo {
    private Long feeId;
    private Long teacherUserId;
    private String subject;
    private String subjectLabel;
    private Integer totalHours;
    private Integer usedHours;
    private Integer remainingHours;
    private String teacherName;
    private Boolean scheduledToday;
    private String scheduleSummary;
}
