package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
public class WeeklyChatAnalysisServiceV2 extends AbstractChatAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyChatAnalysisServiceV2.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Autowired
    private WxCardUserRepository wxCardUserRepository;

    @Override
    public void runAnalysis() {
        var now = ZonedDateTime.now();
        logger.info("Running weekly analysis at current time: {}", now);
        doRunAnalysis(now);
    }

    // 2026-01-05
    @Override
    public void runAnalysis(String targetDate) {
        var target = LocalDate.parse(targetDate).atStartOfDay(ZoneId.systemDefault());
        logger.info("Running weekly analysis at simulated time: {}", target);
        doRunAnalysis(target);
    }

    /**
     * 指定时间的上一个周末（周天）作为分析目标
     * @param time
     */
    private void doRunAnalysis(ZonedDateTime simulatedRunningTime) {
        var targetWeekend = simulatedRunningTime.with(DayOfWeek.SUNDAY).minusWeeks(1);
        var employees = getActiveEmployees();
        for (var employee : employees) {
            runWeeklyAnalysisForEmployee(employee, targetWeekend);
        }
    }

    private void runWeeklyAnalysisForEmployee(Employee employee, ZonedDateTime targetSunday) {
        var reportPeriod = targetSunday.toLocalDate().toString();
        var bizDate = reportPeriod;

        var targetSundayEndTime = DateTimeUtils.asEndOfDay(targetSunday);

        var customers = getLatest4WeeksCustomers(employee, targetSundayEndTime);

        for (var customer : customers) {
            var reportType = getReportTypeForCustomer(employee, customer, targetSunday);
            if (isReportTypeSupported(reportType)) {
                ZonedDateTime chatRangeStart = getChatTimeRangeStart(customer, targetSundayEndTime, reportType);
                executorService.submit(() -> runCustomerAnalysisWithType(employee, customer, chatRangeStart, targetSundayEndTime, reportType, reportPeriod, bizDate));
            }
        }
    }


    private List<WxCardUser> getLatest4WeeksCustomers(Employee employee, ZonedDateTime targetSunday) {
        ZonedDateTime fromTime = DateTimeUtils.asStartOfDay(targetSunday.minusDays(30));
        ZonedDateTime toTime = DateTimeUtils.asStartOfDay(targetSunday.minusDays(2));

        return wxCardUserRepository.findByEmployeeQwidAndTimeRange(employee.getQwId(), fromTime, toTime);
    }

    private ZonedDateTime getChatTimeRangeStart(WxCardUser customer, ZonedDateTime targetSundayEndTime, String reportType) {
        ZonedDateTime chatRangeStart = customer.getStartTime().minusMinutes(1);
        if (reportType != TypedReportAnalyser.ReportTypeForFirstWeek) {
            chatRangeStart = targetSundayEndTime.minusDays(7);
        }
        return chatRangeStart;
    }


    private String getReportTypeForCustomer(Employee employee, WxCardUser customer, ZonedDateTime targetSunday) {
        ZonedDateTime firstChatTime = customer.getStartTime();
        logger.debug("First chat time between employee {} and customer {}: {}", employee.getQwId(), customer.getExternalUserid(), firstChatTime);
        int weekNumber =  WeekNumberCalculator.calculateReportType(firstChatTime, targetSunday);

        return switch (weekNumber) {
            case 1 -> TypedReportAnalyser.ReportTypeForFirstWeek;
            case 2 -> TypedReportAnalyser.ReportTypeForSecondWeek;
            case 3 -> TypedReportAnalyser.ReportTypeForThirdWeek;
            case 4 -> TypedReportAnalyser.ReportTypeForFourthWeek;
            default -> {
                if (weekNumber < 1) {
                    logger.warn("Target Sunday {} is before first week Sunday {}, returning empty");
                } else {
                    logger.debug("Week number {} is beyond 4 weeks, returning empty", weekNumber);
                }
                yield ""; // 大于4周或小于1周，返回空
            }
        };
    }

}
