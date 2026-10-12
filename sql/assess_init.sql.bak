-- ============================================================
-- 综测智办系统 初始化脚本
-- 包含：8张业务表 DDL + 菜单/权限 + 角色 + 角色权限映射
-- 依赖：若依 RuoYi 基础表(sys_menu/sys_role/sys_role_menu)已存在
-- 字符集：utf8mb4  引擎：InnoDB
-- ============================================================

-- ============================================================
-- 一、业务表 DDL（8张）
-- ============================================================

-- ---------- 1. 细则主表（百级） ----------
DROP TABLE IF EXISTS assess_regulation;
CREATE TABLE assess_regulation (
    regulation_id    BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '细则ID',
    regulation_no    VARCHAR(50)  NOT NULL                 COMMENT '细则编号',
    title            VARCHAR(200) NOT NULL                 COMMENT '细则标题',
    publish_date     DATE                  DEFAULT NULL    COMMENT '发布日期',
    effective_date   DATE                  DEFAULT NULL    COMMENT '生效日期',
    expire_date      DATE                  DEFAULT NULL    COMMENT '失效日期',
    status           CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '状态(0草案 1已发布 2已废止)',
    version_num      INT          NOT NULL DEFAULT 1       COMMENT '版本号',
    content          TEXT                   DEFAULT NULL    COMMENT '细则内容(富文本)',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    remark           VARCHAR(500)          DEFAULT NULL     COMMENT '备注',
    PRIMARY KEY (regulation_id),
    UNIQUE KEY uk_regulation_no (regulation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测细则主表';

-- ---------- 2. 条款表（千级，支持层级树） ----------
DROP TABLE IF EXISTS assess_regulation_clause;
CREATE TABLE assess_regulation_clause (
    clause_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '条款ID',
    regulation_id    BIGINT       NOT NULL                 COMMENT '所属细则ID',
    parent_id        BIGINT       NOT NULL DEFAULT 0       COMMENT '父条款ID(0=顶级条款)',
    clause_no        VARCHAR(50)  NOT NULL                 COMMENT '条款编号',
    clause_title     VARCHAR(200) NOT NULL                 COMMENT '条款标题',
    clause_text      TEXT                   DEFAULT NULL    COMMENT '条款原文',
    clause_type      CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '条款类型(0基础项 1加分项 2扣分项)',
    max_score        DECIMAL(5,2)          DEFAULT NULL    COMMENT '最高分值',
    sort_order       INT          NOT NULL DEFAULT 0       COMMENT '排序号',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    remark           VARCHAR(500)          DEFAULT NULL     COMMENT '备注',
    PRIMARY KEY (clause_id),
    KEY idx_regulation_id (regulation_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测条款表';

-- ---------- 3. 材料主表（万级，含OCR/抽取状态） ----------
DROP TABLE IF EXISTS assess_material;
CREATE TABLE assess_material (
    material_id      BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '材料ID',
    user_id          BIGINT       NOT NULL                 COMMENT '上传学生用户ID',
    student_no       VARCHAR(30)  NOT NULL                 COMMENT '学号',
    student_name     VARCHAR(50)  NOT NULL                 COMMENT '学生姓名',
    class_name       VARCHAR(50)           DEFAULT NULL    COMMENT '班级名称',
    dept_id          BIGINT       NOT NULL                 COMMENT '班级部门ID(数据隔离)',
    material_name    VARCHAR(200) NOT NULL                 COMMENT '材料名称',
    material_type    VARCHAR(50)           DEFAULT NULL    COMMENT '材料类型(证书/证明/报告等)',
    file_url         VARCHAR(500) NOT NULL                 COMMENT '文件存储URL',
    file_type        VARCHAR(10)  NOT NULL                 COMMENT '文件类型(image/pdf)',
    file_size        BIGINT                DEFAULT 0       COMMENT '文件大小(字节)',
    ocr_status       CHAR(1)      NOT NULL DEFAULT '0'     COMMENT 'OCR状态(0未识别 1识别中 2已识别 3失败)',
    ocr_text         TEXT                   DEFAULT NULL    COMMENT 'OCR识别全文',
    extract_status   CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '抽取状态(0未抽取 1抽取中 2已抽取 3失败)',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    remark           VARCHAR(500)          DEFAULT NULL     COMMENT '备注',
    PRIMARY KEY (material_id),
    KEY idx_user_id (user_id),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测材料主表';

-- ---------- 4. 材料结构化字段KV表（十万级） ----------
DROP TABLE IF EXISTS assess_material_field;
CREATE TABLE assess_material_field (
    field_id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '字段ID',
    material_id      BIGINT       NOT NULL                 COMMENT '所属材料ID',
    field_key        VARCHAR(100) NOT NULL                 COMMENT '字段名',
    field_value      TEXT                   DEFAULT NULL    COMMENT '字段值',
    field_type       VARCHAR(50)           DEFAULT NULL    COMMENT '字段类型(string/number/date等)',
    confidence       DECIMAL(5,4)          DEFAULT NULL    COMMENT '抽取置信度',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    PRIMARY KEY (field_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料结构化字段KV表';

-- ---------- 5. 申报单主表（万级） ----------
DROP TABLE IF EXISTS assess_declaration;
CREATE TABLE assess_declaration (
    declaration_id   BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '申报单ID',
    declaration_no   VARCHAR(50)  NOT NULL                 COMMENT '申报单号',
    regulation_id    BIGINT       NOT NULL                 COMMENT '依据细则ID',
    user_id          BIGINT       NOT NULL                 COMMENT '申报学生用户ID',
    student_name     VARCHAR(50)  NOT NULL                 COMMENT '学生姓名',
    class_name       VARCHAR(50)           DEFAULT NULL    COMMENT '班级名称',
    academic_year    VARCHAR(20)  NOT NULL                 COMMENT '学年(如2025-2026)',
    semester         CHAR(1)      NOT NULL                 COMMENT '学期(1上学期 2下学期)',
    total_score      DECIMAL(6,2)          DEFAULT 0.00    COMMENT '总分',
    status           CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '状态(0草稿 1已提交 2审核中 3已通过 4已驳回)',
    submit_time      DATETIME              DEFAULT NULL     COMMENT '提交时间',
    review_user_id   BIGINT                DEFAULT NULL    COMMENT '审核人用户ID',
    review_time      DATETIME              DEFAULT NULL     COMMENT '审核时间',
    review_color     CHAR(1)               DEFAULT NULL    COMMENT '审核颜色(1绿2黄3红)',
    review_comment   VARCHAR(500)          DEFAULT NULL    COMMENT '审核意见',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    remark           VARCHAR(500)          DEFAULT NULL     COMMENT '备注',
    PRIMARY KEY (declaration_id),
    UNIQUE KEY uk_declaration_no (declaration_no),
    KEY idx_user_id (user_id),
    KEY idx_regulation_id (regulation_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测申报单主表';

-- ---------- 6. 申报明细表（十万级）—— 核心表 ----------
DROP TABLE IF EXISTS assess_declaration_item;
CREATE TABLE assess_declaration_item (
    item_id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    declaration_id       BIGINT       NOT NULL                 COMMENT '申报单ID',
    material_id          BIGINT                DEFAULT NULL    COMMENT '关联材料ID',
    clause_id            BIGINT       NOT NULL                 COMMENT '关联条款ID',
    suggested_score      DECIMAL(5,2)          DEFAULT NULL    COMMENT 'AI建议分值(不入库,仅参考)',
    rule_adjusted_score  DECIMAL(5,2)          DEFAULT NULL    COMMENT '规则引擎调整后分值',
    final_score          DECIMAL(5,2)          DEFAULT NULL    COMMENT '最终入库分值',
    evidence_clause_no   VARCHAR(50)           DEFAULT NULL    COMMENT '证据-条款编号',
    evidence_clause_text TEXT                  DEFAULT NULL    COMMENT '证据-条款原文',
    evidence_confidence  DECIMAL(5,4)          DEFAULT NULL    COMMENT '证据-匹配置信度',
    match_reason         VARCHAR(500)          DEFAULT NULL    COMMENT '匹配原因说明',
    match_status         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '匹配状态(0待匹配 1已匹配 2未匹配 3存疑)',
    rule_check_result    TEXT                  DEFAULT NULL    COMMENT '规则校验结果(JSON:封顶/互斥/时间窗)',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    PRIMARY KEY (item_id),
    KEY idx_declaration_id (declaration_id),
    KEY idx_material_id (material_id),
    KEY idx_clause_id (clause_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测申报明细表';

-- ---------- 7. 审核记录表（万级） ----------
DROP TABLE IF EXISTS assess_review;
CREATE TABLE assess_review (
    review_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '审核记录ID',
    declaration_id   BIGINT       NOT NULL                 COMMENT '申报单ID',
    reviewer_id      BIGINT       NOT NULL                 COMMENT '审核人用户ID',
    reviewer_name    VARCHAR(50)  NOT NULL                 COMMENT '审核人姓名',
    review_round     INT          NOT NULL DEFAULT 1       COMMENT '审核轮次',
    review_result    CHAR(1)      NOT NULL                 COMMENT '审核结果(1通过 2驳回 3退回修改)',
    review_score     DECIMAL(6,2)          DEFAULT NULL    COMMENT '审核评分',
    review_comment   VARCHAR(1000)         DEFAULT NULL    COMMENT '审核意见',
    review_time      DATETIME              DEFAULT NULL     COMMENT '审核时间',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    PRIMARY KEY (review_id),
    KEY idx_declaration_id (declaration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测审核记录表';

-- ---------- 8. 规则引擎配置表（百级） ----------
DROP TABLE IF EXISTS assess_rule_config;
CREATE TABLE assess_rule_config (
    rule_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '规则ID',
    regulation_id    BIGINT       NOT NULL                 COMMENT '所属细则ID',
    rule_name        VARCHAR(100) NOT NULL                 COMMENT '规则名称',
    rule_type        VARCHAR(50)  NOT NULL                 COMMENT '规则类型(cap封顶 exclude互斥 time_window时间窗)',
    rule_desc        VARCHAR(500)          DEFAULT NULL    COMMENT '规则描述',
    rule_condition   TEXT                  DEFAULT NULL    COMMENT '规则条件(JSON)',
    rule_action      TEXT                  DEFAULT NULL    COMMENT '规则动作(JSON)',
    priority         INT          NOT NULL DEFAULT 0       COMMENT '优先级(数值越小越先执行)',
    status           CHAR(1)      NOT NULL DEFAULT '1'     COMMENT '状态(0禁用 1启用)',
    create_by        VARCHAR(64)           DEFAULT ''       COMMENT '创建者',
    create_time      DATETIME              DEFAULT NULL     COMMENT '创建时间',
    update_by        VARCHAR(64)           DEFAULT ''       COMMENT '更新者',
    update_time      DATETIME              DEFAULT NULL     COMMENT '更新时间',
    del_flag         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志(0存在 2删除)',
    remark           VARCHAR(500)          DEFAULT NULL     COMMENT '备注',
    PRIMARY KEY (rule_id),
    KEY idx_regulation_id (regulation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测规则引擎配置表';


-- ============================================================
-- 二、菜单与权限（sys_menu）
-- 结构：1个目录(M) + 7个菜单(C) + 32个按钮(F) = 40条
-- AI匹配模块为内部调用,不挂菜单,不分配角色
-- menu_type: M=目录 C=菜单 F=按钮
-- ============================================================

-- ---- 父目录：综测智办 ----
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2000, '综测智办', 0, 10, 'assess', NULL, 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '综测智办目录');

-- ============ 细则管理（辅导员） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (1000, '细则管理', 2000, 1, 'regulation', 'assess/regulation/index', 1, 0, 'C', '0', '0', 'assess:regulation:list', 'documentation', 'admin', sysdate(), '细则菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(1001, '细则查询', 1000, 1, 1, 0, 'F', '0', '0', 'assess:regulation:query',  '#', 'admin', sysdate(), ''),
(1002, '细则新增', 1000, 2, 1, 0, 'F', '0', '0', 'assess:regulation:add',    '#', 'admin', sysdate(), ''),
(1003, '细则修改', 1000, 3, 1, 0, 'F', '0', '0', 'assess:regulation:edit',   '#', 'admin', sysdate(), ''),
(1004, '细则删除', 1000, 4, 1, 0, 'F', '0', '0', 'assess:regulation:remove', '#', 'admin', sysdate(), ''),
(1005, '细则导出', 1000, 5, 1, 0, 'F', '0', '0', 'assess:regulation:export', '#', 'admin', sysdate(), '');

-- ============ 条款管理（辅导员） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (1100, '条款管理', 2000, 2, 'clause', 'assess/clause/index', 1, 0, 'C', '0', '0', 'assess:clause:list', 'tree-table', 'admin', sysdate(), '条款菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(1101, '条款查询', 1100, 1, 1, 0, 'F', '0', '0', 'assess:clause:query',  '#', 'admin', sysdate(), ''),
(1102, '条款新增', 1100, 2, 1, 0, 'F', '0', '0', 'assess:clause:add',    '#', 'admin', sysdate(), ''),
(1103, '条款修改', 1100, 3, 1, 0, 'F', '0', '0', 'assess:clause:edit',   '#', 'admin', sysdate(), ''),
(1104, '条款删除', 1100, 4, 1, 0, 'F', '0', '0', 'assess:clause:remove', '#', 'admin', sysdate(), ''),
(1105, '条款导出', 1100, 5, 1, 0, 'F', '0', '0', 'assess:clause:export', '#', 'admin', sysdate(), '');

-- ============ 材料管理（学生） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (1200, '材料管理', 2000, 3, 'material', 'assess/material/index', 1, 0, 'C', '0', '0', 'assess:material:list', 'upload', 'admin', sysdate(), '材料菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(1201, '材料查询', 1200, 1, 1, 0, 'F', '0', '0', 'assess:material:query',     '#', 'admin', sysdate(), ''),
(1202, '材料上传', 1200, 2, 1, 0, 'F', '0', '0', 'assess:material:upload',    '#', 'admin', sysdate(), ''),
(1203, '材料修改', 1200, 3, 1, 0, 'F', '0', '0', 'assess:material:edit',      '#', 'admin', sysdate(), ''),
(1204, '材料删除', 1200, 4, 1, 0, 'F', '0', '0', 'assess:material:remove',    '#', 'admin', sysdate(), ''),
(1205, '材料导出', 1200, 5, 1, 0, 'F', '0', '0', 'assess:material:export',    '#', 'admin', sysdate(), ''),
(1206, '重新OCR', 1200, 6, 1, 0, 'F', '0', '0', 'assess:material:reprocess',  '#', 'admin', sysdate(), '');

-- ============ 申报管理（学生+班委只读） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2200, '申报管理', 2000, 4, 'declaration', 'assess/declaration/index', 1, 0, 'C', '0', '0', 'assess:declaration:list', 'form', 'admin', sysdate(), '申报菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(2201, '申报查询', 2200, 1, 1, 0, 'F', '0', '0', 'assess:declaration:query',  '#', 'admin', sysdate(), ''),
(2202, '申报列表', 2200, 2, 1, 0, 'F', '0', '0', 'assess:declaration:list',   '#', 'admin', sysdate(), ''),
(2203, '申报新增', 2200, 3, 1, 0, 'F', '0', '0', 'assess:declaration:add',    '#', 'admin', sysdate(), ''),
(2204, '申报修改', 2200, 4, 1, 0, 'F', '0', '0', 'assess:declaration:edit',   '#', 'admin', sysdate(), ''),
(2205, '申报导出', 2200, 5, 1, 0, 'F', '0', '0', 'assess:declaration:export', '#', 'admin', sysdate(), ''),
(2206, '申报删除', 2200, 6, 1, 0, 'F', '0', '0', 'assess:declaration:remove', '#', 'admin', sysdate(), ''),
(2207, '申报提交', 2200, 7, 1, 0, 'F', '0', '0', 'assess:declaration:submit', '#', 'admin', sysdate(), '');

-- ============ 审核管理（辅导员） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2400, '审核管理', 2000, 5, 'review', 'assess/review/index', 1, 0, 'C', '0', '0', 'assess:review:list', 'validCode', 'admin', sysdate(), '审核菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(2401, '审核查询', 2400, 1, 1, 0, 'F', '0', '0', 'assess:review:query',  '#', 'admin', sysdate(), ''),
(2402, '审核列表', 2400, 2, 1, 0, 'F', '0', '0', 'assess:review:list',   '#', 'admin', sysdate(), ''),
(2403, '审核操作', 2400, 3, 1, 0, 'F', '0', '0', 'assess:review:edit',   '#', 'admin', sysdate(), '');

-- ============ 规则引擎（辅导员） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2500, '规则引擎', 2000, 6, 'rule', 'assess/rule/index', 1, 0, 'C', '0', '0', 'assess:rule:list', 'slider', 'admin', sysdate(), '规则引擎菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(2501, '规则查询', 2500, 1, 1, 0, 'F', '0', '0', 'assess:rule:query',  '#', 'admin', sysdate(), ''),
(2502, '规则列表', 2500, 2, 1, 0, 'F', '0', '0', 'assess:rule:list',   '#', 'admin', sysdate(), ''),
(2503, '规则新增', 2500, 3, 1, 0, 'F', '0', '0', 'assess:rule:add',    '#', 'admin', sysdate(), ''),
(2504, '规则修改', 2500, 4, 1, 0, 'F', '0', '0', 'assess:rule:edit',   '#', 'admin', sysdate(), '');

-- ============ 班级统计（班委） ============
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2600, '班级统计', 2000, 7, 'stats', 'assess/stats/index', 1, 0, 'C', '0', '0', 'assess:stats:query', 'chart', 'admin', sysdate(), '班级统计菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(2601, '统计查询', 2600, 1, 1, 0, 'F', '0', '0', 'assess:stats:query',  '#', 'admin', sysdate(), ''),
(2602, '统计导出', 2600, 2, 1, 0, 'F', '0', '0', 'assess:stats:export', '#', 'admin', sysdate(), '');


-- ============================================================
-- 三、角色定义（sys_role）
-- role_id: 100=辅导员 101=学生 102=班委
-- data_scope: 1=全部数据 3=本部门数据 5=仅本人数据
-- ============================================================

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES
(100, '综测辅导员', 'assess_counselor', 10, 1, 1, 1, '0', '0', 'admin', sysdate(), '负责细则上传/条款编辑/审核/规则配置'),
(101, '综测学生',   'assess_student',   11, 5, 1, 1, '0', '0', 'admin', sysdate(), '负责材料上传/申报创建/明细调整'),
(102, '综测班委',   'assess_committee', 12, 3, 1, 1, '0', '0', 'admin', sysdate(), '本班统计数据只读 + 申报查询/列表/导出');


-- ============================================================
-- 四、角色-菜单映射（sys_role_menu）
-- ============================================================

-- ---- 辅导员(100)：细则管理 + 条款管理 + 审核管理 + 规则引擎 ----
-- 目录 + 细则(6) + 条款(6) + 审核(4) + 规则(5) = 22条
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(100, 2000),
(100, 1000), (100, 1001), (100, 1002), (100, 1003), (100, 1004), (100, 1005),
(100, 1100), (100, 1101), (100, 1102), (100, 1103), (100, 1104), (100, 1105),
(100, 2400), (100, 2401), (100, 2402), (100, 2403),
(100, 2500), (100, 2501), (100, 2502), (100, 2503), (100, 2504);

-- ---- 学生(101)：材料管理 + 申报管理(增改查删+提交) ----
-- 目录 + 材料(7: 查询/上传/修改/删除/导出/重新OCR) + 申报(6: 查询/列表/新增/修改/删除/提交) = 15条
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(101, 2000),
(101, 1200), (101, 1201), (101, 1202), (101, 1203), (101, 1204), (101, 1205), (101, 1206),
(101, 2200), (101, 2201), (101, 2202), (101, 2203), (101, 2204), (101, 2206), (101, 2207);

-- ---- 班委(102)：班级统计 + 申报只读(查询/列表/导出) ----
-- 目录 + 统计(3) + 申报(4: 菜单+查询+列表+导出) = 8条
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(102, 2000),
(102, 2600), (102, 2601), (102, 2602),
(102, 2200), (102, 2201), (102, 2202), (102, 2205);
