package com.ruoyi.assess.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI服务配置
 * <p>
 * 对应 application.yml 中 assess.ai 配置段。
 * api-key 从配置读取，不硬编码在代码中。
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "assess.ai")
public class AiConfig
{
    /** AI总开关(false=降级走Mock) */
    private boolean enabled = false;

    /** MaaS基础地址 */
    private String baseUrl = "https://api.modelarts-maas.com/v1";

    /** API Key(从配置读取，禁止硬编码) */
    private String apiKey = "";

    /** 文本模型(匹配/抽取/草稿) */
    private String model = "qwen2.5-vl-72b";

    /** 视觉模型(图片OCR识别) */
    private String vlmModel = "qwen2.5-vl-72b";

    /**
     * RestTemplate Bean，超时30秒
     */
    @Bean("aiRestTemplate")
    public RestTemplate aiRestTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getVlmModel()
    {
        return vlmModel;
    }

    public void setVlmModel(String vlmModel)
    {
        this.vlmModel = vlmModel;
    }
}
