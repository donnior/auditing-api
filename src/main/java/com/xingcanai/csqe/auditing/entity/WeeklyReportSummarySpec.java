package com.xingcanai.csqe.auditing.entity;

import com.xingcanai.csqe.auditing.web.WeeklyReportSummaryQueryParams;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * 周报汇总查询规格
 */
public class WeeklyReportSummarySpec {

    /**
     * 根据查询参数构建动态查询条件
     */
    public static Specification<WeeklyReportSummary> byQueryParams(WeeklyReportSummaryQueryParams params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 员工ID过滤
            if (params.getEmployeeId() != null && !params.getEmployeeId().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("employeeId"), params.getEmployeeId()));
            }

            // 评估周期过滤
            if (params.getEvalPeriod() != null && !params.getEvalPeriod().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("evalPeriod"), params.getEvalPeriod()));
            }

            // 评估类型过滤
            if (params.getEvalType() != null && !params.getEvalType().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("evalType"), params.getEvalType()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
