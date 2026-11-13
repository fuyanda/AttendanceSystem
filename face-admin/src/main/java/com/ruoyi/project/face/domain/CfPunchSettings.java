package com.ruoyi.project.face.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalTime;

/**
 * 打卡设置对象 cf_punch_settings
 *
 * @author 付延达
 * @date 2024-08-24
 */
@Data
@TableName("cf_punch_settings")
@EqualsAndHashCode(callSuper = true)
public class CfPunchSettings extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
    private String id;

    /**
     * 校区主键
     */
    @Excel(name = "校区主键")
    private String deptId;

    /**
     * 时间段名称
     */
    @Excel(name = "时间段名称")
    private String name;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "HH:mm")
    @Excel(name = "开始时间", width = 30, dateFormat = "HH:mm")
    private LocalTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "HH:mm")
    @Excel(name = "结束时间", width = 30, dateFormat = "HH:mm")
    private LocalTime endTime;

    /**
     * 状态，1启用0未启用
     */
    @Excel(name = "状态，1启用0未启用")
    private String status;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("deptId", getDeptId())
                .append("name", getName())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("status", getStatus())
                .toString();
    }
}
