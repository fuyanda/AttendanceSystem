package com.ruoyi.project.face.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeLedgerSummaryVo {
    private int payCount;
    private int payHours;
    private BigDecimal payAmount = BigDecimal.ZERO;

    private int consumeCount;
    private int consumeHours;
    private BigDecimal consumeAmount = BigDecimal.ZERO;

    private int refundCount;
    private int refundHours;
    private BigDecimal refundAmount = BigDecimal.ZERO;

    /** 净收入 = 缴费 - 退费 */
    private BigDecimal netAmount = BigDecimal.ZERO;
}
