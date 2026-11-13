-- -*- coding: utf-8 -*-
-- 排课结束时间、消课时间、批量消课菜单

ALTER TABLE cf_student_fee_schedule
    ADD COLUMN end_time VARCHAR(8) NULL COMMENT '结束时间 HH:mm' AFTER start_time;

ALTER TABLE cf_student_fee_consume
    ADD COLUMN consume_time DATETIME NULL COMMENT '消课时间' AFTER checkin_id;

-- 批量消课：与缴费管理(2100)同级，勿挂在 C 类型菜单下
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2106, '批量消课', p.parent_id, IFNULL(p.order_num, 0) + 1, 'feeConsume', 'face/fee/consume/index', '', '', 1, 0, 'C', '0', '0', 'face:fee:consume', 'edit', 'admin', NOW(), '老师批量消课'
FROM sys_menu p
WHERE p.menu_id = 2100
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2106);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2107, '代课消课', 2106, 1, '#', '', 'face:fee:consume:assign', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2107);

UPDATE sys_menu SET menu_name = '批量消课', remark = '老师批量消课' WHERE menu_id = 2106;
UPDATE sys_menu SET menu_name = '代课消课' WHERE menu_id = 2107;
