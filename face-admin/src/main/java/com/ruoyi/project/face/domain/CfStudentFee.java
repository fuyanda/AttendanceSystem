package com.ruoyi.project.face.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CfStudentFee extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long studentId;
    @Excel(name = "科目")
    private String subject;
    private Integer totalHours;
    private Integer usedHours;
    @Excel(name = "课时费")
    private BigDecimal totalAmount;
    private Long teacherUserId;
    /** 0有效 1关闭 */
    private String status;
    private String delFlag;

    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private String teacherName;
    @TableField(exist = false)
    private Integer remainingHours;
    @TableField(exist = false)
    private String scheduleSummary;
    @TableField(exist = false)
    private List<CfStudentFeeSchedule> schedules;
}
