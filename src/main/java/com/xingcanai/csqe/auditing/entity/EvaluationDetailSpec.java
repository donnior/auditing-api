package com.xingcanai.csqe.auditing.entity;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EvaluationDetailSpec {

    public static Specification<EvaluationDetail> bySummaryAndMetric(WeeklyReportSummary summary, String metric) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("employeeId"), summary.getEmployeeId()));
            predicates.add(criteriaBuilder.equal(root.get("evalPeriod"), summary.getEvalPeriod()));
            predicates.add(criteriaBuilder.equal(root.get("evalType"), summary.getEvalType()));

            switch (metric) {
                case "totalCustomers" -> {
                    // no extra filter
                }
                case "totalMaterialSend" -> predicates.add(criteriaBuilder.equal(root.get("hasMaterialSend"), 1));
                case "totalCourseRemind" -> predicates.add(criteriaBuilder.equal(root.get("hasCourseRemind"), 1));
                case "totalHomeworkPublish" -> predicates.add(criteriaBuilder.equal(root.get("hasHomeworkPublish"), 1));
                case "totalWeekMaterialSend" -> predicates.add(criteriaBuilder.equal(root.get("hasWeekMaterialSend"), 1));
                case "totalSundayLinkSend" -> predicates.add(criteriaBuilder.equal(root.get("hasSundayLinkSend"), 1));
                case "totalFeedbackTrack" -> predicates.add(criteriaBuilder.equal(root.get("hasFeedbackTrack"), 1));
                case "totalRiskWordTrigger" -> predicates.add(criteriaBuilder.equal(root.get("hasRiskWordTrigger"), 1));
                case "totalIntroduceTeacher" -> predicates.add(criteriaBuilder.equal(root.get("hasIntroduceTeacher"), 1));
                case "totalIntroduceCourse" -> predicates.add(criteriaBuilder.equal(root.get("hasIntroduceCourse"), 1));
                case "totalIntroduceSchedule" -> predicates.add(criteriaBuilder.equal(root.get("hasIntroduceSchedule"), 1));
                case "totalIntroduceCourseTime" -> predicates.add(criteriaBuilder.equal(root.get("hasIntroduceCourseTime"), 1));
                case "totalOrderCheck" -> predicates.add(criteriaBuilder.equal(root.get("hasOrderCheck"), 1));
                case "totalIntroduceCompleted" -> {
                    predicates.add(criteriaBuilder.equal(root.get("hasIntroduceTeacher"), 1));
                    predicates.add(criteriaBuilder.equal(root.get("hasIntroduceCourse"), 1));
                    predicates.add(criteriaBuilder.equal(root.get("hasIntroduceSchedule"), 1));
                    predicates.add(criteriaBuilder.equal(root.get("hasIntroduceCourseTime"), 1));
                    predicates.add(criteriaBuilder.equal(root.get("hasOrderCheck"), 1));
                }
                default -> throw new IllegalArgumentException("Unsupported metric: " + metric);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
