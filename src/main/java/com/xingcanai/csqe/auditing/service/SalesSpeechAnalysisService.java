package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.config.SalesSpeechAnalysisProperties;
import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysis;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisRepository;
import com.xingcanai.csqe.auditing.entity.SalesSpeechAnalysisStatus;
import com.xingcanai.csqe.llm.supports.deepseek.DeepSeekProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class SalesSpeechAnalysisService {

    private final SalesSpeechAnalysisRepository repository;
    private final SalesSpeechAnalysisTaskRunner taskRunner;
    private final TaskExecutor taskExecutor;
    private final SalesSpeechAnalysisProperties properties;
    private final DeepSeekProperties deepSeekProperties;
    private final ZoneId zoneId;

    public SalesSpeechAnalysisService(
            SalesSpeechAnalysisRepository repository,
            SalesSpeechAnalysisTaskRunner taskRunner,
            @Qualifier("salesSpeechAnalysisExecutor") TaskExecutor taskExecutor,
            SalesSpeechAnalysisProperties properties,
            DeepSeekProperties deepSeekProperties) {
        this.repository = repository;
        this.taskRunner = taskRunner;
        this.taskExecutor = taskExecutor;
        this.properties = properties;
        this.deepSeekProperties = deepSeekProperties;
        this.zoneId = ZoneId.of(properties.getZoneId());
    }

    public Optional<SalesSpeechAnalysis> get(String employeeId, LocalDate evalPeriod) {
        return repository.findFirstByEmployeeIdAndEvalPeriodOrderByUpdateTimeDescCreateTimeDesc(
                employeeId,
                evalPeriod);
    }

    @Transactional
    public SalesSpeechAnalysisStartResult start(
            Employee employee,
            LocalDate evalPeriod,
            boolean regenerate,
            String requestedBy) {

        if (employee.getQwId() == null || employee.getQwId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Employee has no WeCom ID");
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        SalesSpeechAnalysis analysis = repository.findForUpdate(
                        employee.getId(),
                        evalPeriod,
                        properties.getPromptVersion())
                .orElse(null);

        if (analysis == null) {
            analysis = createAnalysis(employee, evalPeriod, requestedBy, now);
            repository.saveAndFlush(analysis);
            submitAfterCommit(analysis.getId());
            return new SalesSpeechAnalysisStartResult(analysis, true);
        }

        if (analysis.getStatus() == SalesSpeechAnalysisStatus.PROCESSING) {
            boolean stale = analysis.getUpdateTime() == null
                    || analysis.getUpdateTime().isBefore(now.minus(properties.getStaleAfter()));
            if (!stale) {
                if (regenerate) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Sales speech analysis is already processing");
                }
                return new SalesSpeechAnalysisStartResult(analysis, false);
            }
        } else if (!regenerate) {
            return new SalesSpeechAnalysisStartResult(analysis, false);
        }

        resetForProcessing(analysis, employee, requestedBy, now);
        repository.saveAndFlush(analysis);
        submitAfterCommit(analysis.getId());
        return new SalesSpeechAnalysisStartResult(analysis, true);
    }

    private SalesSpeechAnalysis createAnalysis(
            Employee employee,
            LocalDate evalPeriod,
            String requestedBy,
            ZonedDateTime now) {
        SalesSpeechAnalysis analysis = new SalesSpeechAnalysis();
        analysis.setEmployeeId(employee.getId());
        analysis.setEmployeeQwId(employee.getQwId());
        analysis.setEvalPeriod(evalPeriod);
        analysis.setPeriodStartTime(evalPeriod.minusDays(6).atStartOfDay(zoneId));
        analysis.setPeriodEndTime(evalPeriod.plusDays(1).atStartOfDay(zoneId));
        analysis.setStatus(SalesSpeechAnalysisStatus.PROCESSING);
        analysis.setPromptVersion(properties.getPromptVersion());
        analysis.setModelName(deepSeekProperties.getModel());
        analysis.setRequestedBy(requestedBy);
        analysis.setCreateTime(now);
        analysis.setUpdateTime(now);
        return analysis;
    }

    private void resetForProcessing(
            SalesSpeechAnalysis analysis,
            Employee employee,
            String requestedBy,
            ZonedDateTime now) {
        analysis.setEmployeeQwId(employee.getQwId());
        analysis.setPeriodStartTime(analysis.getEvalPeriod().minusDays(6).atStartOfDay(zoneId));
        analysis.setPeriodEndTime(analysis.getEvalPeriod().plusDays(1).atStartOfDay(zoneId));
        analysis.setStatus(SalesSpeechAnalysisStatus.PROCESSING);
        analysis.setReportMarkdown(null);
        analysis.setModelName(deepSeekProperties.getModel());
        analysis.setFinishReason(null);
        analysis.setPromptTokens(null);
        analysis.setCompletionTokens(null);
        analysis.setTotalTokens(null);
        analysis.setErrorCode(null);
        analysis.setErrorMessage(null);
        analysis.setRequestedBy(requestedBy);
        analysis.setUpdateTime(now);
        analysis.setCompletedTime(null);
    }

    private void submitAfterCommit(String analysisId) {
        Runnable submission = () -> {
            try {
                taskExecutor.execute(() -> taskRunner.run(analysisId));
            } catch (TaskRejectedException exception) {
                taskRunner.markSubmissionFailed(analysisId);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    submission.run();
                }
            });
        } else {
            submission.run();
        }
    }
}
