-- -*- coding: utf-8 -*-
-- 课堂点评

CREATE TABLE IF NOT EXISTS cf_class_review_session (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    teacher_user_id  BIGINT       NOT NULL COMMENT '任课老师',
    subject          VARCHAR(32)  NOT NULL COMMENT '科目',
    class_time       DATETIME     NULL COMMENT '上课时间',
    class_content    VARCHAR(500) NULL COMMENT '上课内容',
    class_images     VARCHAR(2000) NULL COMMENT '课堂图片JSON',
    status           CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0草稿 1已发送',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0',
    remark           VARCHAR(255) NULL,
    create_by        VARCHAR(64)  NULL,
    create_time      DATETIME     NULL,
    update_by        VARCHAR(64)  NULL,
    update_time      DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_review_session_teacher (teacher_user_id),
    KEY idx_review_session_time (class_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课堂点评批次';

CREATE TABLE IF NOT EXISTS cf_class_review_item (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id           BIGINT       NOT NULL COMMENT '批次ID',
    student_id           BIGINT       NOT NULL COMMENT '学生ID',
    fee_id               BIGINT       NULL COMMENT '缴费ID',
    consume_id           BIGINT       NULL COMMENT '消课流水ID',
    discipline_score     TINYINT      NOT NULL DEFAULT 5 COMMENT '纪律专注1-5',
    participation_score  TINYINT      NOT NULL DEFAULT 5 COMMENT '活跃参与1-5',
    content              VARCHAR(1000) NULL COMMENT '学员评语',
    images               VARCHAR(2000) NULL COMMENT '图片JSON',
    push_flag            CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0未推送 1已推送',
    create_by            VARCHAR(64)  NULL,
    create_time          DATETIME     NULL,
    update_by            VARCHAR(64)  NULL,
    update_time          DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_review_item_session (session_id),
    KEY idx_review_item_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课堂点评明细';

-- 菜单：与批量消课同级，面向老师
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2111, '课堂点评', p.parent_id, IFNULL(p.order_num, 0) + 3, 'classReview', 'face/review/index', '', '', 1, 0, 'C', '0', '0', 'face:review:list', 'message', 'admin', NOW(), '消课后课堂点评'
FROM sys_menu p WHERE p.menu_id = 2106 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2111);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2112, '点评编辑', 2111, 1, '#', '', 'face:review:edit', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2112);

-- 若已存在 C 类型编辑菜单，改为按钮权限（编辑页走前端 dynamicRoutes）
UPDATE sys_menu SET menu_type = 'F', path = '#', component = '', visible = '0'
WHERE menu_id = 2112 AND menu_type = 'C';

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2113, '发送点评', 2111, 2, '#', '', 'face:review:send', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2113);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2114, '点评查询', 2111, 3, '#', '', 'face:review:query', 'F', '0', '0', 'admin', NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2114);

-- 授权：已有批量消课(2106)或缴费管理(2100)的角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2111 FROM sys_role_menu rm
WHERE rm.menu_id IN (2100, 2106) AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = 2111);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, mid FROM sys_role_menu rm
CROSS JOIN (SELECT 2112 AS mid UNION SELECT 2113 UNION SELECT 2114) t
WHERE rm.menu_id IN (2100, 2106)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = t.mid);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, mid FROM (SELECT 2111 AS mid UNION SELECT 2112 UNION SELECT 2113 UNION SELECT 2114) t
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = t.mid);
