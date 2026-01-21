package com.xingcanai.csqe.auditing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 本地开发阶段用于跳过真实 LLM/Coze 调用的开关配置。
 */
@Component
@ConfigurationProperties(prefix = "auditing.report-analysis")
@Data
public class TypedReportAnalysisMockProperties {

    /**
     * 是否启用 mock（启用后将不会调用真实第三方 API）。
     */
    private boolean mockEnabled = false;

    /**
     * mock 响应文本所在的 classpath 资源路径。
     */
    private String mockResponseResource = "mock/typed_report_analysis_response.txt";
}

