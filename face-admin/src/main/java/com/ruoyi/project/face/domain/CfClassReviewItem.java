package com.ruoyi.project.face.domain;

import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CfClassReviewItem extends BaseEntity {
    private Long id;
    private Long sessionId;
    private Long studentId;
    private Long feeId;
    private Long consumeId;
    private Integer disciplineScore;
    private Integer participationScore;
    private String content;
    private String images;
    private String pushFlag;

    private String studentName;
    private String studentAvatar;
    private Integer consumeHours;
    private Integer remainingHours;
}
