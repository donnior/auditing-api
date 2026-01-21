package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.WeeklyReportSummary;
import com.xingcanai.csqe.auditing.entity.WeeklyReportSummaryRepository;
import com.xingcanai.csqe.auditing.entity.WeeklyReportSummarySpec;
import com.xingcanai.csqe.auditing.entity.EvaluationDetail;
import com.xingcanai.csqe.auditing.entity.EvaluationDetailRepository;
import com.xingcanai.csqe.auditing.entity.EvaluationDetailSpec;
import com.xingcanai.csqe.common.XCPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/weekly-report-summaries")
public class WeeklyReportSummaryController {

    @Autowired
    private WeeklyReportSummaryRepository weeklyReportSummaryRepository;

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

    /**
     * 周报汇总列表（支持过滤和排序）
     *
     * @param pageRequest 分页参数
     * @param queryParams 查询参数（employeeId, evalPeriod, evalType）
     * @return 分页结果
     */
    @GetMapping("")
    public Page<WeeklyReportSummary> listWeeklyReportSummaries(
            XCPageRequest pageRequest,
            WeeklyReportSummaryQueryParams queryParams) {

        // 构建动态查询条件
        Specification<WeeklyReportSummary> spec = WeeklyReportSummarySpec.byQueryParams(queryParams);

        // 默认排序：评估周期降序、评估类型、员工ID
        var req = pageRequest.toPageRequest().withSort(
            Sort.by("evalPeriod").descending()
            .and(Sort.by("evalType"))
            .and(Sort.by("employeeId")));

        return weeklyReportSummaryRepository.findAll(spec, req);
    }

    @GetMapping("/{id}")
    public WeeklyReportSummary getWeeklyReportSummary(@PathVariable String id) {
        return weeklyReportSummaryRepository.findById(id).orElse(null);
    }

    /**
     * 周报汇总维度详情列表
     *
     * @param id 汇总ID
     * @param metric 维度标识
     */
    @GetMapping("/{id}/details")
    public Page<EvaluationDetail> listWeeklyReportSummaryDetails(
            @PathVariable String id,
            @RequestParam String metric,
            XCPageRequest pageRequest) {

        WeeklyReportSummary summary = weeklyReportSummaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weekly report summary not found"));

        Specification<EvaluationDetail> spec = EvaluationDetailSpec.bySummaryAndMetric(summary, metric);
        return evaluationDetailRepository.findAll(spec, pageRequest.toPageRequest());
    }



}
