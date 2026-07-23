package com.xingcanai.csqe.auditing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "sales-speech-analysis")
public class SalesSpeechAnalysisProperties {

    private String promptVersion = "v1.3.0";
    private String promptResource = "classpath:prompts/sales-speech-analysis-v1.3.0.md";
    private Duration staleAfter = Duration.ofMinutes(20);
    private String zoneId = "Asia/Shanghai";
    private int executorConcurrency = 2;
    private int executorQueueCapacity = 20;
}
