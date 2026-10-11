-- ============================================================
-- 班级统计模块 · 补充 SQL（手动执行）
-- ============================================================

-- ============================================================
-- 一、给辅导员角色分配统计权限
-- 辅导员(role_id=100)需要能查统计概览和导出
-- ============================================================
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(100, 2600),   -- 班级统计菜单
(100, 2601),   -- 统计查询
(100, 2602);   -- 统计导出

-- ============================================================
-- 二、测试账号（如需自测，执行以下 SQL）
-- 密码哈希同 admin 账号，密码为 admin123
-- 包含 sys_user_role 角色关联，否则 studentCount 统计不准
-- ============================================================

-- 1. 创建两个班级部门（挂在 101 深圳总公司下，按需调整 parent_id）
INSERT INTO sys_dept (dept_id, dept_name, parent_id, order_num, leader, phone, email, status, del_flag, create_by, create_time)
VALUES
(200, '计科1班', 101, 10, '', '', '', '0', '0', 'admin', sysdate()),
(201, '计科2班', 101, 11, '', '', '', '0', '0', 'admin', sysdate());

-- 2. 创建账号：1班委 + 2学生 + 1辅导员
INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
VALUES
(1001, 200, 'committee01', '张班委',   '', '', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate()),
(1002, 200, 'student01',   '李学生',   '', '', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate()),
(1003, 201, 'student02',   '王同学',   '', '', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate()),
(1004, 101, 'counselor01', '赵辅导员', '', '', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', sysdate());

-- 3. 角色关联（必须有！学生不挂 assess_student 角色则 studentCount 统计为 0）
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1001, 102),   -- 班委 → assess_committee
(1002, 101),   -- 学生1 → assess_student
(1003, 101),   -- 学生2 → assess_student
(1004, 100);   -- 辅导员 → assess_counselor

-- 4. 造测试申报单（2个不同班学生各1单）
INSERT INTO assess_declaration (declaration_id, declaration_no, regulation_id, user_id, student_name, class_name, academic_year, semester, total_score, status, submit_time, create_by, create_time)
VALUES
(1001, 'SB202610110001', 1, 1002, '李学生', '计科1班', '2025-2026', '1', 85.50, '1', sysdate(), 'student01', sysdate()),
(1002, 'SB202610110002', 1, 1003, '王同学', '计科2班', '2025-2026', '1', 92.00, '4', sysdate(), 'student02', sysdate());

-- ============================================================
-- 三、清理测试数据（如需删除）
-- ============================================================
-- DELETE FROM assess_declaration WHERE declaration_id IN (1001, 1002);
-- DELETE FROM sys_user_role WHERE user_id IN (1001, 1002, 1003, 1004);
-- DELETE FROM sys_user WHERE user_id IN (1001, 1002, 1003, 1004);
-- DELETE FROM sys_dept WHERE dept_id IN (200, 201);
