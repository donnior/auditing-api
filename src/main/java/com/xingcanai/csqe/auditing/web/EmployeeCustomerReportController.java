package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.EmployeeCustomerChatAnalysis;
import com.xingcanai.csqe.auditing.entity.EmployeeCustomerChatAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 员工分析结果查看接口
 */
@RestController
@RequestMapping("/employee-customer-reports")
@Slf4j
public class EmployeeCustomerReportController {

    // @Autowired
    // private EmployeeDailyAnalysisRepository employeeDailyAnalysisRepository;

    @Autowired
    private EmployeeCustomerChatAnalysisRepository employeeCustomerAnalysisRepository;

    /**
     * 获取员工与客户聊天分析明细列表
     */
    @GetMapping("")
    public List<EmployeeCustomerChatAnalysis> getCustomerChatAnalysisList(
            @RequestParam String qwid,
            @RequestParam(name = "start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(name = "end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {

        // ZonedDateTime cycleStart = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        // ZonedDateTime cycleEnd = endDate.withHour(23).withMinute(59).withSecond(59).withNano(0);
        var cycleStart = startDate;
        var cycleEnd = endDate;

        log.info("getCustomerChatAnalysisList qwid: {}, startDate: {}, endDate: {}", qwid, cycleStart, cycleEnd);

        return employeeCustomerAnalysisRepository.findByQwAccountIdAndCycleStartTimeAndCycleEndTime(
                qwid, cycleStart, cycleEnd);
    }

    /**
     * 获取员工与特定客户的聊天分析详情
     */
    @GetMapping("/{analysisId}")
    public EmployeeCustomerChatAnalysis getCustomerChatAnalysisDetail(@PathVariable String analysisId) {
        return employeeCustomerAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("分析记录不存在"));
    }
}
