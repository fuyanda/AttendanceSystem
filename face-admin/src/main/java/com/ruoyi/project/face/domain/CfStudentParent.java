package com.ruoyi.project.face.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.framework.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CfStudentParent extends BaseEntity {

    private Long id;

    private Long studentId;

    private Long userId;

    private String relationType;

    private Integer isPrimary;

    private Integer notifyEnabled;

    @TableField(exist = false)
    private String phone;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String openId;

    @TableField(exist = false)
    private String studentName;
}
