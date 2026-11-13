package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 手动签退候选学生（今日已签到且当前时段可签退）
 */
@Data
public class ManualCheckoutCandidateVo {

    private Long checkinId;

    private Long studentId;

    private String studentName;

    private String avatar;

    private String deptId;

    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinTime;
}
