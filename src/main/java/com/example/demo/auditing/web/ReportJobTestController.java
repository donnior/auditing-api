package com.example.demo.auditing.web;

import com.example.demo.auditing.service.ChatAnalysisService;
import com.example.demo.auditing.service.ChatDailyAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/report-job-test")
public class ReportJobTestController {

    @Autowired
    private ChatAnalysisService chatAnalysisService;

    @Autowired
    private ChatDailyAnalysisService chatDailyAnalysisService;

    /**
     * 员工列表（默认按id倒序）
     */
    @GetMapping("")
    public void testReportJob() {
        chatAnalysisService.runWeeklyAnalysis();
    }

    @GetMapping("/daily")
    public void testDailyReportJob() {
        chatDailyAnalysisService.runAnalysis();
    }

}
