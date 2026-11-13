package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceSyncResultVo {

    private int total;

    private int success;

    private int failed;
}
