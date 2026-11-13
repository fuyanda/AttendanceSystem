-- -*- coding: utf-8 -*-
-- 学生缴费 / 排课 / 消课

CREATE TABLE IF NOT EXISTS cf_student_fee (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id      BIGINT          NOT NULL COMMENT '学生ID',
    subject         VARCHAR(32)     NOT NULL COMMENT '缴费科目(字典cf_fee_subject)',
    total_hours     INT             NOT NULL DEFAULT 0 COMMENT '购买课时',
    used_hours      INT             NOT NULL DEFAULT 0 COMMENT '已消课时',
    total_amount    DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '课时费总额',
    teacher_user_id BIGINT          NULL COMMENT '任课老师(sys_user.user_id)',
    status          CHAR(1)         NOT NULL DEFAULT '0' COMMENT '0有效 1关闭',
    del_flag        CHAR(1)         NOT NULL DEFAULT '0' COMMENT '0正常 2删除',
    remark          VARCHAR(500)    NULL COMMENT '备注',
    create_by       VARCHAR(64)     NULL,
    create_time     DATETIME        NULL,
    update_by       VARCHAR(64)     NULL,
    update_time     DATETIME        NULL,
    PRIMARY KEY (id),
    KEY idx_cf_student_fee_student (student_id),
    KEY idx_cf_student_fee_teacher (teacher_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生缴费课时';

CREATE TABLE IF NOT EXISTS cf_student_fee_schedule (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    fee_id      BIGINT       NOT NULL COMMENT '缴费ID',
    week_day    TINYINT      NOT NULL COMMENT '星期1-7(周一至周日)',
    start_time  VARCHAR(8)   NOT NULL COMMENT '开始时间 HH:mm',
    end_time    VARCHAR(8)   NOT NULL COMMENT '结束时间 HH:mm',
    PRIMARY KEY (id),
    KEY idx_cf_fee_schedule_fee (fee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费排课';

CREATE TABLE IF NOT EXISTS cf_student_fee_consume (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    fee_id        BIGINT       NOT NULL COMMENT '缴费ID',
    hours         INT          NOT NULL DEFAULT 1 COMMENT '本次消课',
    source        VARCHAR(16)  NOT NULL COMMENT 'manual/checkin',
    checkin_id    BIGINT       NULL COMMENT '签到记录ID',
    consume_time  DATETIME     NULL COMMENT '消课时间',
    remark        VARCHAR(255) NULL COMMENT '备注',
    create_by     VARCHAR(64)  NULL,
    create_time   DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_cf_fee_consume_fee (fee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消课流水';

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
SELECT 200, '缴费科目', 'cf_fee_subject', '0', 'admin', NOW(), '学生缴费科目'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'cf_fee_subject');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2001, 1, '语文', 'chinese', 'cf_fee_subject', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'cf_fee_subject' AND dict_value = 'chinese');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2002, 2, '数学', 'math', 'cf_fee_subject', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'cf_fee_subject' AND dict_value = 'math');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2003, 3, '英语', 'english', 'cf_fee_subject', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'cf_fee_subject' AND dict_value = 'english');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2004, 4, '地理', 'geography', 'cf_fee_subject', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'cf_fee_subject' AND dict_value = 'geography');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2005, 5, '生物', 'biology', 'cf_fee_subject', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'cf_fee_subject' AND dict_value = 'biology');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2006, 6, '化学', 'chemistry', 'cf_fee_subject', '', 'default', 'N', '0', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'cf_fee_subject' AND dict_value = 'chemistry');

-- 菜单：parent_id 请改为人脸模块父菜单 ID（若需要）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2100, '缴费管理', 0, 6, 'fee', 'face/fee/index', '', '', 1, 0, 'C', '0', '0', 'face:fee:list', 'money', 'admin', NOW(), '学生缴费课时管理'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2100);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2101, '缴费查询', 2100, 1, '#', '', 'face:fee:query', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2101);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2102, '缴费新增', 2100, 2, '#', '', 'face:fee:add', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2102);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2103, '缴费修改', 2100, 3, '#', '', 'face:fee:edit', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2103);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2104, '缴费删除', 2100, 4, '#', '', 'face:fee:remove', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2104);

-- 批量消课：与缴费管理同级
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2106, '批量消课', p.parent_id, IFNULL(p.order_num, 0) + 1, 'feeConsume', 'face/fee/consume/index', '', '', 1, 0, 'C', '0', '0', 'face:fee:consume', 'edit', 'admin', NOW(), '老师批量消课'
FROM sys_menu p
WHERE p.menu_id = 2100
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2106);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2105, '手动消课', 2106, 1, '#', '', 'face:fee:consume', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2105);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2107, '代课消课', 2106, 2, '#', '', 'face:fee:consume:assign', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2107);

-- 中道退费（详见 sql/cf_student_fee_refund.sql）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, create_by, create_time)
SELECT 2108, '中道退费', 2100, 5, '#', '', 'face:fee:refund', 'F', '0', '0', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2108);

-- 缴费台账（详见 sql/cf_student_fee_ledger.sql）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2109, '缴费台账', p.parent_id, IFNULL(p.order_num, 0) + 2, 'feeLedger', 'face/fee/ledger/index', '', '', 1, 0, 'C', '0', '0', 'face:fee:ledger', 'chart', 'admin', NOW(), '缴费消课退费台账统计'
FROM sys_menu p WHERE p.menu_id = 2100 AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2109);
