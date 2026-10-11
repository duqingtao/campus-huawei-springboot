package com.ruoyi.assess.ai.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.assess.ai.FieldExtractService;
import com.ruoyi.assess.ai.client.JsonResponseUtil;
import com.ruoyi.assess.ai.client.MaaSClient;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.assess.ai.dto.MaterialFieldDto;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS字段结构化抽取服务实现(Step 1)
 * <p>
 * 从OCR文本中抽取结构化字段，逐字段JSON输出。
 * 解析失败的字段跳过不入库(AGENTS.md第6条合规)。
 * 降级：ai.enabled=false时返回空列表。
 *
 * @author ruoyi
 */
@Service
public class MaaSFieldExtractServiceImpl implements FieldExtractService
{
    private static final Logger log = LoggerFactory.getLogger(MaaSFieldExtractServiceImpl.class);

    /** 默认置信度(模型未返回单字段置信度时使用) */
    private static final BigDecimal DEFAULT_CONFIDENCE = new BigDecimal("0.80");

    /** 系统提示词 */
    private static final String SYSTEM_PROMPT =
        "从以下OCR识别文本中抽取证明材料的关键结构化字段。无法确定的字段填null，不要编造。" +
        "只输出JSON，不要输出其他内容。JSON格式如下：\n" +
        "{\n" +
        "  \"award_name\": \"竞赛或活动名称\",\n" +
        "  \"award_level\": \"国家级/省级/市级/校级/院级\",\n" +
        "  \"award_rank\": \"获奖等次，如一等奖\",\n" +
        "  \"award_date\": \"获奖日期YYYY-MM-DD\",\n" +
        "  \"organization\": \"发证单位\",\n" +
        "  \"participant_name\": \"获奖人或参与人姓名\",\n" +
        "  \"paper_title\": \"论文题目，如有\",\n" +
        "  \"journal_name\": \"期刊或会议名称，如有\",\n" +
        "  \"volunteer_hours\": \"志愿服务时长(数字)，如有\",\n" +
        "  \"extra_info\": \"其他补充信息\"\n" +
        "}";

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private MaaSClient maaSClient;

    @Override
    public List<MaterialFieldDto> extractFields(String ocrText, String materialType)
    {
        List<MaterialFieldDto> result = new ArrayList<>();

        if (!aiConfig.isEnabled())
        {
            log.info("AI功能未启用，字段抽取跳过");
            return result;
        }

        if (StringUtils.isEmpty(ocrText))
        {
            log.info("OCR文本为空，字段抽取跳过");
            return result;
        }

        // 构建用户输入
        String userContent = "OCR文本：" + ocrText + "\n材料类型提示：" + (StringUtils.isNotEmpty(materialType) ? materialType : "未知");

        // 调用大模型
        String response = maaSClient.chatCompletion(SYSTEM_PROMPT, userContent, aiConfig.getModel());
        if (StringUtils.isEmpty(response))
        {
            log.error("字段抽取：大模型返回空响应");
            return result;
        }

        // 解析JSON
        JSONObject json = JsonResponseUtil.parseJsonObject(response);
        if (json == null)
        {
            log.error("字段抽取：JSON解析失败, raw={}", response);
            return result;
        }

        // 逐字段提取(解析失败的字段跳过)
        extractField(result, json, "award_name", "string");
        extractField(result, json, "award_level", "string");
        extractField(result, json, "award_rank", "string");
        extractField(result, json, "award_date", "date");
        extractField(result, json, "organization", "string");
        extractField(result, json, "participant_name", "string");
        extractField(result, json, "paper_title", "string");
        extractField(result, json, "journal_name", "string");
        extractField(result, json, "volunteer_hours", "number");
        extractField(result, json, "extra_info", "string");

        log.info("字段抽取完成，共{}个字段", result.size());
        return result;
    }

    /**
     * 从JSON中提取单个字段，非null则加入结果列表
     */
    private void extractField(List<MaterialFieldDto> result, JSONObject json, String key, String type)
    {
        String value = JsonResponseUtil.getString(json, key);
        if (StringUtils.isEmpty(value) || "null".equals(value))
        {
            return;
        }

        MaterialFieldDto dto = new MaterialFieldDto();
        dto.setFieldKey(key);
        dto.setFieldValue(value);
        dto.setFieldType(type);
        dto.setConfidence(DEFAULT_CONFIDENCE);
        result.add(dto);
    }
}
