package com.ruoyi.project.face.domain;

import lombok.Data;

@Data
public class CfStudentFeeSchedule {
    private Long id;
    private Long feeId;
    /** 1-7 周一至周日 */
    private Integer weekDay;
    private String startTime;
    private String endTime;
}
