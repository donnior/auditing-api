package com.xingcanai.csqe.auditing.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * 员工企微帐号-客户聊天分析明细实体
 */
@Entity
@Table(name = "xca_employee_analysis_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAnalysisReport {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "generating_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GeneratingStatus generatingStatus;

    @Column(name = "qw_account_id", nullable = false)
    private String qwAccountId;

    @Column(name = "qw_account_name")
    private String qwAccountName;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "cycle_start_time", nullable = false)
    private ZonedDateTime cycleStartTime;

    @Column(name = "cycle_end_time", nullable = false)
    private ZonedDateTime cycleEndTime;

    @Column(name = "report_summary")
    private String reportSummary;

    @Column(name = "report_rating")
    private String reportRating;

    @Column(name = "report_score")
    private int reportScore;

    @Column(name = "report_suggestions")
    private String reportSuggestions;

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
