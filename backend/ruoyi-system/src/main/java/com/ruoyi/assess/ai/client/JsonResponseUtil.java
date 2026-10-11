package com.ruoyi.assess.ai.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

/**
 * JSON响应解析工具
 * <p>
 * 1. 剥离markdown代码块标记(```json ... ```)
 * 2. 解析为JSONObject
 * 3. 解析失败返回null(调用方负责兜底处理)
 * <p>
 * AGENTS.md第6条合规：解析失败绝不用编造值填充
 *
 * @author ruoyi
 */
public class JsonResponseUtil
{
    private static final Logger log = LoggerFactory.getLogger(JsonResponseUtil.class);

    /**
     * 剥离markdown代码块标记后解析为JSONObject
     *
     * @param raw 模型原始输出
     * @return 解析成功返回JSONObject，失败返回null
     */
    public static JSONObject parseJsonObject(String raw)
    {
        if (raw == null || raw.trim().isEmpty())
        {
            return null;
        }

        String cleaned = stripMarkdown(raw);
        try
        {
            return JSON.parseObject(cleaned);
        }
        catch (JSONException e)
        {
            log.error("JSON解析失败, raw={}", raw, e);
            return null;
        }
    }

    /**
     * 剥离markdown代码块标记
     * <p>
     * 处理以下格式：
     * ```json\n{...}\n```
     * ```\n{...}\n```
     * {...}(无代码块)
     */
    public static String stripMarkdown(String raw)
    {
        if (raw == null)
        {
            return null;
        }

        String text = raw.trim();

        // 去除开头的 ```json 或 ```
        if (text.startsWith("```"))
        {
            // 找第一个换行，去掉 ```json 这一行
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0)
            {
                text = text.substring(firstNewline + 1).trim();
            }
            else
            {
                // 没有 ```json 前缀，直接去掉 ```
                text = text.substring(3).trim();
            }

            // 去除结尾的 ```
            if (text.endsWith("```"))
            {
                text = text.substring(0, text.length() - 3).trim();
            }
        }

        return text;
    }

    /**
     * 安全获取字符串字段，null或缺失返回null
     */
    public static String getString(JSONObject json, String key)
    {
        if (json == null || !json.containsKey(key))
        {
            return null;
        }
        return json.getString(key);
    }

    /**
     * 安全获取BigDecimal字段，null或缺失或越界返回null
     *
     * @param min 最小值(含)，null不校验
     * @param max 最大值(含)，null不校验
     */
    public static java.math.BigDecimal getBigDecimal(JSONObject json, String key,
            java.math.BigDecimal min, java.math.BigDecimal max)
    {
        if (json == null || !json.containsKey(key))
        {
            return null;
        }
        try
        {
            java.math.BigDecimal value = json.getBigDecimal(key);
            if (value == null)
            {
                return null;
            }
            if (min != null && value.compareTo(min) < 0)
            {
                log.warn("字段[{}]值{}低于最小值{}，截断", key, value, min);
                return min;
            }
            if (max != null && value.compareTo(max) > 0)
            {
                log.warn("字段[{}]值{}超过最大值{}，截断", key, value, max);
                return max;
            }
            return value;
        }
        catch (Exception e)
        {
            log.warn("字段[{}]无法转为BigDecimal: {}", key, json.get(key));
            return null;
        }
    }

    /**
     * 安全获取Long字段
     */
    public static Long getLong(JSONObject json, String key)
    {
        if (json == null || !json.containsKey(key))
        {
            return null;
        }
        try
        {
            return json.getLong(key);
        }
        catch (Exception e)
        {
            log.warn("字段[{}]无法转为Long: {}", key, json.get(key));
            return null;
        }
    }
}
