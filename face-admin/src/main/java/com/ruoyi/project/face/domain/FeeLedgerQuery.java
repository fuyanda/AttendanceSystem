package com.ruoyi.project.face.domain;

import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeeLedgerQuery extends BaseEntity {
    private String studentName;
    private String subject;
    /** pay / consume / refund */
    private String bizType;
    private Long teacherUserId;
}
