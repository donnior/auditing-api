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
        logger.info("runWeeklyAnalysis");
        doRunAnalysis(ZonedDateTime.now());
    }

    // 2026-01-05
    @Override
    public void runAnalysis(String targetDate) {
        doRunAnalysis(LocalDate.parse(targetDate).atStartOfDay(ZoneId.systemDefault()));
    }

    private void doRunAnalysis(ZonedDateTime time) {
        var lastSunday = time.with(DayOfWeek.SUNDAY).minusWeeks(1);
        var employees = getActiveEmployees();
        for (var employee : employees) {
            runWeeklyAnalysisForEmployee(employee, lastSunday);
        }
    }

    private void runWeeklyAnalysisForEmployee(Employee employee, ZonedDateTime targetSunday) {
        var reportPeriod = targetSunday.toLocalDate().toString();
        var bizDate = reportPeriod;

        var targetSundayEndTime = targetSunday.withHour(0).withMinute(0).withSecond(0).withNano(0);

        ZonedDateTime fromTime = targetSunday.minusDays(30).withHour(0).withMinute(0).withSecond(0);
        // ZonedDateTime toTime = toMonday.withHour(0).withMinute(0).withSecond(0);
        ZonedDateTime toTime = targetSunday.minusDays(2).withHour(0).withMinute(0).withSecond(9);

        var customers = getCustomersByEmployeeAndTimeRange(employee, fromTime, toTime);

        for (var customer : customers) {
            var reportType = getReportTypeForCustomer(employee, customer, targetSunday);
            if (isReportTypeSupported(reportType)) {
                ZonedDateTime chatRangeStart = getChatRangeStart(customer, targetSundayEndTime, reportType);
                executorService.submit(() -> runCustomerAnalysisWithType(employee, customer, chatRangeStart, targetSundayEndTime, reportType, reportPeriod, bizDate));
            }
        }
    }

    private ZonedDateTime getChatRangeStart(WxCardUser customer, ZonedDateTime targetSundayEndTime, String reportType) {
        ZonedDateTime chatRangeStart = customer.getStartTime().minusMinutes(1);
        if (reportType != TypedReportAnalyser.ReportTypeForFirstWeek) {
            chatRangeStart = targetSundayEndTime.minusDays(7);
        }
        return chatRangeStart;
    }

    private List<WxCardUser> getCustomersByEmployeeAndTimeRange(Employee employee, ZonedDateTime fromTime, ZonedDateTime toTime) {
        return wxCardUserRepository.findByEmployeeQwidAndTimeRange(employee.getQwId(), fromTime, toTime);
    }

    private String getReportTypeForCustomer(Employee employee, WxCardUser customer, ZonedDateTime targetSunday) {
        // 获取员工和客户之间最早的一条聊天记录
        ZonedDateTime firstChatTime = customer.getStartTime();
        logger.debug("First chat time between employee {} and customer {}: {}", employee.getQwId(), customer.getExternalUserid(), firstChatTime);
        int weekNumber =  WeeklyChatReportTypeCalculator.calculateReportType(firstChatTime, targetSunday);

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
