package com.ruoyi.project.face.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 学生管理对象 cf_student
 *
 * @author ruoyi
 * @date 2024-10-17
 */
@Data
@TableName("cf_student")
@EqualsAndHashCode(callSuper = true)
public class CfStudent extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 姓名
     */
    @Excel(name = "姓名")
    private String name;

    /**
     * 年龄
     */
    @Excel(name = "年龄")
    private Long age;

    /**
     * 性别
     */
    @Excel(name = "性别")
    private String sex;

    /**
     * 人脸照片
     */
    @Excel(name = "人脸照片")
    private String avatar;

    /**
     * 校区ID
     */
    @Excel(name = "校区ID")
    private String deptId;

    /** 校区名称（关联查询，非表字段） */
    @TableField(exist = false)
    private String deptName;

    /**
     * 家庭组
     */
    @Excel(name = "家庭组")
    private String family;

    /**
     * 缴费截止日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "缴费截止日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deadline;

    /**
     * 父母电话
     */
    @Excel(name = "父母电话")
    private String parentTel;

    /**
     * 学生电话
     */
    @Excel(name = "学生电话")
    private String phone;

    /**
     * 年级
     */
    @Excel(name = "年级")
    private String grade;

    /**
     * 学校
     */
    @Excel(name = "学校")
    private String school;

    /**
     * 家长绑定码
     */
    @Excel(name = "绑定码")
    private String bindCode;

    /** 绑定的家长列表（非数据库字段） */
    @TableField(exist = false)
    private java.util.List<StudentParentBindVo> parents;
}
