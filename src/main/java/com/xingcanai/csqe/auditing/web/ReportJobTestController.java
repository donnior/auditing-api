package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.service.WeeklyChatAnalysisServiceV2;
import com.xingcanai.csqe.util.Strings;
import com.xingcanai.csqe.auditing.service.DailyChatAnalysisServiceV2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/report-job-test")
public class ReportJobTestController {

    @Autowired
    private WeeklyChatAnalysisServiceV2 weeklyChatAnalysisService;

    @Autowired
    private DailyChatAnalysisServiceV2 dailyChatAnalysisService;

    /**
     * 员工列表（默认按id倒序）
     */
    @GetMapping("")
    public void testReportJob() {
        weeklyChatAnalysisService.runAnalysis();
    }

    @GetMapping("/daily")
    public void testDailyReportJob(@RequestParam(required = false, name = "target_date") String targetDate) {
        if(Strings.isNotEmpty(targetDate)) {
            dailyChatAnalysisService.runAnalysis(targetDate);
        } else {
            dailyChatAnalysisService.runAnalysis();
        }
    }

}
