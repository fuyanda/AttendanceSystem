-- 学生绑定码：家长输入相同绑定码即可关联
ALTER TABLE cf_student
    ADD COLUMN bind_code VARCHAR(16) NULL COMMENT '家长绑定码' AFTER school;

UPDATE cf_student
SET bind_code = UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 6))
WHERE bind_code IS NULL OR bind_code = '';

ALTER TABLE cf_student
    MODIFY COLUMN bind_code VARCHAR(16) NOT NULL COMMENT '家长绑定码';

CREATE UNIQUE INDEX uk_cf_student_bind_code ON cf_student (bind_code);
