package com.example.demo.auditing.web;

import com.example.demo.auditing.entity.Employee;
import com.example.demo.auditing.entity.EmployeeRepository;
import com.example.demo.auditing.entity.WeeklyReportSummary;
import com.example.demo.auditing.entity.WeeklyReportSummaryRepository;
import com.example.demo.common.XCPageRequest;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/weekly-report-summaries")
public class WeeklyReportSummaryController {

    @Autowired
    private WeeklyReportSummaryRepository weeklyReportSummaryRepository;

    /**
     * 员工列表（默认按id倒序）
     */
    @GetMapping("")
    public Page<WeeklyReportSummary> listWeeklyReportSummaries(XCPageRequest pageRequest) {
        var req = pageRequest.toPageRequest().withSort(
            Sort.by("evalPeriod").descending()
            .and(Sort.by("evalType"))
            .and(Sort.by("employeeId")));
        return weeklyReportSummaryRepository.findAll(req);
    }

    @GetMapping("/{id}")
    public WeeklyReportSummary getWeeklyReportSummary(@PathVariable String id) {
        return weeklyReportSummaryRepository.findById(id).orElseThrow(() -> new RuntimeException("Weekly report summary not found"));
    }



}
