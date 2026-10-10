-- ============================================================
-- 综测申报管理模块 — 菜单与权限初始化SQL
-- 包含：申报单(assess:declaration:*) + 规则配置(assess:rule:*)
-- 申报单菜单ID沿用 2200-2207（assess_init.sql已定义）
-- 规则配置菜单ID 2210-2216
-- ============================================================

-- --------------------------------------------------------
-- 1. 规则引擎配置菜单（父菜单ID 2210）
-- --------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(2210, '规则引擎配置', 2200, 2, 'rule', 'assess/rule/index', 1, 0, 'C', '0', '0', 'assess:rule:list', 'setting', 'admin', sysdate(), '规则引擎配置菜单');

-- 按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(2211, '规则查询', 2210, 1, '#', '', 1, 0, 'F', '0', '0', 'assess:rule:query', '#', 'admin', sysdate(), ''),
(2212, '规则新增', 2210, 2, '#', '', 1, 0, 'F', '0', '0', 'assess:rule:add', '#', 'admin', sysdate(), ''),
(2213, '规则修改', 2210, 3, '#', '', 1, 0, 'F', '0', '0', 'assess:rule:edit', '#', 'admin', sysdate(), ''),
(2214, '规则删除', 2210, 4, '#', '', 1, 0, 'F', '0', '0', 'assess:rule:remove', '#', 'admin', sysdate(), ''),
(2215, '规则导出', 2210, 5, '#', '', 1, 0, 'F', '0', '0', 'assess:rule:export', '#', 'admin', sysdate(), '');

-- --------------------------------------------------------
-- 2. 申报单补充权限：提交按钮（挂在申报单菜单2200下）
--    assess_init.sql已定义 list/query/add/edit/remove/export
--    这里补充 submit 权限（使用已有edit权限即可，不单独建按钮）
--    如需独立权限，取消下面注释
-- --------------------------------------------------------
-- INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
-- VALUES (2208, '申报提交', 2200, 7, '#', '', 1, 0, 'F', '0', '0', 'assess:declaration:submit', '#', 'admin', sysdate(), '');

-- --------------------------------------------------------
-- 3. 角色权限分配
-- --------------------------------------------------------

-- 3.1 管理员角色（role_id=1）— 自动拥有所有权限，无需手动分配

-- 3.2 辅导员/审核角色（role_id=100）— 可查看申报单 + 管理规则配置
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(100, 2210), (100, 2211), (100, 2212), (100, 2213), (100, 2214), (100, 2215);

-- 3.3 班委角色（role_id=101）— 可查看申报单（已在assess_init.sql分配）
--     班委不能管理规则配置，无需分配2210-2215

-- 3.4 学生角色（role_id=102）— 可创建/查看/提交自己的申报单，不能管理规则
--     申报单权限已在assess_init.sql分配，此处不重复

-- --------------------------------------------------------
-- 4. 验证SQL（执行后检查）
-- --------------------------------------------------------
-- SELECT menu_id, menu_name, perms FROM sys_menu WHERE menu_id BETWEEN 2210 AND 2215 ORDER BY menu_id;
-- SELECT r.role_name, m.menu_name, m.perms
--   FROM sys_role_menu rm
--   JOIN sys_role r ON rm.role_id = r.role_id
--   JOIN sys_menu m ON rm.menu_id = m.menu_id
--  WHERE m.menu_id BETWEEN 2210 AND 2215;
