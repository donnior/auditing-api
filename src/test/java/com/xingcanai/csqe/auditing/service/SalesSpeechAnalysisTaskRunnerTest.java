package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.config.SalesSpeechAnalysisProperties;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformanceRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysis;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisStatus;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekClient;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekException;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesSpeechAnalysisTaskRunnerTest {

    private final SalesSpeechAnalysisRepository analysisRepository = mock(SalesSpeechAnalysisRepository.class);
    private final WxChatMessageRepository chatRepository = mock(WxChatMessageRepository.class);
    private final CampCustomerDailyPerformanceRepository performanceRepository =
            mock(CampCustomerDailyPerformanceRepository.class);
    private final SalesSpeechAnalysisPromptBuilder promptBuilder = mock(SalesSpeechAnalysisPromptBuilder.class);
    private final DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

    private SalesSpeechAnalysis analysis;
    private SalesSpeechAnalysisTaskRunner runner;

    @BeforeEach
    void setUp() {
        SalesSpeechAnalysisProperties properties = new SalesSpeechAnalysisProperties();
        properties.setZoneId("Asia/Shanghai");
        runner = new SalesSpeechAnalysisTaskRunner(
                analysisRepository,
                chatRepository,
                performanceRepository,
                promptBuilder,
                deepSeekClient,
                properties);

        analysis = new SalesSpeechAnalysis();
        analysis.setId("analysis-1");
        analysis.setEmployeeId("employee-1");
        analysis.setEmployeeQwId("qw-1");
        analysis.setEvalPeriod(LocalDate.of(2026, 7, 18));
        analysis.setPeriodStartTime(
                ZonedDateTime.of(2026, 7, 12, 0, 0, 0, 0, ZoneId.of("Asia/Shanghai")));
        analysis.setPeriodEndTime(
                ZonedDateTime.of(2026, 7, 19, 0, 0, 0, 0, ZoneId.of("Asia/Shanghai")));
        analysis.setStatus(SalesSpeechAnalysisStatus.PROCESSING);
        analysis.setPromptVersion("v1");
        analysis.setRequestedBy("admin");
        analysis.setCreateTime(ZonedDateTime.now());
        analysis.setUpdateTime(ZonedDateTime.now());

        when(analysisRepository.findById("analysis-1")).thenReturn(Optional.of(analysis));
        when(chatRepository.findDirectMessagesByEmployeeAndTimeRange(
                "qw-1", analysis.getPeriodStartTime(), analysis.getPeriodEndTime()))
                .thenReturn(List.of());
        when(performanceRepository.findRawBySysUserIdAndStatDateRange(
                "qw-1", LocalDate.of(2026, 7, 12), LocalDate.of(2026, 7, 18)))
                .thenReturn(List.of());
        when(promptBuilder.build(
                "qw-1",
                LocalDate.of(2026, 7, 12),
                LocalDate.of(2026, 7, 18),
                List.of(),
                List.of()))
                .thenReturn(new SalesSpeechAnalysisPrompt("system", "user"));
    }

    @Test
    void savesSuccessfulMarkdownExactlyAsReturned() {
        String rawMarkdown = "# 不完整报告\n\n数字：999999\n<div>原始HTML文本</div>";
        when(deepSeekClient.complete("system", "user"))
                .thenReturn(new DeepSeekResult(
                        rawMarkdown,
                        "deepseek-v4-pro",
                        "length",
                        10L,
                        20L,
                        30L));

        runner.run("analysis-1");

        ArgumentCaptor<SalesSpeechAnalysis> captor = ArgumentCaptor.forClass(SalesSpeechAnalysis.class);
        verify(analysisRepository).save(captor.capture());
        SalesSpeechAnalysis saved = captor.getValue();
        assertEquals(SalesSpeechAnalysisStatus.COMPLETED, saved.getStatus());
        assertEquals(rawMarkdown, saved.getReportMarkdown());
        assertEquals("length", saved.getFinishReason());
        assertEquals(30L, saved.getTotalTokens());
        assertNull(saved.getErrorCode());
    }

    @Test
    void persistsDeepSeekFailureWithoutLeakingInput() {
        when(deepSeekClient.complete("system", "user"))
                .thenThrow(new DeepSeekException(
                        "HTTP_402",
                        false,
                        "DeepSeek request failed with HTTP 402"));

        runner.run("analysis-1");

        ArgumentCaptor<SalesSpeechAnalysis> captor = ArgumentCaptor.forClass(SalesSpeechAnalysis.class);
        verify(analysisRepository).save(captor.capture());
        SalesSpeechAnalysis saved = captor.getValue();
        assertEquals(SalesSpeechAnalysisStatus.FAILED, saved.getStatus());
        assertEquals("HTTP_402", saved.getErrorCode());
        assertEquals("DeepSeek request failed with HTTP 402", saved.getErrorMessage());
        assertNull(saved.getReportMarkdown());
    }
}

