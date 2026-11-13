package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentBindResultVo {

    private String phone;

    private String password;

    private boolean newlyCreated;

    private String bindCode;

    private List<String> studentNames;

    private int boundCount;
}
