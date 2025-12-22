package com.example.demo.auditing.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * 员工企微帐号聊天分析汇总实体
 */
@Entity
@Table(name = "xca_employee_daily_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDailyAnalysis {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "qw_account_id", nullable = false)
    private String qwAccountId;

    @Column(name = "qw_account_name")
    private String qwAccountName;

    @Column(name = "cycle_start_time", nullable = false)
    private ZonedDateTime cycleStartTime;

    @Column(name = "cycle_end_time", nullable = false)
    private ZonedDateTime cycleEndTime;

    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;

    @Column(name = "total_customers")
    private Integer totalCustomers = 0;

    @Column(name = "total_messages")
    private Integer totalMessages = 0;

    @Column(name = "avg_response_time", precision = 10, scale = 2)
    private BigDecimal avgResponseTime = BigDecimal.ZERO;

    @Column(name = "overall_satisfaction", precision = 3, scale = 2)
    private BigDecimal overallSatisfaction = BigDecimal.ZERO;

    @Column(name = "total_violations")
    private Integer totalViolations = 0;

    @Column(name = "service_quality_score", precision = 3, scale = 2)
    private BigDecimal serviceQualityScore = BigDecimal.ZERO;

    @Column(name = "performance_rating")
    private String performanceRating;

    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = ZonedDateTime.now();
    }
}
