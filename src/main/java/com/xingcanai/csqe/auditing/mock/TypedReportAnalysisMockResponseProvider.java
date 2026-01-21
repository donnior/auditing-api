package com.xingcanai.csqe.auditing.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.xingcanai.csqe.auditing.config.TypedReportAnalysisMockProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * TypedReportAnalyser 的 mock 响应提供器。
 * <p>
 * 从 classpath 资源读取 mock 文本，并进行简单缓存，避免每次请求重复 IO。
 */
@Component
@Slf4j
public class TypedReportAnalysisMockResponseProvider {

    private final TypedReportAnalysisMockProperties properties;
    private volatile String cached;

    public TypedReportAnalysisMockResponseProvider(TypedReportAnalysisMockProperties properties) {
        this.properties = properties;
    }

    public String getMockResponse() {
        var local = cached;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cached != null) {
                return cached;
            }
            cached = loadFromClasspath(properties.getMockResponseResource());
            return cached;
        }
    }

    private String loadFromClasspath(String resourcePath) {
        try {
            var resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.warn("Mock response resource not found: {}", resourcePath);
                return fallbackMockResponse();
            }
            try (var in = resource.getInputStream()) {
                var text = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
                if (text == null || text.isBlank()) {
                    log.warn("Mock response resource is empty: {}", resourcePath);
                    return fallbackMockResponse();
                }
                return text;
            }
        } catch (IOException e) {
            log.warn("Failed to load mock response resource: {}", resourcePath, e);
            return fallbackMockResponse();
        }
    }

    /**
     * 兜底的 mock 文本（确保解析器至少能正常解析）。
     */
    private String fallbackMockResponse() {
        return String.join("\n",
            "完成资料发送：是",
            "完成到课提醒：是",
            "完成课后作业发布：是",
            "下周资料发送：否",
            "完成课后学习感受追踪：是",
            "周日螳螂销转链接：否",
            "风险词触发：否",
            "完成看课指导：是",
            "完成老师介绍：是",
            "完成课表介绍：是",
            "完成上课时间介绍：是",
            "索要订单号并核对：否"
        );
    }
}
