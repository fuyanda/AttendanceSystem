package com.ruoyi.project.face.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ClassReviewInfoVo {
    private String studentName;
    private String subjectLabel;
    private String teacherName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date classTime;
    private String classContent;
    private List<String> classImages;
    private Integer disciplineScore;
    private Integer participationScore;
    private String content;
    private List<String> images;
    private String school;
}
