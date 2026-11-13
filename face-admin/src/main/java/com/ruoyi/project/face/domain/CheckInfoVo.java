package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信通知详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInfoVo {
    // 人脸照片
    private String avatar;
    // 打卡类型
    private String checkType;
    // 学生姓名
    private String studentName;
    // 打开时间
    private String createTime;
    // 打卡校区
    private String school;
    /** 年级/班级等展示用 */
    private String project;
}
