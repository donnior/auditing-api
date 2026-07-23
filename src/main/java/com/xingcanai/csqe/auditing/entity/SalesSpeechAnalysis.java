package com.xingcanai.csqe.auditing.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "xca_sales_speech_analysis")
public class SalesSpeechAnalysis {

    @Id
    @Column(name = "id")
    private String id = UlidCreator.getUlid().toLowerCase();

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "employee_qw_id", nullable = false)
    private String employeeQwId;

    @Column(name = "eval_period", nullable = false)
    private LocalDate evalPeriod;

    @Column(name = "period_start_time", nullable = false)
    private ZonedDateTime periodStartTime;

    @Column(name = "period_end_time", nullable = false)
    private ZonedDateTime periodEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SalesSpeechAnalysisStatus status;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Column(name = "report_markdown", columnDefinition = "TEXT")
    private String reportMarkdown;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "finish_reason")
    private String finishReason;

    @Column(name = "prompt_tokens")
    private Long promptTokens;

    @Column(name = "completion_tokens")
    private Long completionTokens;

    @Column(name = "total_tokens")
    private Long totalTokens;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "create_time", nullable = false)
    private ZonedDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private ZonedDateTime updateTime;

    @Column(name = "completed_time")
    private ZonedDateTime completedTime;
}

