-- -*- coding: utf-8 -*-
-- App 版本（Android APK 整包更新）

CREATE TABLE IF NOT EXISTS cf_app_version (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    platform      VARCHAR(16)   NOT NULL DEFAULT 'android' COMMENT '平台 android',
    version_name  VARCHAR(32)   NOT NULL COMMENT '版本名 如1.1.4',
    version_code  INT           NOT NULL COMMENT '版本号 比较用',
    apk_url       VARCHAR(500)  NOT NULL COMMENT 'APK下载地址',
    force_update  CHAR(1)       NOT NULL DEFAULT '0' COMMENT '0可选 1强制',
    changelog     VARCHAR(2000) NULL COMMENT '更新说明',
    status        CHAR(1)       NOT NULL DEFAULT '1' COMMENT '0停用 1启用',
    remark        VARCHAR(500)  NULL,
    create_by     VARCHAR(64)   NULL,
    create_time   DATETIME      NULL,
    update_by     VARCHAR(64)   NULL,
    update_time   DATETIME      NULL,
    PRIMARY KEY (id),
    KEY idx_app_version_platform_code (platform, status, version_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App版本';

-- 菜单：挂在缴费(2100)同级业务下
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2121, 'App版本', p.parent_id, IFNULL(p.order_num, 0) + 6, 'appVersion', 'face/appVersion/index', '', '', 1, 0, 'C', '0', '0', 'face:appVersion:list', 'upload', 'admin', NOW(), '教师端Android版本发布'
FROM sys_menu p WHERE p.menu_id = 2100 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2121);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2122, '版本查询', 2121, 1, '#', '', 'face:appVersion:query', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2122);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2123, '版本新增', 2121, 2, '#', '', 'face:appVersion:add', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2123);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2124, '版本修改', 2121, 3, '#', '', 'face:appVersion:edit', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2124);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2125, '版本删除', 2121, 4, '#', '', 'face:appVersion:remove', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2125);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, mid
FROM sys_role_menu rm
CROSS JOIN (
    SELECT 2121 AS mid UNION SELECT 2122 UNION SELECT 2123 UNION SELECT 2124 UNION SELECT 2125
) t
WHERE rm.menu_id IN (2100, 2000)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = t.mid);
