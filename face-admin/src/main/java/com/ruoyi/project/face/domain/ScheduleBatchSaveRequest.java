package com.ruoyi.project.face.domain;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleBatchSaveRequest {
    private Long teacherUserId;
    private String subject;
    private String remark;
    private List<Long> studentIds;
    private List<ScheduleTimeSlotRequest> slots;
}
