package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysis;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record SalesSpeechAnalysisResponse(
        String id,
        String employeeId,
        LocalDate evalPeriod,
        String status,
        String promptVersion,
        String modelName,
        String finishReason,
        String reportMarkdown,
        TokenUsage tokenUsage,
        String errorCode,
        String errorMessage,
        boolean retryable,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        ZonedDateTime completedAt) {

    public static SalesSpeechAnalysisResponse from(SalesSpeechAnalysis analysis) {
        return new SalesSpeechAnalysisResponse(
                analysis.getId(),
                analysis.getEmployeeId(),
                analysis.getEvalPeriod(),
                analysis.getStatus().name(),
                analysis.getPromptVersion(),
                analysis.getModelName(),
                analysis.getFinishReason(),
                analysis.getReportMarkdown(),
                new TokenUsage(
                        analysis.getPromptTokens(),
                        analysis.getCompletionTokens(),
                        analysis.getTotalTokens()),
                analysis.getErrorCode(),
                analysis.getErrorMessage(),
                analysis.getStatus().name().equals("FAILED"),
                analysis.getCreateTime(),
                analysis.getUpdateTime(),
                analysis.getCompletedTime());
    }

    public static SalesSpeechAnalysisResponse notStarted(String employeeId, LocalDate evalPeriod) {
        return new SalesSpeechAnalysisResponse(
                null,
                employeeId,
                evalPeriod,
                "NOT_STARTED",
                null,
                null,
                null,
                null,
                new TokenUsage(null, null, null),
                null,
                null,
                false,
                null,
                null,
                null);
    }

    public record TokenUsage(Long promptTokens, Long completionTokens, Long totalTokens) {
    }
}

