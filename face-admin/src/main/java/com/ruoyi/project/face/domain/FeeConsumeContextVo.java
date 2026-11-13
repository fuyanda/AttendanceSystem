package com.ruoyi.project.face.domain;

import lombok.Data;

import java.util.List;

@Data
public class FeeConsumeContextVo {
    private Long userId;
    private String nickName;
    private Long deptId;
    /** 是否可选择其他任课老师（管理员 / 校区负责人岗位） */
    private boolean canSelectTeacher;
    private Long defaultTeacherUserId;
    /** 任课老师选项：教师角色仅含本人，负责人含可选列表 */
    private List<FeeTeacherVo> teachers;
}
