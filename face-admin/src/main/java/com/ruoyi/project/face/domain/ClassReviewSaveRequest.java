package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ClassReviewSaveRequest {
    private Long sessionId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date classTime;
    private String classContent;
    private String classImages;
    private List<CfClassReviewItem> items;
}
