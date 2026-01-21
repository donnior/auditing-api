package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.WxCardUser;
import com.xingcanai.csqe.auditing.entity.WxCardUserRepository;

/**
 * 聊天分析服务
 */
@Service
public class DailyChatAnalysisServiceV2 extends AbstractChatAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DailyChatAnalysisServiceV2.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Autowired
    private WxCardUserRepository wxCardUserRepository;

    public void runAnalysis() {
        var now = ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault());
        /**
         * 以当前时间的0点为基准运行分析，跑前一天的数据
         */
        logger.info("Running daily analysis at current time: {}", now);
        doRunAnalysis(now);
        // runAnalysis("2026-01-01");
    }

    /**
     * 以指定日期的0点基准运行分析，跑前一天的数据
     */
    public void runAnalysis(String targetDate) {
        var target = LocalDate.parse(targetDate).atStartOfDay(ZoneId.systemDefault());
        logger.info("Running daily analysis at simulated time: {}", target);
        doRunAnalysis(target);
    }

    private void doRunAnalysis(ZonedDateTime simulatedRunningTime) {
        var targetDate = simulatedRunningTime.minusDays(1);
        var employees = getActiveEmployees();
        for (var employee : employees) {
            runAnalysisForEmployee(employee, targetDate);
        }
    }

    private String getReportName(ZonedDateTime firstChatTime) {
        String reportName = firstChatTime.with(DayOfWeek.SUNDAY).toLocalDate().toString();
        if (firstChatTime.getDayOfWeek().getValue() > DayOfWeek.THURSDAY.getValue()) {
            reportName = firstChatTime.with(DayOfWeek.SUNDAY).plusWeeks(1).toLocalDate().toString();
        }
        return reportName;
    }

    private List<WxCardUser> getCustomersByEmployeeAndTimeRange(Employee employee, ZonedDateTime fromTime, ZonedDateTime toTime) {
        return wxCardUserRepository.findByEmployeeQwidAndTimeRange(employee.getQwId(), fromTime, toTime);
    }

    private void runAnalysisForEmployee(Employee employee, ZonedDateTime targetDate) {

        var toTime = DateTimeUtils.asEndOfDay(targetDate);
        var fromTime = toTime.minusHours(72);

        var customers = getCustomersByEmployeeAndTimeRange(employee, fromTime, toTime);

        for (var customer : customers) {
            var firstChatTime = customer.getStartTime().withZoneSameInstant(ZoneId.systemDefault());
            var rangeEnd = toTime.minusHours(48);
            String reportName = getReportName(firstChatTime);
            String bizDate = targetDate.toLocalDate().toString();

            if(firstChatTime.isAfter(fromTime) && firstChatTime.isBefore(rangeEnd)) {
                CompletableFuture.runAsync(
                    () -> runCustomerAnalysisWithType(employee, customer, fromTime, firstChatTime.plusHours(48), TypedReportAnalyser.ReportTypeForWithin48Hours,reportName, bizDate),
                    executorService
                ).exceptionally(ex -> {
                            logger.error("Error running customer analysis for employee {} and customer {}", employee.getQwId(), customer, ex);
                            return null;
                });

                // executorService.execute(() -> runCustomerAnalysisWithType(employee, customer, fromTime, firstChatTime.plusHours(48), TypedReportAnalyser.ReportTypeForWithin48Hours, reportName));
            }
        }
    }

}
