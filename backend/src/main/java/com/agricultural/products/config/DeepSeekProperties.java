package com.agricultural.products.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /** API 密钥 */
    private String apiKey;

    /** API 基础地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 最大生成 token 数 */
    private int maxTokens = 1024;

    /** 温度参数（0-2，越高越随机） */
    private double temperature = 0.7;

    /** 请求超时时间（秒） */
    private int timeoutSeconds = 30;
}
