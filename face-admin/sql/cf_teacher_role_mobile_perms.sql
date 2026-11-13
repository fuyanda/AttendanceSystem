-- -*- coding: utf-8 -*-
-- 教师角色(role_key=teacher) — face-teacher 手机端完整权限
-- 岗位(学校负责人/校区负责人/教师)不决定接口权限，统一由本角色菜单控制。
-- 执行后相关账号必须重新登录。

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN (
    -- 学生管理（列表/查/增/改/删）
    SELECT 2013 AS menu_id UNION ALL
    SELECT 2014 UNION ALL
    SELECT 2015 UNION ALL
    SELECT 2016 UNION ALL
    SELECT 2017 UNION ALL
    -- 签到（列表/查/增，含人脸识别与重推）
    SELECT 2031 UNION ALL
    SELECT 2032 UNION ALL
    SELECT 2033 UNION ALL
    -- 签退（列表/查/增，含手动签退）
    SELECT 2037 UNION ALL
    SELECT 2038 UNION ALL
    SELECT 2039 UNION ALL
    -- 打卡设置（列表/查/增/改/删）
    SELECT 2005 UNION ALL
    SELECT 2000 UNION ALL
    SELECT 2001 UNION ALL
    SELECT 2002 UNION ALL
    SELECT 2003
) m
WHERE r.role_key = 'teacher'
  AND r.del_flag = '0'
  AND EXISTS (SELECT 1 FROM sys_menu x WHERE x.menu_id = m.menu_id)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- 系统管理目录（菜单树父节点）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, 1
FROM sys_role r
WHERE r.role_key = 'teacher'
  AND r.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = 1
  );
