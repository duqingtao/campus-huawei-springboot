# 综测智办项目开发规则（所有代码生成必须遵守）

- 后端：若依 RuoYi（Spring Boot + MyBatis），分层 Controller / Service / ServiceImpl / Mapper
- 前端：若依自带 Vue2 + Element UI，接口放 src/api/业务名/，页面放 src/views/业务名/
- 数据库：MySQL，表名用英文小写+下划线，必须有 create_by/create_time/update_by/update_time/del_flag
- AI 服务：华为云盘古大模型 MaaS + OCR，统一封装在 ai/ 包，禁止散落在业务代码中

1. 最小化改动：只改当前任务涉及的文件，禁止顺手重构、升级依赖、调整无关格式
2. 后端返回统一用 AjaxResult；实体继承 BaseEntity；新增/修改接口写 @Log 操作日志和 @PreAuthorize 权限注解
3. 列表页必须含：序号列、ID 列、行级操作按钮（编辑/删除/详情），不做工具栏批量按钮
4. 所有中文提示，Java 字段小驼峰，SQL 字段下划线，字段必须有 COMMENT
5. AI 只产出"建议"：任何分值结果必须携带 evidence（条款编号+条款原文+置信度），
   最终入库分值由规则引擎计算，禁止直接采信大模型给出的分数
6. 大模型调用必须定义严格 JSON 输出 Schema，代码里做空值/越界/解析失败兜底
7. 写代码前先简述改动文件清单和方案，经我确认后再动手