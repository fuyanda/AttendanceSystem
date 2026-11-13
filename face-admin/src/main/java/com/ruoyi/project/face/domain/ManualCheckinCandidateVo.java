package com.ruoyi.project.face.domain;

import lombok.Data;

/**
 * 手动签到候选学生（今日尚未签到）
 */
@Data
public class ManualCheckinCandidateVo {

    private Long studentId;

    private String studentName;

    private String avatar;

    private String deptId;

    private String deptName;
}
