package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CfScheduleSession extends BaseEntity {
    private Long id;
    private Long teacherUserId;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /** fee / manual */
    private String source;
    private Long feeId;
    private Long feeScheduleId;
    /** 0未编辑 1已编辑 */
    private String editedFlag;
    private String delFlag;

    private String teacherName;
    private String subjectLabel;
    private String periodLabel;
    private Integer studentCount;
    private List<CfScheduleStudent> students;
    private List<Long> studentIds;
}
