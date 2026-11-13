-- -*- coding: utf-8 -*-
-- 排课：具体日期场次（可由缴费每周模板同步）

CREATE TABLE IF NOT EXISTS cf_schedule_session (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    teacher_user_id  BIGINT       NOT NULL COMMENT '任课老师',
    subject          VARCHAR(32)  NOT NULL COMMENT '科目(字典cf_fee_subject)',
    start_time       DATETIME     NOT NULL COMMENT '开始时间',
    end_time         DATETIME     NOT NULL COMMENT '结束时间',
    source           VARCHAR(16)  NOT NULL DEFAULT 'manual' COMMENT 'fee同步/manual手工',
    fee_id           BIGINT       NULL COMMENT '来源缴费ID',
    fee_schedule_id  BIGINT       NULL COMMENT '来源缴费排课模板ID',
    edited_flag      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '1用户已编辑,同步不覆盖',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0正常 2删除',
    remark           VARCHAR(500) NULL,
    create_by        VARCHAR(64)  NULL,
    create_time      DATETIME     NULL,
    update_by        VARCHAR(64)  NULL,
    update_time      DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_schedule_teacher_time (teacher_user_id, start_time),
    KEY idx_schedule_time (start_time, end_time),
    KEY idx_schedule_fee_tpl (fee_schedule_id, start_time),
    KEY idx_schedule_fee (fee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课场次';

CREATE TABLE IF NOT EXISTS cf_schedule_student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id  BIGINT       NOT NULL COMMENT '场次ID',
    student_id  BIGINT       NOT NULL COMMENT '学生ID',
    create_by   VARCHAR(64)  NULL,
    create_time DATETIME     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_session_student (session_id, student_id),
    KEY idx_schedule_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课学员';

-- 菜单：与缴费管理同级
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2115, '老师排课', p.parent_id, IFNULL(p.order_num, 0) + 4, 'scheduleTeacher', 'face/schedule/teacher', '', '', 1, 0, 'C', '0', '0', 'face:schedule:list', 'date', 'admin', NOW(), '老师日历排课'
FROM sys_menu p WHERE p.menu_id = 2100 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2115);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2116, '学生课表', p.parent_id, IFNULL(p.order_num, 0) + 5, 'scheduleStudent', 'face/schedule/student', '', '', 1, 0, 'C', '0', '0', 'face:schedule:list', 'education', 'admin', NOW(), '按学生查看课表'
FROM sys_menu p WHERE p.menu_id = 2100 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2116);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2117, '排课查询', 2115, 1, '#', '', 'face:schedule:query', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2117);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2118, '排课新增', 2115, 2, '#', '', 'face:schedule:add', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2118);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2119, '排课修改', 2115, 3, '#', '', 'face:schedule:edit', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2119);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2120, '排课删除', 2115, 4, '#', '', 'face:schedule:remove', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2120);

-- 授权：已有缴费(2100)/消课(2106)/教师角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, mid
FROM sys_role_menu rm
CROSS JOIN (
    SELECT 2115 AS mid UNION SELECT 2116 UNION SELECT 2117 UNION SELECT 2118 UNION SELECT 2119 UNION SELECT 2120
) t
WHERE rm.menu_id IN (2100, 2106)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = t.mid);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, mid
FROM sys_role r
CROSS JOIN (
    SELECT 2115 AS mid UNION SELECT 2116 UNION SELECT 2117 UNION SELECT 2118 UNION SELECT 2119 UNION SELECT 2120
) t
WHERE r.role_key = 'teacher' AND r.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = r.role_id AND x.menu_id = t.mid);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, mid FROM (
    SELECT 2115 AS mid UNION SELECT 2116 UNION SELECT 2117 UNION SELECT 2118 UNION SELECT 2119 UNION SELECT 2120
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = t.mid);
