# 综测智办 · 架构设计文档

> 大学生综合素质测评加分智能认定系统 — MVP 设计草案
>
> 约定：后端若依 RuoYi（Spring Boot + MyBatis），前端 Vue2 + Element UI，数据库 MySQL，AI 服务华为云MaaS外接qwen2.5-vl-72b大模型 + OCR。

---

## 一、角色与用例（权限边界）

系统复用若依自带 `sys_user / sys_role / sys_dept` 体系。`sys_dept` 表示学院→系→班级的树形组织，学生和班委挂在班级节点下，辅导员挂在系或年级节点下。

| 角色 | 角色标识 | 数据范围 | 可执行用例 |
|------|---------|---------|-----------|
| **学生** | `assess:student` | 仅本人数据 | ① 批量上传证明材料（图片/PDF）<br>② 创建申报单（选择材料→触发AI匹配）<br>③ 查看本人申报明细：建议分、证据、规则校验结果<br>④ 导出申报表（PDF/Excel）<br>⑤ 被驳回后修改材料重新提交 |
| **班委** | `assess:committee` | 本班级（同 dept_id）学生数据 | ① 查看本班申报统计（已提交/待审/通过/驳回人数）<br>② 查看本班学生申报明细（只读）<br>③ 协助导出本班汇总表<br>❌ 无审核权、无打分权、无细则管理权 |
| **辅导员** | `assess:counselor` | 所辖班级范围（按 dept 树向下） | ① 上传/管理综测细则 PDF（触发条款切分）<br>② 启用/停用细则版本<br>③ 审核看板：三色（通过🟢/存疑🟡/驳回🔴）<br>④ 逐条或整单审核：通过、标记存疑、驳回<br>⑤ 生成驳回意见（AI辅助起草，辅导员确认）<br>⑥ 配置规则引擎参数（封顶/互斥/时间窗） |

> **权限边界说明**：班委是"只读+导出"角色，不能改任何分值和状态；学生不能看到其他同学的申报；辅导员只能审自己管辖范围内的学生。数据隔离通过 `dept_id` 树形权限 + MyBatis 拦截器实现。

---

## 二、数据库表设计

共 8 张业务表，全部以 `assess_` 为前缀，继承若依 BaseEntity 审计字段。

### 2.1 表清单总览

| 序号 | 表名 | 说明 | 预估行级 |
|------|------|------|---------|
| 1 | `assess_regulation` | 综测细则主表 | 百级（每年1-2版） |
| 2 | `assess_regulation_clause` | 细则条款表（切分后） | 千级（每版50-200条） |
| 3 | `assess_material` | 证明材料主表 | 万级（每生每年5-20份） |
| 4 | `assess_material_field` | 材料结构化字段表（KV） | 十万级（每材料5-15字段） |
| 5 | `assess_declaration` | 申报单主表 | 万级（每生每年1-3单） |
| 6 | `assess_declaration_item` | 申报明细表（材料×条款×分值） | 十万级（每单5-30条） |
| 7 | `assess_review` | 审核操作记录表 | 万级（每单1-3次审核） |
| 8 | `assess_rule_config` | 规则引擎配置表 | 百级（每版细则10-30条规则） |

### 2.2 建表 SQL

```sql
-- ============================================================
-- 综测智办业务表 DDL (MySQL 5.7+)
-- 约定：ID 用 bigint 自增；del_flag char(1) 0=正常 2=删除
-- 所有字段必须有 COMMENT；时间用 datetime
-- ============================================================

-- 1. 综测细则主表
CREATE TABLE `assess_regulation` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '细则ID',
  `title`         VARCHAR(200) NOT NULL                 COMMENT '细则标题（如：2025-2026学年综测细则）',
  `version`       VARCHAR(20)  NOT NULL DEFAULT 'v1'    COMMENT '版本号',
  `academic_year` VARCHAR(20)  NOT NULL                 COMMENT '适用学年（如 2025-2026）',
  `file_url`      VARCHAR(500) NOT NULL                 COMMENT 'PDF文件存储URL（OBS）',
  `file_name`     VARCHAR(200) NOT NULL                 COMMENT '原始文件名',
  `clause_count`  INT          NOT NULL DEFAULT 0       COMMENT '切分条款数',
  `status`        CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '状态（0=草稿 1=启用 2=停用）',
  `split_status`  CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '条款切分状态（0=待处理 1=处理中 2=成功 3=失败）',
  `create_by`     VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`      CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_academic_year` (`academic_year`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测细则主表';

-- 2. 细则条款表（大模型切分后入库）
CREATE TABLE `assess_regulation_clause` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '条款ID',
  `regulation_id` BIGINT       NOT NULL                 COMMENT '所属细则ID',
  `clause_no`     VARCHAR(50)  NOT NULL                 COMMENT '条款编号（如 3.1.2，保留原文层级）',
  `clause_title`  VARCHAR(200) DEFAULT ''               COMMENT '条款标题',
  `clause_content`TEXT         NOT NULL                 COMMENT '条款正文（自然语言全文）',
  `category`      VARCHAR(20)  NOT NULL DEFAULT '其他'  COMMENT '类别（竞赛/论文/志愿/社会实践/专利/其他）',
  `max_score`     DECIMAL(5,1) NOT NULL DEFAULT 0.0     COMMENT '该条款可加最高分',
  `score_rule`    VARCHAR(500) DEFAULT ''               COMMENT '加分条件描述（如：国家级一等奖加3分）',
  `parent_id`     BIGINT       DEFAULT 0                COMMENT '父条款ID（0=顶级，支持层级树）',
  `sort_order`    INT          NOT NULL DEFAULT 0       COMMENT '排序号',
  `create_by`     VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`      CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_regulation_id` (`regulation_id`),
  KEY `idx_category` (`category`),
  KEY `idx_clause_no` (`clause_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测细则条款表';

-- 3. 证明材料主表
CREATE TABLE `assess_material` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '材料ID',
  `student_id`    BIGINT       NOT NULL                 COMMENT '学生用户ID（sys_user.user_id）',
  `student_name`  VARCHAR(50)  NOT NULL                 COMMENT '学生姓名（冗余，便于查询）',
  `student_no`    VARCHAR(30)  NOT NULL                 COMMENT '学号',
  `dept_id`       BIGINT       NOT NULL                 COMMENT '班级部门ID（数据隔离用）',
  `file_url`      VARCHAR(500) NOT NULL                 COMMENT '文件存储URL（OBS）',
  `file_name`     VARCHAR(200) NOT NULL                 COMMENT '原始文件名',
  `file_type`     VARCHAR(10)  NOT NULL                 COMMENT '文件类型（image/pdf）',
  `file_size`     BIGINT       NOT NULL DEFAULT 0       COMMENT '文件大小（字节）',
  `material_type` VARCHAR(20)  DEFAULT ''               COMMENT '材料类型（竞赛证书/志愿证明/论文录用/专利/其他）',
  `ocr_status`    CHAR(1)      NOT NULL DEFAULT '0'     COMMENT 'OCR状态（0=待处理 1=处理中 2=成功 3=失败）',
  `ocr_text`      TEXT                                  COMMENT 'OCR识别全文文本',
  `extract_status`CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '字段抽取状态（0=待处理 1=处理中 2=成功 3=失败）',
  `create_by`     VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`      CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_ocr_status` (`ocr_status`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证明材料主表';

-- 4. 材料结构化字段表（大模型抽取，KV 模式适配异构材料）
CREATE TABLE `assess_material_field` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '字段ID',
  `material_id`   BIGINT       NOT NULL                 COMMENT '所属材料ID',
  `field_key`     VARCHAR(50)  NOT NULL                 COMMENT '字段名（award_name/award_level/award_date/organization/participant/paper_title/journal_name/volunteer_hours等）',
  `field_value`   VARCHAR(500) DEFAULT ''               COMMENT '字段值',
  `confidence`    DECIMAL(3,2) DEFAULT 0.00             COMMENT '抽取置信度（0.00-1.00）',
  `create_time`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料结构化字段表';

-- 5. 申报单主表
CREATE TABLE `assess_declaration` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '申报单ID',
  `student_id`    BIGINT       NOT NULL                 COMMENT '学生用户ID',
  `student_name`  VARCHAR(50)  NOT NULL                 COMMENT '学生姓名',
  `student_no`    VARCHAR(30)  NOT NULL                 COMMENT '学号',
  `dept_id`       BIGINT       NOT NULL                 COMMENT '班级部门ID',
  `academic_year` VARCHAR(20)  NOT NULL                 COMMENT '申报学年',
  `regulation_id` BIGINT       NOT NULL                 COMMENT '依据的细则ID',
  `total_score`   DECIMAL(6,1) NOT NULL DEFAULT 0.0     COMMENT '申报总分（各明细final_score之和）',
  `status`        CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '申报状态（0=草稿 1=已提交 2=审核中 3=部分通过 4=全部通过 5=已驳回）',
  `review_color`  CHAR(1)      DEFAULT NULL             COMMENT '审核颜色（1=通过绿 2=存疑黄 3=驳回红），NULL=待审',
  `create_by`     VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`      CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_academic_year` (`academic_year`),
  KEY `idx_status` (`status`),
  KEY `idx_review_color` (`review_color`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申报单主表';
```

```sql
-- 6. 申报明细表（核心：材料 × 条款 × 分值 × 证据）
CREATE TABLE `assess_declaration_item` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `declaration_id`       BIGINT       NOT NULL                 COMMENT '所属申报单ID',
  `material_id`          BIGINT       NOT NULL                 COMMENT '关联材料ID',
  `clause_id`            BIGINT       DEFAULT NULL             COMMENT '匹配到的条款ID（NULL=未匹配）',
  `item_desc`            VARCHAR(500) DEFAULT ''               COMMENT '申报说明（学生填写）',
  `suggested_score`      DECIMAL(5,1) DEFAULT NULL             COMMENT 'AI建议分值（大模型产出，不直接入库）',
  `evidence_clause_no`   VARCHAR(50)  DEFAULT ''               COMMENT '证据-条款编号',
  `evidence_clause_text` TEXT                                  COMMENT '证据-条款原文',
  `evidence_confidence`  DECIMAL(3,2) DEFAULT 0.00             COMMENT '证据-置信度（0.00-1.00）',
  `match_reason`         TEXT                                  COMMENT 'AI匹配理由（自然语言）',
  `rule_check_result`    TEXT                                  COMMENT '规则引擎校验结果（JSON：封顶/互斥/时间窗明细 + alternatives候选）',
  `rule_adjusted_score`  DECIMAL(5,1) DEFAULT NULL             COMMENT '规则引擎调整后分值',
  `final_score`          DECIMAL(5,1) DEFAULT NULL             COMMENT '最终入库分值（规则引擎产出）',
  `match_status`         CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '匹配状态（0=待匹配 1=已匹配 2=未匹配 3=存疑）',
  `create_by`            VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`          DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`            VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`          DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`             CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_declaration_id` (`declaration_id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_clause_id` (`clause_id`),
  KEY `idx_match_status` (`match_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申报明细表';

-- 7. 审核操作记录表
CREATE TABLE `assess_review` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '审核记录ID',
  `declaration_id`  BIGINT       NOT NULL                 COMMENT '申报单ID',
  `item_id`         BIGINT       DEFAULT NULL             COMMENT '明细ID（NULL=整单审核）',
  `reviewer_id`     BIGINT       NOT NULL                 COMMENT '审核人用户ID',
  `reviewer_name`   VARCHAR(50)  NOT NULL                 COMMENT '审核人姓名',
  `review_action`   CHAR(1)      NOT NULL                 COMMENT '审核动作（1=通过 2=存疑 3=驳回）',
  `review_comment`  TEXT                                  COMMENT '审核意见（驳回时必填，可AI辅助生成）',
  `ai_generated`    CHAR(1)      DEFAULT '0'              COMMENT '意见是否AI生成（0=人工 1=AI辅助）',
  `create_by`       VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`     DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`     DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`        CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_declaration_id` (`declaration_id`),
  KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核操作记录表';

-- 8. 规则引擎配置表
CREATE TABLE `assess_rule_config` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '规则ID',
  `regulation_id` BIGINT       NOT NULL                 COMMENT '所属细则ID',
  `rule_type`     VARCHAR(20)  NOT NULL                 COMMENT '规则类型（cap=封顶 mutex=互斥 time_window=时间窗）',
  `rule_name`     VARCHAR(100) NOT NULL                 COMMENT '规则名称',
  `rule_config`   TEXT         NOT NULL                 COMMENT '规则配置（JSON，见下方说明）',
  `priority`      INT          NOT NULL DEFAULT 0       COMMENT '执行优先级（小先执行）',
  `status`        CHAR(1)      NOT NULL DEFAULT '1'     COMMENT '状态（0=禁用 1=启用）',
  `create_by`     VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT NULL             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT NULL             COMMENT '更新时间',
  `del_flag`      CHAR(1)      DEFAULT '0'              COMMENT '删除标志（0=存在 2=删除）',
  PRIMARY KEY (`id`),
  KEY `idx_regulation_id` (`regulation_id`),
  KEY `idx_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则引擎配置表';
```

### 2.3 rule_config JSON 各类型示例

```jsonc
// cap（封顶）：同一类别总分不超过上限
{"category": "竞赛", "max_total": 10.0}

// mutex（互斥）：同一材料只能匹配其中一条条款
{"clause_ids": [101, 102, 103], "strategy": "highest"}

// time_window（时间窗）：材料日期须在指定区间内
{"date_field": "award_date", "start": "2025-09-01", "end": "2026-08-31"}
```

---

## 三、后端接口清单

> 约定：统一返回 `AjaxResult`；列表接口走若依 `TableDataInfo` 分页；权限标识格式 `assess:模块:操作`。

### 3.1 细则管理（辅导员）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 1 | POST | `/assess/regulation/upload` | `MultipartFile file, String academicYear, String title` | `{code, msg, data:{regulationId, clauseCount}}` | `assess:regulation:upload` | 上传PDF，异步触发条款切分 |
| 2 | GET | `/assess/regulation/list` | `String academicYear, String status, pageNum, pageSize` | `TableDataInfo[{id, title, version, academicYear, clauseCount, status, splitStatus}]` | `assess:regulation:list` | 细则列表 |
| 3 | GET | `/assess/regulation/{id}` | `id` | `{id, title, version, academicYear, fileUrl, clauseCount, status}` | `assess:regulation:query` | 细则详情 |
| 4 | GET | `/assess/regulation/{id}/clauses` | `id, String category` | `[{id, clauseNo, clauseTitle, clauseContent, category, maxScore, scoreRule, parentId}]` | `assess:regulation:query` | 细则下条款列表（可按类别过滤） |
| 5 | PUT | `/assess/regulation/{id}/status` | `id, String status` | `{code, msg}` | `assess:regulation:edit` | 启用/停用细则 |
| 6 | DELETE | `/assess/regulation/{ids}` | `ids` | `{code, msg}` | `assess:regulation:remove` | 删除细则（连带条款逻辑删除） |

### 3.2 条款管理（辅导员）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 7 | GET | `/assess/clause/{id}` | `id` | `{id, regulationId, clauseNo, clauseTitle, clauseContent, category, maxScore, scoreRule, parentId, sortOrder}` | `assess:clause:query` | 条款详情 |
| 8 | PUT | `/assess/clause` | `AssessRegulationClause` | `{code, msg}` | `assess:clause:edit` | 编辑条款（人工修正切分结果） |

### 3.3 材料管理（学生）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 9 | POST | `/assess/material/upload` | `MultipartFile[] files, String materialType, Long declarationId(可选)` | `{code, msg, data:[{materialId, fileName, fileType}]}` | `assess:material:upload` | 批量上传，异步触发OCR+抽取；若传declarationId则直接关联申报单 |
| 10 | GET | `/assess/material/list` | `String materialType, String ocrStatus, pageNum, pageSize` | `TableDataInfo[{id, fileName, fileType, materialType, ocrStatus, extractStatus, createTime}]` | `assess:material:list` | 本人材料列表 |
| 11 | GET | `/assess/material/{id}` | `id` | `{id, fileUrl, fileName, ocrText, ocrStatus, fields:[{fieldKey, fieldValue, confidence}]}` | `assess:material:query` | 材料详情（含结构化字段） |
| 12 | DELETE | `/assess/material/{ids}` | `ids` | `{code, msg}` | `assess:material:remove` | 删除材料 |
| 13 | POST | `/assess/material/{id}/reprocess` | `id` | `{code, msg}` | `assess:material:edit` | 重新OCR+抽取（失败重试） |

### 3.4 申报管理（学生）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 14 | POST | `/assess/declaration` | `{regulationId, academicYear, materialIds:[], remark}` | `{code, msg, data:{declarationId, itemCount}}` | `assess:declaration:add` | 创建申报单（选材料→触发AI匹配） |
| 15 | GET | `/assess/declaration/list` | `String academicYear, String status, pageNum, pageSize` | `TableDataInfo[{id, academicYear, totalScore, status, reviewColor, createTime}]` | `assess:declaration:list` | 本人申报列表 |
| 16 | GET | `/assess/declaration/{id}` | `id` | `{id, academicYear, regulationId, totalScore, status, reviewColor, items:[{...declaration_item全字段, materialFileName, clauseNo, clauseContent}]}` | `assess:declaration:query` | 申报详情（含明细+证据） |
| 17 | PUT | `/assess/declaration/{id}/submit` | `id` | `{code, msg}` | `assess:declaration:edit` | 提交申报（草稿→已提交） |
| 18 | GET | `/assess/declaration/{id}/export` | `id, String format(pdf/excel)` | 文件流（Content-Type: application/pdf 或 excel） | `assess:declaration:export` | 导出申报表 |
| 19 | PUT | `/assess/declaration/item/{itemId}` | `{itemId, clauseId, itemDesc}` | `{code, msg}` | `assess:declaration:edit` | 修改明细（人工调整匹配条款） |


### 3.5 AI 匹配（内部调用，学生触发）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 20 | POST | `/assess/match/run` | `{declarationId}` | `{code, msg, data:{matchedCount, pendingCount, failedCount}}` | `assess:declaration:edit` | 触发AI匹配（材料字段→条款语义匹配+建议分+规则校验） |
| 21 | GET | `/assess/match/status/{declarationId}` | `declarationId` | `{declarationId, totalItems, matched, pending, failed, processing}` | `assess:declaration:query` | 查询匹配进度 |

### 3.6 审核管理（辅导员）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 22 | GET | `/assess/review/dashboard` | `String academicYear, Long deptId` | `{total, pending, passed, suspicious, rejected, byClass:[{deptId, deptName, total, passed, suspicious, rejected}]}` | `assess:review:list` | 审核看板（三色统计） |
| 23 | GET | `/assess/review/list` | `String reviewColor, String studentName, String studentNo, pageNum, pageSize` | `TableDataInfo[{declarationId, studentName, studentNo, totalScore, reviewColor, submitTime}]` | `assess:review:list` | 待审核申报列表 |
| 24 | GET | `/assess/review/{declarationId}` | `declarationId` | 同接口16 + `reviewHistory:[{reviewerName, reviewAction, reviewComment, createTime}]` | `assess:review:query` | 审核详情 |
| 25 | PUT | `/assess/review/action` | `{declarationId, itemId(可选), reviewAction(1/2/3), reviewComment}` | `{code, msg}` | `assess:review:edit` | 审核操作（1=通过 2=存疑 3=驳回） |
| 26 | POST | `/assess/review/rejection-draft` | `{declarationId, itemId, reasonHint}` | `{code, msg, data:{draftComment}}` | `assess:review:edit` | AI辅助生成驳回意见草稿（辅导员确认后提交） |

### 3.7 规则引擎配置（辅导员）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 27 | GET | `/assess/rule/list` | `Long regulationId, String ruleType, pageNum, pageSize` | `TableDataInfo[{id, regulationId, ruleType, ruleName, ruleConfig, priority, status}]` | `assess:rule:list` | 规则列表 |
| 28 | POST | `/assess/rule` | `AssessRuleConfig` | `{code, msg}` | `assess:rule:add` | 新增规则 |
| 29 | PUT | `/assess/rule` | `AssessRuleConfig` | `{code, msg}` | `assess:rule:edit` | 编辑规则 |
| 30 | DELETE | `/assess/rule/{ids}` | `ids` | `{code, msg}` | `assess:rule:remove` | 删除规则 |

### 3.8 班级统计（班委，只读）

| # | 方法 | 路径 | 入参 | 出参 | 权限标识 | 说明 |
|---|------|------|------|------|---------|------|
| 31 | GET | `/assess/stats/class` | `String academicYear` | `{className, totalStudents, submitted, pending, passed, suspicious, rejected, avgScore}` | `assess:stats:class` | 本班申报统计 |
| 32 | GET | `/assess/stats/class/students` | `String academicYear, String status, pageNum, pageSize` | `TableDataInfo[{studentName, studentNo, declarationId, totalScore, status, reviewColor}]` | `assess:stats:class` | 本班学生申报明细（只读） |

---

## 四、AI 调用链路时序

整条链路共 **4 次模型调用** + **1 次纯规则计算**，封装在 `ai/` 包中，业务代码不直接调模型。

```
学生上传材料                 辅导员上传细则
     │                          │
     ▼                          ▼
 ┌─────────┐              ┌──────────┐
 │ Step 0  │              │ Step A   │
 │  OCR    │              │ PDF解析  │
 │(盘古OCR) │              │(PDF→文本) │
 └────┬────┘              └─────┬────┘
      │                         │
      ▼                         ▼
 ┌─────────┐              ┌──────────┐
 │ Step 1  │              │ Step B   │
 │结构化抽取 │              │条款切分   │
 │(盘古LLM) │              │(盘古LLM) │
 └────┬────┘              └─────┬────┘
      │                         │
      │   ┌─────────────────────┘
      ▼   ▼
 ┌──────────┐
 │ Step 2   │
 │语义匹配   │     输入：材料字段 + 候选条款
 │+建议分    │     输出：Top3候选 + 各自建议分 + 证据 + 置信度
 │(盘古LLM) │
 └─────┬────┘
       │
       ▼
 ┌──────────┐
 │ Step 3   │     纯规则计算，不调模型
 │规则引擎   │     封顶/互斥/时间窗校验
 │(Java代码) │     输出：final_score + check_result
 └─────┬────┘
       │
       ▼
   入库（assess_declaration_item）
```

> 另有 **Step C（驳回意见草稿）**：辅导员审核时按需触发，独立于主链路。

### 4.0 链路总览

| 步骤 | 名称 | 调用对象 | 输入 | 输出 | 是否入库分值 |
|------|------|---------|------|------|------------|
| Step A | PDF解析+条款切分 | 盘古LLM | 细则PDF全文 | `List<{clause_no, clause_content, category, max_score}>` | 否 |
| Step 0 | OCR识别 | 华为云OCR | 图片/PDF二进制 | `{text, blocks}` | 否 |
| Step 1 | 结构化字段抽取 | 盘古LLM | OCR文本+材料类型 | `{award_name, award_level, ...}` | 否 |
| Step 2 | 语义匹配+建议分 | 盘古LLM | 材料字段+候选条款 | Top3候选`{clause_id, suggested_score, evidence, confidence}` | 否（建议分不入库） |
| Step 3 | 规则引擎校验 | 纯Java | `List<MatchResult>` + 规则配置 | `AssessmentResult{final_score, check_details}` | 是（final_score 入库） |
| Step C | 驳回意见草稿 | 盘古LLM | 申报明细+审核上下文 | `{draft_comment}` | 否（草稿不入库） |

### 4.1 Step A — PDF 解析 + 条款切分

**触发时机**：辅导员上传细则 PDF（接口 1）

**A-1. PDF → 纯文本**（不调模型，用 PDF 解析库）

**A-2. 大模型条款切分**

模型：华为云盘古大模型 MaaS

system_prompt：

```
你是综测细则解析专家。将以下细则全文切分为独立条款，每条输出结构化JSON。
```

输入：

```json
{
  "system_prompt": "你是综测细则解析专家。将以下细则全文切分为独立条款，每条输出结构化JSON。",
  "user_content": "<PDF全文>",
  "response_schema": {
    "type": "array",
    "items": {
      "type": "object",
      "properties": {
        "clause_no":     {"type": "string", "description": "条款编号，保留原文层级如3.1.2"},
        "clause_title":  {"type": "string", "description": "条款标题"},
        "clause_content":{"type": "string", "description": "条款正文"},
        "category":      {"type": "string", "enum": ["竞赛","论文","志愿","社会实践","专利","其他"]},
        "max_score":     {"type": "number", "description": "该条款最高可加分值"},
        "score_rule":    {"type": "string", "description": "加分条件描述"},
        "parent_no":     {"type": "string", "description": "父条款编号，顶级为空"}
      },
      "required": ["clause_no", "clause_content", "category", "max_score"]
    }
  }
}
```

输出（严格JSON）：

```json
[
  {"clause_no":"3.1.1","clause_title":"学科竞赛","clause_content":"参加国家级学科竞赛...","category":"竞赛","max_score":3.0,"score_rule":"国家级一等奖3分，二等奖2分，三等奖1分","parent_no":"3.1"},
  {"clause_no":"3.1.2","clause_title":"国家级竞赛","clause_content":"获国家级一等奖加3分...","category":"竞赛","max_score":3.0,"score_rule":"一等奖3分","parent_no":"3.1.1"}
]
```

兜底：解析失败 → `split_status=3`，保留原始文本供人工切分。

---

### 4.2 Step 0 — OCR 识别

**触发时机**：学生上传材料后异步执行（接口 9）

模型：华为云 OCR 服务

输入：图片/PDF文件二进制

输出：

```json
{
  "text": "第十三届\"挑战杯\"大学生课外学术科技作品竞赛\n获奖证书\nXXX同学：\n你的作品《基于深度学习的...》荣获国家级一等奖\n发证单位：共青团中央 全国学联\n2025年6月15日",
  "blocks": [
    {"text": "第十三届\"挑战杯\"...", "location": {"x":100,"y":50,"w":400,"h":30}}
  ]
}
```

兜底：OCR失败 → `ocr_status=3`，不阻塞后续流程，可手动重试（接口13）。

---

### 4.3 Step 1 — 材料字段结构化抽取

**触发时机**：OCR 成功后自动衔接

模型：华为云盘古大模型 MaaS

system_prompt：

```
从以下OCR识别文本中抽取证明材料的关键结构化字段。无法确定的字段填null，不要编造。
```

输入：

```json
{
  "system_prompt": "从以下OCR识别文本中抽取证明材料的关键结构化字段。无法确定的字段填null，不要编造。",
  "user_content": "OCR文本: <Step0.text>\n材料类型提示: <material_type>",
  "response_schema": {
    "type": "object",
    "properties": {
      "award_name":      {"type": "string", "description": "竞赛/活动名称"},
      "award_level":     {"type": "string", "enum": ["国家级","省级","市级","校级","院级"], "description": "获奖级别"},
      "award_rank":      {"type": "string", "description": "获奖等次（一等奖/二等奖/三等奖/优秀奖）"},
      "award_date":      {"type": "string", "description": "获奖日期 YYYY-MM-DD"},
      "organization":    {"type": "string", "description": "发证单位"},
      "participant_name":{"type": "string", "description": "获奖人/参与人姓名"},
      "paper_title":     {"type": "string", "description": "论文题目（如有）"},
      "journal_name":    {"type": "string", "description": "期刊/会议名称（如有）"},
      "volunteer_hours": {"type": "number", "description": "志愿服务时长（小时）"},
      "extra_info":      {"type": "string", "description": "其他补充信息"}
    }
  }
}
```

输出（严格JSON）：

```json
{
  "award_name": "第十三届\"挑战杯\"大学生课外学术科技作品竞赛",
  "award_level": "国家级",
  "award_rank": "一等奖",
  "award_date": "2025-06-15",
  "organization": "共青团中央 全国学联",
  "participant_name": "张三",
  "paper_title": null,
  "journal_name": null,
  "volunteer_hours": null,
  "extra_info": null
}
```

入库：逐字段写入 `assess_material_field`，`confidence` 取模型返回的置信度（若模型未返回单字段置信度，整体置 0.80）。

兜底策略：

| 异常情况 | 处理 |
|---------|------|
| `null` 字段 | 不写入 `assess_material_field`（前端显示"未识别"） |
| JSON 解析失败 | `extract_status=3`，保留 `ocr_text` 供人工补录 |
| 日期格式不合法 | 保留原文写入 `field_value`，`confidence=0.50` |

---

### 4.4 Step 2 — 语义匹配 + 建议分（核心环节）

**触发时机**：学生创建/提交申报单（接口 14/20），对每份材料逐条匹配

**策略**：输出 **Top3 候选条款**，每条携带建议分 + 证据 + 置信度。最终取哪条由 Step 3 规则引擎在互斥校验中决定。学生可看到"你这张证书还可以匹配另一条，但互斥只取高分项"，透明度拉满。

模型：华为云盘古大模型 MaaS

system_prompt：

```
你是综测加分认定专家。给定一份证明材料的结构化字段和若干候选细则条款，
请对每个候选条款进行语义匹配打分，输出 Top3 最匹配的条款。

规则：
1. 只输出真正相关的条款，不足3条则少输出，不要凑数
2. 每个候选必须携带 evidence：条款编号、条款原文、匹配置信度
3. suggested_score 不得超过该条款的 max_score
4. match_reason 用一句话说明为什么这张材料符合该条款
5. 如果没有任何条款匹配，返回空数组
```

输入：

```json
{
  "material": {
    "material_id": 5001,
    "material_type": "竞赛证书",
    "fields": {
      "award_name": "第十三届\"挑战杯\"大学生课外学术科技作品竞赛",
      "award_level": "国家级",
      "award_rank": "一等奖",
      "award_date": "2025-06-15",
      "organization": "共青团中央 全国学联",
      "participant_name": "张三"
    }
  },
  "candidate_clauses": [
    {
      "clause_id": 201,
      "clause_no": "3.1.2",
      "clause_content": "获国家级学科竞赛一等奖加3分",
      "category": "竞赛",
      "max_score": 3.0
    },
    {
      "clause_id": 202,
      "clause_no": "3.1.3",
      "clause_content": "获国家级学科竞赛二等奖加2分",
      "category": "竞赛",
      "max_score": 2.0
    },
    {
      "clause_id": 210,
      "clause_no": "3.2.1",
      "clause_content": "参加国家级竞赛但未获奖加0.5分",
      "category": "竞赛",
      "max_score": 0.5
    }
  ]
}
```

> `candidate_clauses` 的来源：按 `material_type` 映射到 `category`，从当前启用细则中拉取同类别条款。若同类别条款 > 20 条，先按关键词粗筛（如"国家级""一等奖"）再送模型，控制 token 成本。

输出 JSON Schema：

```json
{
  "type": "object",
  "properties": {
    "material_id": {"type": "integer"},
    "candidates": {
      "type": "array",
      "description": "Top3 候选，按 confidence 降序",
      "items": {
        "type": "object",
        "properties": {
          "clause_id":          {"type": "integer"},
          "clause_no":          {"type": "string"},
          "clause_content":     {"type": "string"},
          "suggested_score":    {"type": "number", "description": "建议分值，不超过max_score"},
          "confidence":         {"type": "number", "minimum": 0, "maximum": 1, "description": "匹配置信度"},
          "match_reason":       {"type": "string", "description": "一句话匹配理由"},
          "evidence": {
            "type": "object",
            "properties": {
              "clause_no":      {"type": "string"},
              "clause_text":    {"type": "string"},
              "confidence":     {"type": "number"}
            },
            "required": ["clause_no", "clause_text", "confidence"]
          }
        },
        "required": ["clause_id", "suggested_score", "confidence", "match_reason", "evidence"]
      },
      "maxItems": 3
    }
  },
  "required": ["material_id", "candidates"]
}
```

输出示例：

```json
{
  "material_id": 5001,
  "candidates": [
    {
      "clause_id": 201,
      "clause_no": "3.1.2",
      "clause_content": "获国家级学科竞赛一等奖加3分",
      "suggested_score": 3.0,
      "confidence": 0.95,
      "match_reason": "材料为国家级挑战杯一等奖，与条款3.1.2完全对应",
      "evidence": {
        "clause_no": "3.1.2",
        "clause_text": "获国家级学科竞赛一等奖加3分",
        "confidence": 0.95
      }
    },
    {
      "clause_id": 210,
      "clause_no": "3.2.1",
      "clause_content": "参加国家级竞赛但未获奖加0.5分",
      "suggested_score": 0.5,
      "confidence": 0.30,
      "match_reason": "材料显示已获奖，该条款适用于未获奖情况，仅作备选",
      "evidence": {
        "clause_no": "3.2.1",
        "clause_text": "参加国家级竞赛但未获奖加0.5分",
        "confidence": 0.30
      }
    }
  ]
}
```

入库：Top1 写入 `assess_declaration_item` 主记录（`suggested_score` / `evidence_*` / `match_status=1`），Top2/Top3 以 JSON 存入 `rule_check_result` 的 `alternatives` 字段，供规则引擎互斥择优和学生查看。

兜底策略：

| 异常情况 | 处理 |
|---------|------|
| `candidates` 为空 | `match_status=2`（未匹配），`suggested_score=null`，学生可手动指定条款 |
| JSON 解析失败 | `match_status=0`（待匹配），记录错误日志，支持手动重试 |
| `suggested_score > max_score` | 代码层截断为 `max_score`，记入 `rule_check_result` 的 `warnings` |
| `confidence < 0.50` | `match_status=3`（存疑），前端高亮提醒辅导员重点关注 |
| 模型超时/限流 | 重试 1 次；仍失败则 `match_status=0`，不阻塞其他材料匹配 |

---

### 4.5 Step 3 — 规则引擎校验（纯 Java，不调模型）

**触发时机**：Step 2 全部材料匹配完成后，对整张申报单批量校验

**输入**：`List<MatchResult>`（每份材料的 Top1 + alternatives）

**输出**：`AssessmentResult`（每条明细的最终分 + 校验明细）

**三类规则执行顺序**：`time_window` → `mutex` → `cap`（先过滤不合规，再择优，最后封顶）

```java
/**
 * 规则引擎入口：对一张申报单的所有匹配结果执行封顶/互斥/时间窗校验
 * 纯 Java 计算，不调用任何大模型
 */
public AssessmentResult executeRules(List<MatchResult> matchResults,
                                     List<AssessRuleConfig> rules) {

    AssessmentResult result = new AssessmentResult();
    List<ItemResult> itemResults = new ArrayList<>();

    // ── 1. 时间窗校验（time_window）──
    //    规则：材料上的日期字段必须在指定区间内
    //    不合规的材料：建议分清零，标记违规原因
    List<AssessRuleConfig> timeWindowRules = filterByType(rules, "time_window");

    for (MatchResult mr : matchResults) {
        ItemResult ir = new ItemResult();
        ir.setItemId(mr.getItemId());
        ir.setMaterialId(mr.getMaterialId());
        ir.setSuggestedScore(mr.getSuggestedScore());
        ir.setEvidence(mr.getEvidence());
        List<String> warnings = new ArrayList<>();

        for (AssessRuleConfig rule : timeWindowRules) {
            TimeWindowConfig twc = parseConfig(rule.getRuleConfig(), TimeWindowConfig.class);
            String dateValue = mr.getFieldValue(twc.getDateField()); // 如 award_date

            if (StringUtils.isNotBlank(dateValue)) {
                LocalDate awardDate = parseDate(dateValue);
                if (awardDate == null) {
                    warnings.add("日期格式无法解析: " + dateValue);
                    continue;
                }
                if (awardDate.isBefore(twc.getStart()) || awardDate.isAfter(twc.getEnd())) {
                    warnings.add(String.format(
                        "材料日期%s不在有效区间%s~%s内，该项不加分",
                        dateValue, twc.getStart(), twc.getEnd()));
                    ir.setSuggestedScore(BigDecimal.ZERO); // 时间窗不合规，清零
                    ir.setTimeWindowViolated(true);
                }
            }
        }
        itemResults.add(ir);
    }

    // ── 2. 互斥校验（mutex）──
    //    规则：一组互斥条款中，同一材料只能取一条
    //    策略：从 Top1 + alternatives 中选 suggested_score 最高的一条
    //    被淘汰的候选记入 checkDetail 供学生查看"为什么没选这条"
    List<AssessRuleConfig> mutexRules = filterByType(rules, "mutex");

    for (AssessRuleConfig rule : mutexRules) {
        MutexConfig mc = parseConfig(rule.getRuleConfig(), MutexConfig.class);
        // 找到本申报单中命中了互斥条款组的所有明细
        List<ItemResult> hitItems = itemResults.stream()
            .filter(ir -> mc.getClauseIds().contains(ir.getClauseId()))
            .collect(Collectors.toList());

        if (hitItems.size() <= 1) {
            continue; // 只命中一条，无冲突
        }

        // 按 suggested_score 降序，取最高分项
        hitItems.sort((a, b) -> b.getSuggestedScore().compareTo(a.getSuggestedScore()));
        ItemResult winner = hitItems.get(0);

        for (int i = 1; i < hitItems.size(); i++) {
            ItemResult loser = hitItems.get(i);
            loser.setSuggestedScore(BigDecimal.ZERO);
            loser.addCheckDetail("mutex", false, String.format(
                "与条款%s互斥，已择优保留条款%s（建议分%s），本项不重复加分",
                winner.getClauseNo(), winner.getClauseNo(), winner.getSuggestedScore()));
        }
        winner.addCheckDetail("mutex", true, String.format(
            "互斥组内择优保留（共%d条候选，取最高分%s）",
            hitItems.size(), winner.getSuggestedScore()));
    }

    // ── 3. 封顶校验（cap）──
    //    规则：同一类别的总分不超过上限
    //    超出时从最低分项开始扣减，直到总和 <= 上限
    List<AssessRuleConfig> capRules = filterByType(rules, "cap");

    for (AssessRuleConfig rule : capRules) {
        CapConfig cc = parseConfig(rule.getRuleConfig(), CapConfig.class);
        // 按类别聚合当前申报单的所有建议分
        BigDecimal categoryTotal = itemResults.stream()
            .filter(ir -> ir.getCategory().equals(cc.getCategory()))
            .map(ItemResult::getSuggestedScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (categoryTotal.compareTo(cc.getMaxTotal()) > 0) {
            // 截断策略：从最低分项开始扣减，直到总和 <= 上限
            List<ItemResult> categoryItems = itemResults.stream()
                .filter(ir -> ir.getCategory().equals(cc.getCategory()))
                .sorted(Comparator.comparing(ItemResult::getSuggestedScore))
                .collect(Collectors.toList());

            BigDecimal excess = categoryTotal.subtract(cc.getMaxTotal());
            for (ItemResult ir : categoryItems) {
                if (excess.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal deduct = ir.getSuggestedScore().min(excess);
                ir.setSuggestedScore(ir.getSuggestedScore().subtract(deduct));
                excess = excess.subtract(deduct);
                ir.addCheckDetail("cap", false, String.format(
                    "类别[%s]总分%.1f超过封顶%.1f，本项扣减%.1f",
                    cc.getCategory(), categoryTotal, cc.getMaxTotal(), deduct));
            }
        }
    }

    // ── 4. 产出最终分 ──
    //    rule_adjusted_score = 经过上述三步校验后的建议分
    //    final_score = rule_adjusted_score（入库分值，学生可见）
    BigDecimal totalScore = BigDecimal.ZERO;
    for (ItemResult ir : itemResults) {
        ir.setRuleAdjustedScore(ir.getSuggestedScore()); // 经校验调整后的值
        ir.setFinalScore(ir.getRuleAdjustedScore());     // MVP阶段两者一致
        totalScore = totalScore.add(ir.getFinalScore());
        ir.setRuleCheckResult(toJson(ir.getCheckDetails())); // 明细JSON入库
    }

    result.setItemResults(itemResults);
    result.setTotalScore(totalScore);
    return result;
}
```

核心数据结构：

```java
/** Step 2 的单条匹配结果（含 Top3 alternatives） */
public class MatchResult {
    private Long itemId;
    private Long materialId;
    private Long clauseId;           // Top1 条款
    private String clauseNo;
    private String category;
    private BigDecimal suggestedScore;
    private Evidence evidence;       // 条款编号 + 条款原文 + 置信度
    private List<Alternative> alternatives; // Top2/Top3 候选
    private Map<String, String> fields;     // 材料结构化字段（供时间窗取日期）
}

/** Step 3 的单条校验结果 */
public class ItemResult {
    private Long itemId;
    private Long materialId;
    private Long clauseId;
    private String clauseNo;
    private String category;
    private BigDecimal suggestedScore;    // AI建议分（经校验调整后）
    private BigDecimal ruleAdjustedScore; // 规则引擎调整后
    private BigDecimal finalScore;        // 最终入库分
    private Evidence evidence;
    private boolean timeWindowViolated;
    private List<CheckDetail> checkDetails; // 各规则校验明细
}

/** 单条规则校验明细 */
public class CheckDetail {
    private String ruleType;   // cap / mutex / time_window
    private boolean passed;    // 是否通过
    private String message;    // 人可读的说明
}

/** Step 3 整体输出 */
public class AssessmentResult {
    private List<ItemResult> itemResults;
    private BigDecimal totalScore;  // 申报单总分
}
```

> **AGENTS.md 第 5 条合规说明**：`suggested_score` 来自大模型但**不直接入库为最终分**；`final_score` 由规则引擎经三步校验后产出，满足"最终入库分值由规则引擎计算，禁止直接采信大模型给出的分数"。

---

### 4.6 Step C — 驳回意见草稿（按需触发）

**触发时机**：辅导员在审核看板点击"生成驳回意见"（接口 26）

模型：华为云盘古大模型 MaaS

system_prompt：

```
你是综测审核助手。根据以下申报明细和审核上下文，起草一段驳回意见。
要求：
1. 语气客观、礼貌，以"经审核"开头
2. 明确指出哪条材料不符合哪个条款，给出具体原因
3. 如果是分值问题，说明建议分与实际应得的差异
4. 如果是材料问题，说明缺什么、需要补什么
5. 结尾给出修改建议，如"请补充XXX后重新提交"
6. 不超过200字，不要编造条款内容
```

输入：

```json
{
  "declaration": {
    "declaration_id": 3001,
    "student_name": "张三",
    "student_no": "2025010001",
    "academic_year": "2025-2026"
  },
  "target_item": {
    "material_file_name": "挑战杯一等奖证书.jpg",
    "clause_no": "3.1.2",
    "clause_content": "获国家级学科竞赛一等奖加3分",
    "suggested_score": 3.0,
    "match_reason": "材料为国家级挑战杯一等奖，与条款3.1.2完全对应",
    "rule_check_result": {
      "time_window": {"passed": false, "message": "材料日期2024-06-15不在有效区间2025-09-01~2026-08-31内"}
    }
  },
  "reason_hint": "时间不在本学年范围内",
  "reviewer_name": "李辅导员"
}
```

输出 JSON Schema：

```json
{
  "type": "object",
  "properties": {
    "draft_comment": {
      "type": "string",
      "description": "驳回意见草稿，不超过200字"
    }
  },
  "required": ["draft_comment"]
}
```

输出示例：

```json
{
  "draft_comment": "经审核，您提交的「挑战杯一等奖证书.jpg」匹配条款3.1.2（获国家级学科竞赛一等奖加3分），但材料获奖日期为2024年6月15日，不在本学年有效区间（2025-09-01至2026-08-31）内，该项不加分。请补充本学年内的竞赛证明材料后重新提交。"
}
```

使用流程：

```
辅导员点击"生成驳回意见"
        │
        ▼
   调 Step C → 返回 draft_comment
        │
        ▼
   前端将 draft_comment 填入审核意见输入框（可编辑）
        │
        ▼
   辅导员确认/修改后 → 调接口25（review/action, reviewAction=3, reviewComment=最终文本）
```

> **关键**：`draft_comment` 是草稿，**不直接入库**。辅导员必须在输入框中确认或修改后才提交，`ai_generated=1` 标记该意见经 AI 辅助起草但由人工确认。

兜底策略：

| 异常情况 | 处理 |
|---------|------|
| 模型超时/限流 | 返回 `{code:500, msg:"AI生成失败，请手动填写审核意见"}`，不阻塞审核流程 |
| JSON 解析失败 | 同上，降级为手动填写 |
| `draft_comment` 超过 200 字 | 代码层截断到 200 字并追加"…"，不报错 |

---

## 五、设计约束与合规说明

1. **AGENTS.md 第 5 条**：AI 只产出"建议"，`suggested_score` 不直接入库为最终分；`final_score` 由规则引擎（Step 3）经时间窗→互斥→封顶三步校验后产出。
2. **AGENTS.md 第 6 条**：所有大模型调用均定义严格 JSON 输出 Schema，代码层做空值/越界/解析失败兜底（各 Step 兜底策略见上文）。
3. **表名前缀**：统一使用 `assess_`，避免与若依系统表（`sys_`）混淆。
4. **权限标识**：统一三段式 `assess:模块:操作`，与若依 `@PreAuthorize` 注解配套。
5. **AI 服务封装**：所有大模型/OCR 调用统一封装在 `ai/` 包，禁止散落在业务 Controller/Service 中。
