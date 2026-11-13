package com.ruoyi.project.face.domain;

import lombok.Data;

/**
 * Dept attendance summary VO.
 */
@Data
public class DeptCountVo {
    private String deptId;
    private String deptName;
    private int count;
}
