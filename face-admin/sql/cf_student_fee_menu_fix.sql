-- -*- coding: utf-8 -*-
-- 修复缴费/批量消课菜单不显示
-- 原因1：menu_type=F（2105/2107）仅为按钮权限，不会出现在侧栏
-- 原因2：2106 挂在 C 类型菜单 2100 下，RuoYi 不会渲染其子页面，需改为同级菜单

-- 批量消课：与「缴费管理」同级（同一 parent_id）
UPDATE sys_menu m
JOIN sys_menu p ON p.menu_id = 2100
SET m.parent_id     = p.parent_id,
    m.menu_type     = 'C',
    m.visible       = '0',
    m.status        = '0',
    m.order_num     = IFNULL(p.order_num, 0) + 1,
    m.path          = 'feeConsume',
    m.component     = 'face/fee/consume/index',
    m.perms         = 'face:fee:consume',
    m.is_frame      = '1',
    m.is_cache      = '0',
    m.icon          = 'edit',
    m.menu_name     = '批量消课',
    m.remark        = '老师批量消课'
WHERE m.menu_id = 2106;

-- 若 2106 不存在则插入（与 2100 同级）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2106, '批量消课', p.parent_id, IFNULL(p.order_num, 0) + 1, 'feeConsume', 'face/fee/consume/index', '', '', 1, 0, 'C', '0', '0', 'face:fee:consume', 'edit', 'admin', NOW(), '老师批量消课'
FROM sys_menu p
WHERE p.menu_id = 2100
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2106);

-- 代课消课：按钮权限（不显示在侧栏，供校区负责人选其他老师）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2107, '代课消课', 2106, 1, '#', '', 'face:fee:consume:assign', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2107);

UPDATE sys_menu SET menu_name = '代课消课', parent_id = 2106 WHERE menu_id = 2107;

-- 手动消课(2105) 为按钮权限，挂到批量消课下
UPDATE sys_menu SET parent_id = 2106, menu_name = '手动消课' WHERE menu_id = 2105;

-- 修复菜单中文名
UPDATE sys_menu SET menu_name = '缴费管理', remark = '学生缴费课时管理' WHERE menu_id = 2100;
UPDATE sys_menu SET menu_name = '批量消课', remark = '老师批量消课' WHERE menu_id = 2106;

-- 将新菜单授权给已拥有「缴费管理(2100)」的角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2106
FROM sys_role_menu rm
WHERE rm.menu_id = 2100
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 2106);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2107
FROM sys_role_menu rm
WHERE rm.menu_id IN (2100, 2106)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 2107);

-- 管理员角色兜底
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, mid FROM (
    SELECT 2106 AS mid UNION SELECT 2107
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = t.mid);
