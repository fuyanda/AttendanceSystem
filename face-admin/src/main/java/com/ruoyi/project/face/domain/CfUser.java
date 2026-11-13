package com.ruoyi.project.face.domain;

import lombok.Builder;
import lombok.Data;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 用户信息对象 cf_user
 *
 * @author fuyd
 * @date 2025-10-21
 */
@Data
@Builder
public class CfUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 家庭编码 */
    // @Excel(name = "家庭编码")
    private String code;

    /** 姓名 */
    @Excel(name = "姓名")
    private String name;

    /** 联系电话 */
    @Excel(name = "联系电话/账号")
    private String phone;
    @Excel(name = "密码")
    private String password;

    /** 唯一标识（如微信openID） */
    // @Excel(name = "唯一标识", readConverterExp = "如=微信openID")
    private String openId;

    /** 是否可邀请其他人（0否/1是） */
    // @Excel(name = "是否可邀请其他人", readConverterExp = "0=否/1是")
    private Integer isInviteEnabled;

    /** 删除标志（0存在/1删除） */
    private String delFlag;

    /** 家长绑定学生时使用（非数据库字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bindCode;
}
