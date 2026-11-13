package com.ruoyi.project.face.domain;

import lombok.Data;

@Data
public class FeeConsumeStudentVo {
    private Long feeId;
    private Long studentId;
    private String studentName;
    private String subject;
    private String subjectLabel;
    private Integer totalHours;
    private Integer usedHours;
    private Integer remainingHours;
    private String scheduleSummary;
}
