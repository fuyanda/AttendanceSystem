package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAccountVo {

    private String phone;

    private String password;

    private boolean newlyCreated;
}
