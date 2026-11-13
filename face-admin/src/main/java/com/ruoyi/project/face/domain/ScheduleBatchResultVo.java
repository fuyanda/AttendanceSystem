package com.ruoyi.project.face.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScheduleBatchResultVo {
    private int successCount;
    private int failCount;
    private List<String> failMessages = new ArrayList<>();
}
