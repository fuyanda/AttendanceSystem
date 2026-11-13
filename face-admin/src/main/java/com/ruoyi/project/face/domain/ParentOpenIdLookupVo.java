package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按 openId 查询是否已有家长账号
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentOpenIdLookupVo {
    private boolean exists;
    private String name;
    /** 脱敏手机号，如 138****8000 */
    private String phoneMasked;
}
