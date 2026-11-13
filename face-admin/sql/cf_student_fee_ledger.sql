-- -*- coding: utf-8 -*-
-- 缴费台账统计菜单

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2109, '缴费台账', p.parent_id, IFNULL(p.order_num, 0) + 2, 'feeLedger', 'face/fee/ledger/index', '', '', 1, 0, 'C', '0', '0', 'face:fee:ledger', 'chart', 'admin', NOW(), '缴费消课退费台账统计'
FROM sys_menu p
WHERE p.menu_id = 2100
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2109);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2110, '台账查询', 2109, 1, '#', '', 'face:fee:ledger', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2110);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2109
FROM sys_role_menu rm
WHERE rm.menu_id = 2100
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 2109);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2109 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2109);
