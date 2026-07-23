package com.xingcanai.csqe.llm.supports.deepseek;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "llm.provider.deepseek")
public class DeepSeekProperties {

    private String baseUrl = "https://api.deepseek.com";
    private String apiKey = "";
    private String model = "deepseek-v4-pro";
    private boolean thinkingEnabled = true;
    private String reasoningEffort = "high";
    private double temperature = 0.2;
    private int maxTokens = 32768;
    private Duration timeout = Duration.ofMinutes(15);
    private int maxRetries = 2;
    private Duration retryDelay = Duration.ofSeconds(1);
}

