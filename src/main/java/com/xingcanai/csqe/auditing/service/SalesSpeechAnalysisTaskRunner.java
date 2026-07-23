package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.config.SalesSpeechAnalysisProperties;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformance;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformanceRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysis;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisStatus;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekClient;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekException;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SalesSpeechAnalysisTaskRunner {

    private static final Logger logger = LoggerFactory.getLogger(SalesSpeechAnalysisTaskRunner.class);

    private final SalesSpeechAnalysisRepository analysisRepository;
    private final WxChatMessageRepository chatMessageRepository;
    private final CampCustomerDailyPerformanceRepository performanceRepository;
    private final SalesSpeechAnalysisPromptBuilder promptBuilder;
    private final DeepSeekClient deepSeekClient;
    private final ZoneId zoneId;
    private final Set<String> activeTaskIds = ConcurrentHashMap.newKeySet();

    public SalesSpeechAnalysisTaskRunner(
            SalesSpeechAnalysisRepository analysisRepository,
            WxChatMessageRepository chatMessageRepository,
            CampCustomerDailyPerformanceRepository performanceRepository,
            SalesSpeechAnalysisPromptBuilder promptBuilder,
            DeepSeekClient deepSeekClient,
            SalesSpeechAnalysisProperties properties) {
        this.analysisRepository = analysisRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.performanceRepository = performanceRepository;
        this.promptBuilder = promptBuilder;
        this.deepSeekClient = deepSeekClient;
        this.zoneId = ZoneId.of(properties.getZoneId());
    }

    public void run(String analysisId) {
        if (!activeTaskIds.add(analysisId)) {
            return;
        }

        try {
            SalesSpeechAnalysis analysis = analysisRepository.findById(analysisId).orElse(null);
            if (analysis == null || analysis.getStatus() != SalesSpeechAnalysisStatus.PROCESSING) {
                return;
            }

            List<WxChatMessage> messages =
                    chatMessageRepository.findDirectMessagesByEmployeeAndTimeRange(
                            analysis.getEmployeeQwId(),
                            analysis.getPeriodStartTime(),
                            analysis.getPeriodEndTime());
            List<CampCustomerDailyPerformance> performances =
                    performanceRepository.findRawBySysUserIdAndStatDateRange(
                            analysis.getEmployeeQwId(),
                            analysis.getEvalPeriod().minusDays(6),
                            analysis.getEvalPeriod());

            SalesSpeechAnalysisPrompt prompt = promptBuilder.build(
                    analysis.getEmployeeQwId(),
                    analysis.getEvalPeriod().minusDays(6),
                    analysis.getEvalPeriod(),
                    messages,
                    performances);
            DeepSeekResult result = deepSeekClient.complete(prompt.systemPrompt(), prompt.userPrompt());
            markCompleted(analysisId, result);
        } catch (DeepSeekException exception) {
            logger.warn(
                    "Sales speech analysis task {} failed with {}: {}",
                    analysisId,
                    exception.getErrorCode(),
                    exception.getMessage());
            markFailed(analysisId, exception.getErrorCode(), exception.getMessage());
        } catch (Exception exception) {
            logger.error("Sales speech analysis task {} failed unexpectedly", analysisId);
            markFailed(
                    analysisId,
                    "UNEXPECTED",
                    "Sales speech analysis failed unexpectedly");
        } finally {
            activeTaskIds.remove(analysisId);
        }
    }

    public void markSubmissionFailed(String analysisId) {
        markFailed(
                analysisId,
                "EXECUTOR_REJECTED",
                "Sales speech analysis queue is full; please retry");
    }

    private void markCompleted(String analysisId, DeepSeekResult result) {
        SalesSpeechAnalysis analysis = analysisRepository.findById(analysisId).orElse(null);
        if (analysis == null || analysis.getStatus() != SalesSpeechAnalysisStatus.PROCESSING) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        analysis.setStatus(SalesSpeechAnalysisStatus.COMPLETED);
        analysis.setReportMarkdown(result.content());
        analysis.setModelName(result.model());
        analysis.setFinishReason(result.finishReason());
        analysis.setPromptTokens(result.promptTokens());
        analysis.setCompletionTokens(result.completionTokens());
        analysis.setTotalTokens(result.totalTokens());
        analysis.setErrorCode(null);
        analysis.setErrorMessage(null);
        analysis.setUpdateTime(now);
        analysis.setCompletedTime(now);
        analysisRepository.save(analysis);
    }

    private void markFailed(String analysisId, String errorCode, String errorMessage) {
        SalesSpeechAnalysis analysis = analysisRepository.findById(analysisId).orElse(null);
        if (analysis == null || analysis.getStatus() != SalesSpeechAnalysisStatus.PROCESSING) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        analysis.setStatus(SalesSpeechAnalysisStatus.FAILED);
        analysis.setReportMarkdown(null);
        analysis.setFinishReason(null);
        analysis.setPromptTokens(null);
        analysis.setCompletionTokens(null);
        analysis.setTotalTokens(null);
        analysis.setErrorCode(errorCode);
        analysis.setErrorMessage(errorMessage);
        analysis.setUpdateTime(now);
        analysis.setCompletedTime(now);
        analysisRepository.save(analysis);
    }
}
