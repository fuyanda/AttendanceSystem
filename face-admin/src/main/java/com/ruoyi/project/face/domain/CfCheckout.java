package com.ruoyi.project.face.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 签退对象 cf_checkout
 *
 * @author fuyd
 * @date 2025-08-14
 */
@Data
public class CfCheckout extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 学生ID
     */
    @Excel(name = "学生ID")
    private Long studentId;

    /**
     * 人脸照片
     */
    @Excel(name = "人脸照片")
    private String avatar;

    /**
     * 推送标志（0代表未推 1代表推送）
     */
    @Excel(name = "推送标志", readConverterExp = "0=代表未推,1=代表推送")
    private String pushFlag;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /** 手机端签到时候的头像 **/
    @TableField(exist = false)
    private String imageBase64;
    /** 手机端只查询今天的 **/
    @TableField(exist = false)
    private boolean today;
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String deptId;

    @TableField(exist = false)
    private String deptName;
}
