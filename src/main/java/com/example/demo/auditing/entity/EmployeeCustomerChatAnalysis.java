package com.example.demo.auditing.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * 员工企微帐号-客户聊天分析明细实体
 */
@Entity
@Table(name = "xca_employee_customer_chat_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCustomerChatAnalysis {

    @Id
    @Column(name = "id")
    private String id;

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

    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;

    @Column(name = "message_count")
    private Integer messageCount = 0;

    @Column(name = "response_time_avg", precision = 10, scale = 2)
    private BigDecimal responseTimeAvg = BigDecimal.ZERO;

    @Column(name = "satisfaction_score", precision = 3, scale = 2)
    private BigDecimal satisfactionScore = BigDecimal.ZERO;

    @Column(name = "violation_count")
    private Integer violationCount = 0;

    @Column(name = "service_quality_score", precision = 3, scale = 2)
    private BigDecimal serviceQualityScore = BigDecimal.ZERO;

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
