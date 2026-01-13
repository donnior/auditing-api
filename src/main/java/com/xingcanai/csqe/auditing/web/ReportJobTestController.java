package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.service.WeeklyChatAnalysisService;
import com.xingcanai.csqe.auditing.service.DailyChatAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/report-job-test")
public class ReportJobTestController {

    @Autowired
    private WeeklyChatAnalysisService weeklyChatAnalysisService;

    @Autowired
    private DailyChatAnalysisService dailyChatAnalysisService;

    /**
     * 员工列表（默认按id倒序）
     */
    @GetMapping("")
    public void testReportJob() {
        weeklyChatAnalysisService.runAnalysis();
    }

    @GetMapping("/daily")
    public void testDailyReportJob() {
        dailyChatAnalysisService.runAnalysis();
    }

}
