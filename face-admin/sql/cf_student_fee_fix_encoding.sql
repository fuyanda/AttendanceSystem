-- -*- coding: utf-8 -*-
-- 修复 cf_fee_subject 字典与缴费菜单中文乱码（若已执行过错误编码的 SQL）

UPDATE sys_dict_type SET dict_name = '缴费科目', remark = '学生缴费科目'
WHERE dict_type = 'cf_fee_subject';

UPDATE sys_dict_data SET dict_label = '语文' WHERE dict_type = 'cf_fee_subject' AND dict_value = 'chinese';
UPDATE sys_dict_data SET dict_label = '数学' WHERE dict_type = 'cf_fee_subject' AND dict_value = 'math';
UPDATE sys_dict_data SET dict_label = '英语' WHERE dict_type = 'cf_fee_subject' AND dict_value = 'english';
UPDATE sys_dict_data SET dict_label = '地理' WHERE dict_type = 'cf_fee_subject' AND dict_value = 'geography';
UPDATE sys_dict_data SET dict_label = '生物' WHERE dict_type = 'cf_fee_subject' AND dict_value = 'biology';
UPDATE sys_dict_data SET dict_label = '化学' WHERE dict_type = 'cf_fee_subject' AND dict_value = 'chemistry';

UPDATE sys_menu SET menu_name = '缴费管理', remark = '学生缴费课时管理' WHERE menu_id = 2100;
UPDATE sys_menu SET menu_name = '缴费查询' WHERE menu_id = 2101;
UPDATE sys_menu SET menu_name = '缴费新增' WHERE menu_id = 2102;
UPDATE sys_menu SET menu_name = '缴费修改' WHERE menu_id = 2103;
UPDATE sys_menu SET menu_name = '缴费删除' WHERE menu_id = 2104;
UPDATE sys_menu SET menu_name = '手动消课' WHERE menu_id = 2105;
UPDATE sys_menu SET menu_name = '批量消课', remark = '老师批量消课' WHERE menu_id = 2106;
UPDATE sys_menu SET menu_name = '代课消课' WHERE menu_id = 2107;
