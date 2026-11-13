package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CfClassReviewSession extends BaseEntity {
    private Long id;
    private Long teacherUserId;
    private String subject;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date classTime;
    private String classContent;
    /** JSON 数组字符串 */
    private String classImages;
    /** 0草稿 1已发送 */
    private String status;
    private String delFlag;

    private String teacherName;
    private String subjectLabel;
    private Integer studentCount;
    private List<CfClassReviewItem> items;
}
