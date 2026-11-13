package com.ruoyi.project.face.domain;

import lombok.Data;

@Data
public class ClassReviewTodayVo {
    private Long sessionId;
    private Long itemId;
    /** 0 草稿 / 1 已发送 */
    private String status;
}
