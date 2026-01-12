package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.EmployeeAnalysisReport;
import com.xingcanai.csqe.auditing.entity.EmployeeAnalysisReportRepository;
import com.xingcanai.csqe.common.XCPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee-analysis-report")
public class EmployeeAnalysisReportController {

    @Autowired
    private EmployeeAnalysisReportRepository employeeAnalysisReportRepository;

    @GetMapping("")
    public Page<EmployeeAnalysisReport> getDailyAnalysisList(XCPageRequest pageRequest) {
        Page<EmployeeAnalysisReport> page = employeeAnalysisReportRepository.findAll(pageRequest.toPageRequest());

        return page;
    }

    @GetMapping("/{id}")
    public EmployeeAnalysisReport getAnalysisReport(@PathVariable String id) {
        return employeeAnalysisReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分析记录不存在"));
    }
}
