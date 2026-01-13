package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;

/**
 * 聊天分析服务
 */
@Service
public class DailyChatAnalysisService extends AbstractChatAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DailyChatAnalysisService.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Autowired
    private WxChatMessageRepository wxChatMessageRepository;

    public void runAnalysis() {
        var now = ZonedDateTime.now();
        /**
         * 以当前时间的0点为基准运行分析，跑前一天的数据
         */
        var targetDate = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        doRunAnalysis(targetDate);
        // runAnalysis("2026-01-01");
    }

    /**
     * 以指定日期的0点基准运行分析，跑前一天的数据
     */
    public void runAnalysis(String targetDate) {
        var target = LocalDate.parse(targetDate).atStartOfDay(ZoneId.systemDefault());
        doRunAnalysis(target);
    }

    private void doRunAnalysis(ZonedDateTime time) {
        var employees = getActiveEmployees();
        for (var employee : employees) {
            runWeeklyAnalysisForEmployee(employee, time);
        }
    }

    private String getReportName(ZonedDateTime firstChatTime) {
        String reportName = firstChatTime.with(DayOfWeek.SUNDAY).toLocalDate().toString();
        if (firstChatTime.getDayOfWeek().getValue() > DayOfWeek.THURSDAY.getValue()) {
            reportName = firstChatTime.with(DayOfWeek.SUNDAY).plusWeeks(1).toLocalDate().toString();
        }
        return reportName;
    }

    private void runWeeklyAnalysisForEmployee(Employee employee, ZonedDateTime time) {

        var toTime = time;
        var fromTime = toTime.minusHours(72);

        // has chat in last 72 hours
        var customers = wxChatMessageRepository.findCustomersByEmployeeAndTimeRange(employee.getQwId(), fromTime, toTime);

        for (var customer : customers) {
            WxChatMessage firstChat = wxChatMessageRepository.findFirstChatBetweenEmployeeAndCustomer(employee.getQwId(), customer);
            var firstChatTime = firstChat.getMsgTime();
            var rangeEnd = toTime.minusHours(48);
            String reportName = getReportName(firstChatTime);
            String bizDate = toTime.minusDays(1).toLocalDate().toString();

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
