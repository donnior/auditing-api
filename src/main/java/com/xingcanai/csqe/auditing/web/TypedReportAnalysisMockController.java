package com.xingcanai.csqe.auditing.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xingcanai.csqe.auditing.config.TypedReportAnalysisMockProperties;
import com.xingcanai.csqe.auditing.mock.TypedReportAnalysisMockResponseProvider;

/**
 * 用于本地开发阶段查看/验证 TypedReportAnalyser 的 mock 响应。
 */
@RestController
@RequestMapping("/mock/typed-report-analysis")
public class TypedReportAnalysisMockController {

    @Autowired
    private TypedReportAnalysisMockProperties properties;

    @Autowired
    private TypedReportAnalysisMockResponseProvider mockResponseProvider;

    @GetMapping("")
    public Map<String, Object> getMockResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("mockEnabled", properties.isMockEnabled());
        result.put("mockResponseResource", properties.getMockResponseResource());
        result.put("response", mockResponseProvider.getMockResponse());
        return result;
    }
}

