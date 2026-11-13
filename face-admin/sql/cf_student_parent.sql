-- 学生-家长多对多关联表（方案B）
CREATE TABLE IF NOT EXISTS cf_student_parent (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_id      BIGINT       NOT NULL COMMENT '学生ID',
    user_id         BIGINT       NOT NULL COMMENT '家长用户ID(cf_user.id)',
    relation_type   VARCHAR(20)  DEFAULT 'parent' COMMENT '关系类型：father/mother/grandpa/other/parent',
    is_primary      TINYINT(1)   DEFAULT 0 COMMENT '是否主联系人(同步parent_tel)',
    notify_enabled  TINYINT(1)   DEFAULT 1 COMMENT '是否接收签到签退通知',
    create_by       VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_user (student_id, user_id),
    KEY idx_user_id (user_id),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生-家长关联表';

-- 历史数据迁移：将 parent_tel 已有 cf_user 的记录写入关联表
INSERT INTO cf_student_parent (student_id, user_id, relation_type, is_primary, notify_enabled, create_time)
SELECT cs.id, cu.id, 'parent', 1, 1, NOW()
FROM cf_student cs
INNER JOIN cf_user cu ON cs.parent_tel = cu.phone AND cu.del_flag = '0'
WHERE cs.parent_tel IS NOT NULL
  AND cs.parent_tel != ''
  AND NOT EXISTS (
      SELECT 1 FROM cf_student_parent csp
      WHERE csp.student_id = cs.id AND csp.user_id = cu.id
  );
