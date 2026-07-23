package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.config.SalesSpeechAnalysisProperties;
import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysis;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisStatus;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesSpeechAnalysisServiceTest {

    private final SalesSpeechAnalysisRepository repository = mock(SalesSpeechAnalysisRepository.class);
    private final SalesSpeechAnalysisTaskRunner taskRunner = mock(SalesSpeechAnalysisTaskRunner.class);
    private final SalesSpeechAnalysisProperties properties = new SalesSpeechAnalysisProperties();
    private final DeepSeekProperties deepSeekProperties = new DeepSeekProperties();
    private final TaskExecutor directExecutor = Runnable::run;

    private SalesSpeechAnalysisService service;
    private Employee employee;
    private LocalDate evalPeriod;

    @BeforeEach
    void setUp() {
        reset(repository, taskRunner);
        properties.setPromptVersion("v1-test");
        properties.setZoneId("Asia/Shanghai");
        deepSeekProperties.setModel("deepseek-v4-pro");
        service = new SalesSpeechAnalysisService(
                repository,
                taskRunner,
                directExecutor,
                properties,
                deepSeekProperties);

        employee = new Employee();
        employee.setId("employee-1");
        employee.setQwId("qw-1");
        employee.setName("销售一");
        employee.setIsDeleted(false);
        evalPeriod = LocalDate.of(2026, 7, 18);
    }

    @Test
    void readsLatestAnalysisWithoutFilteringPromptVersion() {
        SalesSpeechAnalysis latest = analysis(SalesSpeechAnalysisStatus.COMPLETED);
        latest.setPromptVersion("v0-historical");
        when(repository.findFirstByEmployeeIdAndEvalPeriodOrderByUpdateTimeDescCreateTimeDesc(
                "employee-1",
                evalPeriod))
                .thenReturn(Optional.of(latest));

        SalesSpeechAnalysis result =
                service.get("employee-1", evalPeriod).orElseThrow();

        assertSame(latest, result);
        verify(repository)
                .findFirstByEmployeeIdAndEvalPeriodOrderByUpdateTimeDescCreateTimeDesc(
                        "employee-1",
                        evalPeriod);
    }

    @Test
    void createsProcessingTaskForSevenNaturalDaysAndSubmitsIt() {
        when(repository.findForUpdate("employee-1", evalPeriod, "v1-test"))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(SalesSpeechAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SalesSpeechAnalysisStartResult result =
                service.start(employee, evalPeriod, false, "admin");

        assertTrue(result.accepted());
        SalesSpeechAnalysis analysis = result.analysis();
        assertEquals(SalesSpeechAnalysisStatus.PROCESSING, analysis.getStatus());
        assertEquals(
                ZonedDateTime.of(2026, 7, 12, 0, 0, 0, 0, ZoneId.of("Asia/Shanghai")),
                analysis.getPeriodStartTime());
        assertEquals(
                ZonedDateTime.of(2026, 7, 19, 0, 0, 0, 0, ZoneId.of("Asia/Shanghai")),
                analysis.getPeriodEndTime());
        assertEquals("v1-test", analysis.getPromptVersion());
        assertEquals("deepseek-v4-pro", analysis.getModelName());
        verify(taskRunner).run(analysis.getId());
    }

    @Test
    void reusesCompletedResultWithoutSubmitting() {
        SalesSpeechAnalysis completed = analysis(SalesSpeechAnalysisStatus.COMPLETED);
        completed.setReportMarkdown("# cached");
        when(repository.findForUpdate("employee-1", evalPeriod, "v1-test"))
                .thenReturn(Optional.of(completed));

        SalesSpeechAnalysisStartResult result =
                service.start(employee, evalPeriod, false, "admin");

        assertFalse(result.accepted());
        assertEquals("# cached", result.analysis().getReportMarkdown());
        verify(taskRunner, never()).run(any());
    }

    @Test
    void deduplicatesActiveProcessingAndRejectsForcedRerun() {
        SalesSpeechAnalysis processing = analysis(SalesSpeechAnalysisStatus.PROCESSING);
        processing.setUpdateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
        when(repository.findForUpdate("employee-1", evalPeriod, "v1-test"))
                .thenReturn(Optional.of(processing));

        SalesSpeechAnalysisStartResult duplicate =
                service.start(employee, evalPeriod, false, "admin");
        assertFalse(duplicate.accepted());
        verify(taskRunner, never()).run(any());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.start(employee, evalPeriod, true, "admin"));
        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void manualRegenerationClearsTerminalFieldsAndSubmits() {
        SalesSpeechAnalysis completed = analysis(SalesSpeechAnalysisStatus.COMPLETED);
        completed.setReportMarkdown("# old");
        completed.setFinishReason("stop");
        completed.setPromptTokens(1L);
        completed.setErrorCode("old-error");
        completed.setCompletedTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")));
        when(repository.findForUpdate("employee-1", evalPeriod, "v1-test"))
                .thenReturn(Optional.of(completed));

        SalesSpeechAnalysisStartResult result =
                service.start(employee, evalPeriod, true, "leader");

        assertTrue(result.accepted());
        assertEquals(SalesSpeechAnalysisStatus.PROCESSING, completed.getStatus());
        assertNull(completed.getReportMarkdown());
        assertNull(completed.getFinishReason());
        assertNull(completed.getPromptTokens());
        assertNull(completed.getErrorCode());
        assertNull(completed.getCompletedTime());
        assertEquals("leader", completed.getRequestedBy());
        verify(taskRunner).run(completed.getId());
    }

    @Test
    void staleProcessingCanBeTriggeredAgainWithoutForce() {
        SalesSpeechAnalysis stale = analysis(SalesSpeechAnalysisStatus.PROCESSING);
        stale.setUpdateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusMinutes(21));
        when(repository.findForUpdate("employee-1", evalPeriod, "v1-test"))
                .thenReturn(Optional.of(stale));

        SalesSpeechAnalysisStartResult result =
                service.start(employee, evalPeriod, false, "admin");

        assertTrue(result.accepted());
        verify(taskRunner).run(stale.getId());
    }

    private SalesSpeechAnalysis analysis(SalesSpeechAnalysisStatus status) {
        SalesSpeechAnalysis analysis = new SalesSpeechAnalysis();
        analysis.setId("analysis-1");
        analysis.setEmployeeId(employee.getId());
        analysis.setEmployeeQwId(employee.getQwId());
        analysis.setEvalPeriod(evalPeriod);
        analysis.setPeriodStartTime(evalPeriod.minusDays(6).atStartOfDay(ZoneId.of("Asia/Shanghai")));
        analysis.setPeriodEndTime(evalPeriod.plusDays(1).atStartOfDay(ZoneId.of("Asia/Shanghai")));
        analysis.setStatus(status);
        analysis.setPromptVersion("v1-test");
        analysis.setRequestedBy("admin");
        analysis.setCreateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusMinutes(5));
        analysis.setUpdateTime(ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).minusMinutes(5));
        return analysis;
    }
}
