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

    private static final Logger logger = LoggerFactory.getLogger(WeeklyChatAnalysisService.class);

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

        ZonedDateTime fromTime = targetSunday.minusDays(30).withHour(0).withMinute(0).withSecond(0);
        // ZonedDateTime toTime = toMonday.withHour(0).withMinute(0).withSecond(0);
        ZonedDateTime toTime = targetSunday.minusDays(2).withHour(0).withMinute(0).withSecond(9);

        var customers = getCustomersByEmployeeAndTimeRange(employee, fromTime, toTime);

        for (var customer : customers) {
            var reportType = getReportTypeForCustomer(employee, customer, targetSunday);
            if (isReportTypeSupported(reportType)) {
                executorService.submit(() -> runCustomerAnalysisWithType(employee, customer, fromTime, toTime, reportType, reportPeriod, bizDate));
            }
        }
    }

    private List<WxCardUser> getCustomersByEmployeeAndTimeRange(Employee employee, ZonedDateTime fromTime, ZonedDateTime toTime) {
        return wxCardUserRepository.findByEmployeeQwidAndTimeRange(employee.getQwId(), fromTime, toTime);
    }

    private String getReportTypeForCustomer(Employee employee, WxCardUser customer, ZonedDateTime targetSunday) {
        // 获取员工和客户之间最早的一条聊天记录
        ZonedDateTime firstChatTime = customer.getStartTime();
        logger.debug("First chat time between employee {} and customer {}: {}", employee.getQwId(), customer.getExternalUserid(), firstChatTime);

        // 找到第一条聊天记录所在周的周四结束时间（周四23:59:59）
        ZonedDateTime thursdayEnd = firstChatTime.with(DayOfWeek.THURSDAY)
            .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // 如果第一条聊天记录在周四之后（周五、周六），需要向后找到下一周的周四四
        if (firstChatTime.getDayOfWeek().getValue() > DayOfWeek.THURSDAY.getValue()) {
            thursdayEnd = thursdayEnd.plusWeeks(1);
        }

        // 确定第一周周日
        // with(DayOfWeek.SUNDAY) 会找到本周的周日（如果当前是周日则返回当前日期，否则向后找到周日）
        ZonedDateTime firstWeekSunday = firstChatTime.with(DayOfWeek.SUNDAY);

        if (firstChatTime.isAfter(thursdayEnd)) {
            // 在周四之后（周五、周六），下一周周四算第一周
            firstWeekSunday = firstWeekSunday.plusWeeks(1);
        }
        // 否则，在周四结束之前（周日、周一、周二、周三、周四），本周周四就是第一周

        logger.debug("First week Sunday for customer {}: {}", customer.getExternalUserid(), firstWeekSunday);

        // 计算目标周日相对于第一周周日是第几周
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
            firstWeekSunday.toLocalDate(),
            targetSunday.toLocalDate()
        );

        int weekNumber = (int) weeksBetween + 1; // +1 因为第一周是1，不是0

        logger.debug("Week number for customer {} at target Sunday {}: {}", customer.getExternalUserid(), targetSunday, weekNumber);

        // 根据周数返回报告类型
        return switch (weekNumber) {
            case 1 -> TypedReportAnalyser.ReportTypeForFirstWeek;
            case 2 -> TypedReportAnalyser.ReportTypeForSecondWeek;
            case 3 -> TypedReportAnalyser.ReportTypeForThirdWeek;
            case 4 -> TypedReportAnalyser.ReportTypeForFourthWeek;
            default -> {
                if (weekNumber < 1) {
                    logger.warn("Target Sunday {} is before first week Sunday {}, returning empty", targetSunday, firstWeekSunday);
                } else {
                    logger.debug("Week number {} is beyond 4 weeks, returning empty", weekNumber);
                }
                yield ""; // 大于4周或小于1周，返回空
            }
        };
    }


}
