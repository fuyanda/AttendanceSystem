package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫码绑定预览（仅学生姓名）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentBindPreviewVo {
    private String bindCode;
    private String studentName;
}
