-- -*- coding: utf-8 -*-
-- 中道退费流水表及菜单权限

CREATE TABLE IF NOT EXISTS cf_student_fee_refund (
    id           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    fee_id       BIGINT         NOT NULL COMMENT '缴费ID',
    hours        INT            NOT NULL DEFAULT 0 COMMENT '退费课时',
    amount       DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '退费金额',
    refund_time  DATETIME       NULL COMMENT '退费时间',
    remark       VARCHAR(255)   NULL COMMENT '备注',
    create_by    VARCHAR(64)    NULL,
    create_time  DATETIME       NULL,
    PRIMARY KEY (id),
    KEY idx_cf_fee_refund_fee (fee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费退费流水';

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2108, '中道退费', 2100, 5, '#', '', 'face:fee:refund', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2108);
