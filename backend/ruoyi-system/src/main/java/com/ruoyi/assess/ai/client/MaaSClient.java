package com.ruoyi.assess.ai.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.assess.ai.config.AiConfig;
import com.ruoyi.common.utils.StringUtils;

/**
 * MaaS统一HTTP客户端
 * <p>
 * OpenAI兼容协议，POST {base-url}/chat/completions。
 * 支持纯文本对话和图片VLM对话两种模式。
 * 超时30s(由AiConfig中RestTemplate配置)。
 * <p>
 * 所有大模型调用统一经过此类，禁止散落到业务代码。
 *
 * @author ruoyi
 */
@Component
public class MaaSClient
{
    private static final Logger log = LoggerFactory.getLogger(MaaSClient.class);

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    @Qualifier("aiRestTemplate")
    private RestTemplate restTemplate;

    /**
     * 纯文本对话
     *
     * @param systemPrompt 系统提示词
     * @param userContent  用户输入
     * @param model        模型名称
     * @return 模型回复内容，失败返回null
     */
    public String chatCompletion(String systemPrompt, String userContent, String model)
    {
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userContent);
        messages.add(userMsg);

        return doChatCompletion(messages, model);
    }

    /**
     * 图片VLM对话
     *
     * @param systemPrompt 系统提示词
     * @param userContent  用户文本输入
     * @param base64Image  图片base64编码(不含data:前缀)
     * @param mimeType     图片MIME类型(如 image/png)
     * @param model        VLM模型名称
     * @return 模型回复内容，失败返回null
     */
    public String chatCompletionWithImage(String systemPrompt, String userContent,
            String base64Image, String mimeType, String model)
    {
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // user消息含text+image_url两部分
        List<Map<String, Object>> contentParts = new ArrayList<>();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", userContent);
        contentParts.add(textPart);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:" + mimeType + ";base64," + base64Image);
        imagePart.put("image_url", imageUrl);
        contentParts.add(imagePart);

        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", contentParts);
        messages.add(userMsg);

        return doChatCompletion(messages, model);
    }

    /**
     * 执行chat/completions请求
     */
    private String doChatCompletion(List<Map<String, Object>> messages, String model)
    {
        String apiKey = aiConfig.getApiKey();
        if (StringUtils.isEmpty(apiKey))
        {
            log.error("MaaS api-key未配置，无法调用大模型");
            return null;
        }

        String url = aiConfig.getBaseUrl() + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 4096);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);

        try
        {
            String response = restTemplate.postForObject(url, entity, String.class);
            if (StringUtils.isEmpty(response))
            {
                log.error("MaaS返回空响应, url={}", url);
                return null;
            }
            return extractContent(response);
        }
        catch (Exception e)
        {
            log.error("MaaS调用失败, url={}, model={}", url, model, e);
            return null;
        }
    }

    /**
     * 从OpenAI兼容响应中提取choices[0].message.content
     */
    private String extractContent(String response)
    {
        try
        {
            JSONObject json = JSON.parseObject(response);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty())
            {
                log.error("MaaS响应无choices: {}", response);
                return null;
            }
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            if (message == null)
            {
                log.error("MaaS响应无message: {}", response);
                return null;
            }
            String content = message.getString("content");
            if (StringUtils.isEmpty(content))
            {
                log.error("MaaS响应content为空: {}", response);
                return null;
            }
            return content.trim();
        }
        catch (Exception e)
        {
            log.error("解析MaaS响应失败: {}", response, e);
            return null;
        }
    }
}
