package com.example.demo.auditing.web;

import com.example.demo.auditing.entity.EmployeeDailyAnalysis;
import com.example.demo.auditing.entity.EmployeeDailyAnalysisRepository;
import com.example.demo.common.XCPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 员工分析结果查看接口
 */
@RestController
@RequestMapping("/employee-analysis")
public class EmployeeAnalysisController {

    @Autowired
    private EmployeeDailyAnalysisRepository employeeDailyAnalysisRepository;


    /**
     * 获取员工每日分析详情
     */
    @GetMapping("/daily/{analysisId}")
    public EmployeeDailyAnalysis getDailyAnalysisDetail(@PathVariable String analysisId) {
        return employeeDailyAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("分析记录不存在"));
    }



    /**
     * 获取总体统计概览
     */
    @GetMapping("/overview")
    public Map<String, Object> getAnalysisOverview() {
        Object[] stats = employeeDailyAnalysisRepository.getAllOverviewStatistics();

        Map<String, Object> overview = new HashMap<>();
        if (stats != null && stats.length > 0) {
            overview.put("totalEmployees", stats[0]);
            overview.put("avgQualityScore", stats[1]);
            overview.put("totalViolations", stats[2]);
            overview.put("avgSatisfaction", stats[3]);
        } else {
            overview.put("totalEmployees", 0);
            overview.put("avgQualityScore", 0.0);
            overview.put("totalViolations", 0);
            overview.put("avgSatisfaction", 0.0);
        }

        return overview;
    }

    /**
     * 获取员工服务质量排名
     */
    @GetMapping("/ranking")
    public List<EmployeeDailyAnalysis> getServiceQualityRanking(
            @RequestParam(defaultValue = "20") int limit) {

        Page<EmployeeDailyAnalysis> page = employeeDailyAnalysisRepository
                .findAllOrderByServiceQualityScoreDesc(PageRequest.of(0, limit));

        return page.getContent();
    }
}
