package com.xingcanai.csqe.auditing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class SalesSpeechAnalysisAsyncConfig {

    @Bean(name = "salesSpeechAnalysisExecutor")
    public ThreadPoolTaskExecutor salesSpeechAnalysisExecutor(SalesSpeechAnalysisProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getExecutorConcurrency());
        executor.setMaxPoolSize(properties.getExecutorConcurrency());
        executor.setQueueCapacity(properties.getExecutorQueueCapacity());
        executor.setThreadNamePrefix("sales-speech-analysis-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}

