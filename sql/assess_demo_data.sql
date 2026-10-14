-- ============================================================
-- assess_demo_data.sql
-- 用途：重建 2025-2026 学年综合素质测评细则演示数据
--   1. 废止碎片化细则（reg 2/3）
--   2. 改造 regulation_id=1 为完整学年细则
--   3. 清理旧条款，重建两层条款树（5 模块 + 15 加分项）
--   4. 删除孤儿申报单
--   5. 回填申报明细证据原文
--
-- 执行前务必备份数据库：
--   mysqldump -uroot -proot ry_cloud > backup_before_demo.sql
--
-- 幂等说明：重跑不报错。
--   - 条款树使用显式 clause_id（100~199 段），重跑前先 DELETE 该段再 INSERT
--   - 废止/改造均为 UPDATE，重复执行结果不变
--   - 孤儿申报单删除为 DELETE，重跑时行已不存在，自动跳过
-- ============================================================


-- ============================================================
-- 第 1 步：废止碎片细则
-- reg2「科研成果加分细则」、reg3「社会实践加分细则」已被申报单引用，
-- 只能废止（status='2'），不能删除，以维持引用完整性。
-- ============================================================
UPDATE assess_regulation
   SET status      = '2',
       update_time = NOW()
 WHERE regulation_id IN (2, 3);


-- ============================================================
-- 第 2 步：改造 regulation_id=1 为整份学年细则
-- ============================================================
UPDATE assess_regulation
   SET regulation_no  = 'ZHCP-2025-2026',
       title          = 'XX大学本科生综合素质测评实施细则（2025-2026学年）',
       version_num    = 1,
       status         = '1',
       publish_date   = '2025-09-01',
       effective_date = '2025-09-01',
       expire_date    = '2026-08-31',
       content        = '本办法适用于全日制本科生 2025-2026 学年综合素质测评，包含思想品德、学业成绩、科技创新、文体活动、社会实践五个模块的加分认定标准。',
       update_time    = NOW()
 WHERE regulation_id = 1;


-- ============================================================
-- 第 3 步：清理 reg1 旧条款
-- ============================================================

-- 3a. 未被任何明细引用的旧条款 → 硬删
DELETE FROM assess_regulation_clause
 WHERE regulation_id = 1
   AND clause_id IN (3, 4, 5, 6, 7);

-- 3b. 被明细引用的旧条款 → 软删（保留行以维持外键）
UPDATE assess_regulation_clause
   SET del_flag    = '2',
       update_time = NOW()
 WHERE regulation_id = 1
   AND clause_id IN (1, 2);

-- 3c. reg2/reg3 全部条款软删（其细则已废止）
UPDATE assess_regulation_clause
   SET del_flag    = '2',
       update_time = NOW()
 WHERE regulation_id IN (2, 3);


-- ============================================================
-- 第 4 步：幂等清理本次条款段（clause_id 100~599）
-- 重跑时先清掉上一次插入的条款，再重新 INSERT
-- ============================================================
DELETE FROM assess_regulation_clause
 WHERE regulation_id = 1
   AND clause_id BETWEEN 100 AND 599;


-- ============================================================
-- 第 5 步：插入两层条款树（共 20 行）
-- 模块层：parent_id=0, clause_type='0', max_score=NULL
-- 子项层：parent_id=所属模块 clause_id, clause_type='1'
-- 全部 regulation_id=1, create_by='admin', create_time=NOW(), del_flag='0'
-- ============================================================
INSERT INTO assess_regulation_clause
    (clause_id, regulation_id, parent_id, clause_no, clause_title, clause_text, max_score, clause_type, sort_order, create_by, create_time, del_flag)
VALUES
    -- ===== 一、思想品德素质 =====
    (101, 1, 0,   '1',   '一、思想品德素质',
     '本模块测评学生思想政治表现与社会责任感。',
     NULL, '0', 1, 'admin', NOW(), '0'),

    (111, 1, 101, '1.1', '思想政治理论学习',
     '积极参加学校组织的思想政治理论学习及主题教育活动，全勤且考核合格者，加 2 分。',
     2.00, '1', 101, 'admin', NOW(), '0'),

    (112, 1, 101, '1.2', '志愿服务',
     '参加志愿服务活动，累计服务时长每满 20 小时加 1 分，最高加 3 分，须提供志愿服务证明。',
     3.00, '1', 102, 'admin', NOW(), '0'),

    (113, 1, 101, '1.3', '表彰奖励',
     '获得校级及以上"三好学生""优秀学生干部""优秀共青团员"等荣誉称号者，国家级加 3 分，省级加 2 分，校级加 1 分。',
     3.00, '1', 103, 'admin', NOW(), '0'),

    -- ===== 二、学业成绩素质 =====
    (102, 1, 0,   '2',   '二、学业成绩素质',
     '本模块测评学生学业水平与专业学习成果。',
     NULL, '0', 2, 'admin', NOW(), '0'),

    (121, 1, 102, '2.1', '学业成绩排名',
     '学年学业成绩排名位于本专业前 10% 者加 5 分，前 30% 者加 3 分，前 50% 者加 1 分。',
     5.00, '1', 201, 'admin', NOW(), '0'),

    (122, 1, 102, '2.2', '外语水平',
     '通过全国大学英语四级考试（CET-4）加 1 分，通过六级考试（CET-6）加 2 分。',
     2.00, '1', 202, 'admin', NOW(), '0'),

    (123, 1, 102, '2.3', '计算机水平',
     '通过全国计算机等级考试二级及以上者加 2 分。',
     2.00, '1', 203, 'admin', NOW(), '0'),

    -- ===== 三、科技创新素质 =====
    (103, 1, 0,   '3',   '三、科技创新素质',
     '本模块测评学生科研创新与学科竞赛成果。',
     NULL, '0', 3, 'admin', NOW(), '0'),

    (131, 1, 103, '3.1', '学科竞赛获奖',
     '参加学校认定的学科竞赛并获奖者，国家级一等奖加 10 分、二等奖 8 分、三等奖 6 分；省级一等奖 5 分、二等奖 3 分、三等奖 2 分；校级获奖加 1 分。',
     10.00, '1', 301, 'admin', NOW(), '0'),

    (132, 1, 103, '3.2', '学术论文发表',
     '以第一作者在核心期刊发表学术论文者加 8 分，普通期刊加 3 分，须提供论文见刊证明。',
     8.00, '1', 302, 'admin', NOW(), '0'),

    (133, 1, 103, '3.3', '专利授权',
     '以第一发明人身份获得发明专利授权者加 6 分，实用新型或外观设计专利加 3 分。',
     6.00, '1', 303, 'admin', NOW(), '0'),

    (134, 1, 103, '3.4', '科研项目',
     '主持或参与大学生创新创业训练计划（SRTP）项目并结题者，国家级加 5 分，省级加 3 分，校级加 2 分。',
     5.00, '1', 304, 'admin', NOW(), '0'),

    -- ===== 四、文体活动素质 =====
    (104, 1, 0,   '4',   '四、文体活动素质',
     '本模块测评学生文化体育竞赛与社团活动表现。',
     NULL, '0', 4, 'admin', NOW(), '0'),

    (141, 1, 104, '4.1', '文艺竞赛',
     '参加校级及以上文艺演出或比赛并获奖者，省级及以上加 4 分，校级加 2 分。',
     4.00, '1', 401, 'admin', NOW(), '0'),

    (142, 1, 104, '4.2', '体育竞赛',
     '参加校级及以上体育竞赛并获奖者，省级及以上加 4 分，校级加 2 分。',
     4.00, '1', 402, 'admin', NOW(), '0'),

    (143, 1, 104, '4.3', '社团活动',
     '积极参加社团活动并担任社团负责人满一学年者加 2 分。',
     2.00, '1', 403, 'admin', NOW(), '0'),

    -- ===== 五、社会实践素质 =====
    (105, 1, 0,   '5',   '五、社会实践素质',
     '本模块测评学生社会实践与社会工作表现。',
     NULL, '0', 5, 'admin', NOW(), '0'),

    (151, 1, 105, '5.1', '社会实践',
     '参加暑期"三下乡"等社会实践活动并提交实践报告者加 2 分；被评为校级及以上社会实践优秀个人者加 4 分。',
     4.00, '1', 501, 'admin', NOW(), '0'),

    (152, 1, 105, '5.2', '社会工作',
     '担任学生干部（班委、学生会干事及以上）满一学年且考核合格者加 2 分；担任学生会部长及以上学生干部加 3 分。',
     3.00, '1', 502, 'admin', NOW(), '0');


-- ============================================================
-- 第 6 步：回填申报明细证据原文
-- 说明：assess_declaration_item 表没有 clause_text 列，
--       实际列名为 evidence_clause_text，此处回填该列。
-- ============================================================
UPDATE assess_declaration_item i
   JOIN assess_regulation_clause c ON i.clause_id = c.clause_id
   SET i.evidence_clause_text = c.clause_text,
       i.update_time          = NOW()
 WHERE (i.evidence_clause_text IS NULL OR i.evidence_clause_text = '')
   AND c.clause_text IS NOT NULL
   AND i.del_flag = '0';


-- ============================================================
-- 第 7 步：删除孤儿申报单（0 明细 0 材料）
-- 申报单 1001、1002 无任何明细行和材料关联
-- ============================================================
DELETE FROM assess_declaration_item
 WHERE declaration_id IN (1001, 1002);

DELETE FROM assess_declaration
 WHERE declaration_id IN (1001, 1002);


-- ============================================================
-- 第 8 步：验证查询（只读 SELECT，供第三步执行确认）
-- ============================================================

-- 8a. 细则列表
SELECT regulation_id, regulation_no, title, status
  FROM assess_regulation
 ORDER BY regulation_id;

-- 8b. 条款树（regulation_id=1，按 sort_order 排列）
SELECT clause_id, parent_id, clause_no, clause_title, max_score, clause_type, sort_order
  FROM assess_regulation_clause
 WHERE regulation_id = 1
   AND del_flag = '0'
 ORDER BY sort_order, clause_id;

-- 8c. 检查是否有 clause_text 为 NULL 的活跃条款
SELECT COUNT(*) AS null_text_cnt
  FROM assess_regulation_clause
 WHERE regulation_id = 1
   AND del_flag = '0'
   AND clause_text IS NULL;

-- 8d. 申报单列表
SELECT declaration_id, declaration_no, regulation_id, status
  FROM assess_declaration
 WHERE del_flag = '0'
 ORDER BY declaration_id;
