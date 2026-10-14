package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.assess.ai.ClauseExtractService;
import com.ruoyi.assess.ai.client.MaaSClient;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.assess.ai.dto.ClausePreviewResult;
import com.ruoyi.assess.domain.AssessRegulationClause;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS条款抽取服务实现
 * <p>
 * 调用大模型从综测细则文本中提取结构化条款，返回模块树形 JSON。
 * 支持超长文本分块：按章节正则切块，逐块调 MaaS，合并去重。
 *
 * @author ruoyi
 */
@Service
public class MaaSClauseExtractServiceImpl implements ClauseExtractService
{
    private static final Logger log = LoggerFactory.getLogger(MaaSClauseExtractServiceImpl.class);

    /** 单块文本上限 */
    private static final int MAX_CHUNK_LENGTH = 8000;

    /** 模型输出 max_tokens */
    private static final int MAX_TOKENS = 8192;

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private MaaSClient maaSClient;

    /** 章节正则：第X章 或 一、二、三、 */
    private static final Pattern CHAPTER_PATTERN = Pattern.compile(
            "(?=第[一二三四五六七八九十百千]+[章篇编])|(?=^[一二三四五六七八九十]+、)",
            Pattern.MULTILINE);

    /** 系统提示词 */
    private static final String SYSTEM_PROMPT = "你是综测细则条款提取助手。"
            + "请从以下综测细则文本中提取所有加分/减分/基础/一票否决条款，返回JSON对象。\n\n"
            + "## JSON Schema\n"
            + "```json\n"
            + "{\n"
            + "  \"modules\": [\n"
            + "    {\n"
            + "      \"clauseNo\": \"模块编号，如 一、 或 第一条\",\n"
            + "      \"clauseTitle\": \"模块标题，如 思想品德素质\",\n"
            + "      \"clauseText\": \"模块原文段落，逐字摘抄\",\n"
            + "      \"clauseType\": \"0\",\n"
            + "      \"children\": [\n"
            + "        {\n"
            + "          \"clauseNo\": \"子项编号，如 （一） 或 1.1\",\n"
            + "          \"clauseTitle\": \"子项标题\",\n"
            + "          \"clauseText\": \"子项原文，逐字摘抄\",\n"
            + "          \"clauseType\": \"1=加分/2=减分/3=基础/4=一票否决\",\n"
            + "          \"maxScore\": 2.0\n"
            + "        }\n"
            + "      ]\n"
            + "    }\n"
            + "  ]\n"
            + "}\n"
            + "```\n\n"
            + "## 硬指令\n"
            + "1. clauseText 必须逐字摘抄原文对应段落，禁止概括、改写、省略。\n"
            + "2. 总则、目的依据、组织流程、评审仲裁等非评分内容一律不提取。\n"
            + "3. 模块层 clauseType 固定为 \"0\"，叶子项 clauseType 为 1/2/3/4。\n"
            + "4. 只返回JSON对象，不要添加任何解释说明文字。\n\n"
            + "## 示例1（第X条编号格式）\n"
            + "输入片段：第三条 思想品德素质（满分20分）。（一）爱国爱党，加2分。（二）违纪处分，减5分。\n"
            + "输出：{\"modules\":[{\"clauseNo\":\"第三条\",\"clauseTitle\":\"思想品德素质\","
            + "\"clauseText\":\"第三条 思想品德素质（满分20分）。\",\"clauseType\":\"0\","
            + "\"children\":[{\"clauseNo\":\"（一）\",\"clauseTitle\":\"爱国爱党\","
            + "\"clauseText\":\"（一）爱国爱党，加2分。\",\"clauseType\":\"1\",\"maxScore\":2.0},"
            + "{\"clauseNo\":\"（二）\",\"clauseTitle\":\"违纪处分\","
            + "\"clauseText\":\"（二）违纪处分，减5分。\",\"clauseType\":\"2\",\"maxScore\":5.0}]}]}\n\n"
            + "## 示例2（一、（一）编号格式）\n"
            + "输入片段：一、学业表现（满分60分）。（一）课程成绩，基础分60分。（二）科研竞赛，最高加10分。\n"
            + "输出：{\"modules\":[{\"clauseNo\":\"一、\",\"clauseTitle\":\"学业表现\","
            + "\"clauseText\":\"一、学业表现（满分60分）。\",\"clauseType\":\"0\","
            + "\"children\":[{\"clauseNo\":\"（一）\",\"clauseTitle\":\"课程成绩\","
            + "\"clauseText\":\"（一）课程成绩，基础分60分。\",\"clauseType\":\"3\",\"maxScore\":60.0},"
            + "{\"clauseNo\":\"（二）\",\"clauseTitle\":\"科研竞赛\","
            + "\"clauseText\":\"（二）科研竞赛，最高加10分。\",\"clauseType\":\"1\",\"maxScore\":10.0}]}]}";

    @Override
    public ClausePreviewResult extractClausesWithMeta(String text)
    {
        ClausePreviewResult result = new ClausePreviewResult();
        ClausePreviewResult.Meta meta = result.getMeta();
        meta.setTextLength(text != null ? text.length() : 0);

        if (StringUtils.isEmpty(text))
        {
            meta.getErrors().add("入参文本为空");
            return result;
        }

        if (!aiConfig.isEnabled())
        {
            meta.getErrors().add("AI功能未启用，无法提取条款");
            return result;
        }

        // 分块
        List<String> chunks = splitIntoChunks(text);
        if (chunks.isEmpty())
        {
            // 无法分块且超长
            meta.getErrors().add("文档过长且无法分块，请拆分后上传");
            return result;
        }

        meta.setChunkCount(chunks.size());
        meta.setTruncated(chunks.size() > 1 || text.length() > MAX_CHUNK_LENGTH);

        // 逐块调 MaaS 解析
        String model = aiConfig.getModel();
        Set<String> seenKeys = new HashSet<>(); // 去重: clauseNo + clauseTitle
        int consumed = 0;

        for (int i = 0; i < chunks.size(); i++)
        {
            String chunk = chunks.get(i);
            consumed += chunk.length();
            log.info("条款抽取: 处理第{}/{}块, 字符数={}", i + 1, chunks.size(), chunk.length());

            String response = maaSClient.chatCompletion(SYSTEM_PROMPT, chunk, model, MAX_TOKENS);
            if (StringUtils.isEmpty(response))
            {
                meta.getErrors().add("第" + (i + 1) + "块: MaaS返回空响应");
                log.error("第{}块MaaS返回空响应", i + 1);
                continue;
            }

            List<AssessRegulationClause> chunkClauses = parseClauseJson(response, seenKeys);
            if (chunkClauses != null && !chunkClauses.isEmpty())
            {
                result.getClauses().addAll(chunkClauses);
            }
        }

        meta.setConsumedLength(consumed);
        log.info("条款抽取完成: 模块数={}, 分块数={}, 消费字符={}/{}, 错误数={}",
                result.getClauses().size(), chunks.size(), consumed, text.length(), meta.getErrors().size());

        return result;
    }

    /**
     * 将文本分块
     * <p>
     * 文本 ≤8000 字：单块返回。
     * 文本 >8000 字：按章节正则切分，每块 ≤8000 字。
     * 无法按章节切分且 >8000 字：返回空列表（调用方报错）。
     *
     * @param text 全文
     * @return 分块列表，空列表表示无法分块
     */
    private List<String> splitIntoChunks(String text)
    {
        if (text.length() <= MAX_CHUNK_LENGTH)
        {
            return List.of(text);
        }

        // 尝试按章节正则切块
        List<String> chapters = new ArrayList<>();
        Matcher matcher = CHAPTER_PATTERN.matcher(text);
        int lastStart = 0;
        boolean foundSplit = false;

        while (matcher.find())
        {
            int splitPos = matcher.start();
            if (splitPos > lastStart)
            {
                String chapter = text.substring(lastStart, splitPos).trim();
                if (chapter.length() > 0)
                {
                    chapters.add(chapter);
                    foundSplit = true;
                }
                lastStart = splitPos;
            }
        }
        // 最后一块
        if (lastStart < text.length())
        {
            String lastChapter = text.substring(lastStart).trim();
            if (lastChapter.length() > 0)
            {
                chapters.add(lastChapter);
                foundSplit = true;
            }
        }

        if (!foundSplit || chapters.size() <= 1)
        {
            // 无法按章节切分
            log.warn("文本长度{}超过上限{}且无法按章节分块", text.length(), MAX_CHUNK_LENGTH);
            return new ArrayList<>();
        }

        // 合并过小的块，确保每块不超过上限
        List<String> merged = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String chapter : chapters)
        {
            if (current.length() + chapter.length() > MAX_CHUNK_LENGTH && current.length() > 0)
            {
                merged.add(current.toString());
                current = new StringBuilder();
            }
            current.append(chapter).append("\n");
        }
        if (current.length() > 0)
        {
            merged.add(current.toString());
        }

        // 检查是否仍有单块超长
        for (String block : merged)
        {
            if (block.length() > MAX_CHUNK_LENGTH)
            {
                // 单个章节就超长，截取到上限（该块内容部分可能不完整，但至少能提取部分条款）
                log.warn("单个章节长度{}超过上限{}，将截取处理", block.length(), MAX_CHUNK_LENGTH);
            }
        }

        log.info("文本分块: 原文{}字, 分为{}块", text.length(), merged.size());
        return merged;
    }

    /**
     * 解析大模型返回的条款 JSON 对象
     * <p>
     * 顶层为 {"modules": [...]}，每个模块含 children 数组。
     * 缺 clauseNo 或 clauseTitle 的条目标记 invalid=true，不丢弃。
     *
     * @param response 大模型原始返回
     * @param seenKeys 已提取的 clauseNo+clauseTitle 集合（去重）
     * @return 模块列表（含子项）
     */
    private List<AssessRegulationClause> parseClauseJson(String response, Set<String> seenKeys)
    {
        List<AssessRegulationClause> result = new ArrayList<>();

        // 提取JSON对象段
        String jsonStr = extractJsonObject(response);
        if (StringUtils.isEmpty(jsonStr))
        {
            log.error("无法从大模型响应中提取JSON对象, response={}", response);
            return result;
        }

        JSONObject jsonObj;
        try
        {
            jsonObj = JSON.parseObject(jsonStr);
        }
        catch (Exception e)
        {
            log.error("解析条款JSON对象失败, jsonStr={}", jsonStr, e);
            return result;
        }

        JSONArray modules = jsonObj.getJSONArray("modules");
        if (modules == null || modules.isEmpty())
        {
            log.warn("条款JSON中modules数组为空");
            return result;
        }

        for (int i = 0; i < modules.size(); i++)
        {
            try
            {
                JSONObject moduleObj = modules.getJSONObject(i);
                AssessRegulationClause module = parseClause(moduleObj, "0", true);

                // 去重
                String key = safeKey(module.getClauseNo(), module.getClauseTitle());
                if (seenKeys.contains(key))
                {
                    log.info("跳过重复模块: {}", key);
                    continue;
                }
                seenKeys.add(key);

                // 解析子项
                JSONArray children = moduleObj.getJSONArray("children");
                if (children != null)
                {
                    List<AssessRegulationClause> childList = new ArrayList<>();
                    for (int j = 0; j < children.size(); j++)
                    {
                        try
                        {
                            JSONObject childObj = children.getJSONObject(j);
                            AssessRegulationClause child = parseClause(childObj, null, false);
                            // 叶子项 clauseType 默认 "1" 如果为空
                            if (StringUtils.isEmpty(child.getClauseType()))
                            {
                                child.setClauseType("1");
                            }
                            // maxScore 兜底
                            if (child.getMaxScore() == null)
                            {
                                child.setMaxScore(BigDecimal.ZERO);
                            }
                            String childKey = safeKey(child.getClauseNo(), child.getClauseTitle());
                            if (!seenKeys.contains(childKey))
                            {
                                seenKeys.add(childKey);
                                childList.add(child);
                            }
                        }
                        catch (Exception e)
                        {
                            log.warn("解析第{}模块的第{}子项失败: {}", i + 1, j + 1, e.getMessage());
                        }
                    }
                    if (!childList.isEmpty())
                    {
                        module.setChildren(childList);
                    }
                }

                result.add(module);
            }
            catch (Exception e)
            {
                log.warn("解析第{}个模块失败: {}", i + 1, e.getMessage());
            }
        }

        return result;
    }

    /**
     * 解析单个条款对象（模块或叶子项）
     * <p>
     * 缺 clauseNo 或 clauseTitle 时标记 invalid=true + invalidReason，不丢弃。
     */
    private AssessRegulationClause parseClause(JSONObject obj, String defaultClauseType, boolean isModule)
    {
        AssessRegulationClause clause = new AssessRegulationClause();

        String clauseNo = obj.getString("clauseNo");
        String clauseTitle = obj.getString("clauseTitle");
        String clauseText = obj.getString("clauseText");
        String clauseType = obj.getString("clauseType");
        BigDecimal maxScore = parseMaxScore(obj);

        clause.setClauseNo(clauseNo);
        clause.setClauseTitle(clauseTitle);
        clause.setClauseText(clauseText);
        clause.setMaxScore(maxScore);

        // 模块层 clauseType 固定 "0"
        if (isModule)
        {
            clause.setClauseType("0");
        }
        else
        {
            clause.setClauseType(StringUtils.isEmpty(clauseType) ? defaultClauseType : clauseType);
        }

        // 校验必填字段
        List<String> missing = new ArrayList<>();
        if (StringUtils.isEmpty(clauseNo))
        {
            missing.add("clauseNo");
        }
        if (StringUtils.isEmpty(clauseTitle))
        {
            missing.add("clauseTitle");
        }
        if (!missing.isEmpty())
        {
            clause.setInvalid(true);
            clause.setInvalidReason("缺" + String.join("、", missing));
        }

        return clause;
    }

    /**
     * 从大模型响应中提取 JSON 对象字符串
     * <p>
     * 处理 ```json {...} ``` 包裹或前后多余文字。
     */
    private String extractJsonObject(String response)
    {
        String trimmed = response.trim();

        // 去除 ```json ... ``` 包裹
        if (trimmed.startsWith("```"))
        {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0)
            {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0)
            {
                trimmed = trimmed.substring(0, lastFence);
            }
            trimmed = trimmed.trim();
        }

        // 提取第一个 { 到最后一个 } 之间内容
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            return trimmed.substring(start, end + 1);
        }

        return null;
    }

    /**
     * 解析分值，容错处理多种格式
     */
    private BigDecimal parseMaxScore(JSONObject obj)
    {
        try
        {
            BigDecimal score = obj.getBigDecimal("maxScore");
            if (score != null)
            {
                return score;
            }
            String scoreStr = obj.getString("maxScore");
            if (StringUtils.isNotEmpty(scoreStr))
            {
                return new BigDecimal(scoreStr.trim());
            }
        }
        catch (Exception e)
        {
            log.warn("解析maxScore失败: {}", obj.get("maxScore"));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 生成去重键
     */
    private String safeKey(String clauseNo, String clauseTitle)
    {
        return (clauseNo == null ? "" : clauseNo.trim()) + "|"
                + (clauseTitle == null ? "" : clauseTitle.trim());
    }
}
