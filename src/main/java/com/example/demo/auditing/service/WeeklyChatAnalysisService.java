package com.example.demo.auditing.service;

import java.time.DayOfWeek;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.auditing.entity.Employee;
import com.example.demo.auditing.entity.EmployeeRepository;
import com.example.demo.auditing.entity.EvaluationDetailRepository;
import com.example.demo.auditing.entity.WxChatMessage;
import com.example.demo.auditing.entity.WxChatMessageRepository;

/**
 * 聊天分析服务
 */
@Service
public class WeeklyChatAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyChatAnalysisService.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WxChatMessageRepository wxChatMessageRepository;

    @Autowired
    private TypedReportAnalyser typedReportAnalyser;

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

    public void runWeeklyAnalysis() {
        runWeeklyAnalysis(ZonedDateTime.now());
    }

    // 2026-01-05
    public void runWeeklyAnalysis(String targetDate) {
        runWeeklyAnalysis(ZonedDateTime.parse(targetDate + "T00:00:00Z"));
    }

    public void runWeeklyAnalysis(ZonedDateTime time) {
        var lastSunday = time.with(DayOfWeek.SUNDAY).minusWeeks(1);
        var employees = employeeRepository.findAll();
        for (var employee : employees) {
            System.out.println("employee: " + employee);
            runWeeklyAnalysisForEmployee(employee, lastSunday);
        }
    }

    private void runWeeklyAnalysisForEmployee(Employee employee, ZonedDateTime targetSunday) {
        var reportName = targetSunday.toLocalDate().toString();

        var fromMonday = targetSunday.with(DayOfWeek.MONDAY);
        var toMonday = fromMonday.plusDays(7);

        ZonedDateTime fromTime = fromMonday.withHour(0).withMinute(0).withSecond(0);
        ZonedDateTime toTime = toMonday.withHour(0).withMinute(0).withSecond(0);

        var customers = wxChatMessageRepository.findCustomersByEmployeeAndTimeRange(employee.getQwId(), fromTime, toTime);

        for (var customer : customers) {
            var reportType = getReportTypeForCustomer(employee, customer, targetSunday);
            if (typedReportAnalyser.getReportTypes().contains(reportType)) {
                executorService.submit(() -> runCustomerAnalysisWithType(employee, customer, fromTime, toTime, reportType, reportName));
            }
        }
    }

    private String getReportTypeForCustomer(Employee employee, String customerId, ZonedDateTime targetSunday) {
        // 获取员工和客户之间最早的一条聊天记录
        WxChatMessage firstChat = wxChatMessageRepository.findFirstChatBetweenEmployeeAndCustomer(employee.getQwId(), customerId);
        if (firstChat == null) {
            logger.warn("No chat history found between employee {} and customer {}", employee.getQwId(), customerId);
            return ""; // 没有聊天记录，返回空
        }

        ZonedDateTime firstChatTime = firstChat.getMsgTime();
        logger.debug("First chat time between employee {} and customer {}: {}", employee.getQwId(), customerId, firstChatTime);

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

        logger.debug("First week Sunday for customer {}: {}", customerId, firstWeekSunday);

        // 计算目标周日相对于第一周周日是第几周
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
            firstWeekSunday.toLocalDate(),
            targetSunday.toLocalDate()
        );

        int weekNumber = (int) weeksBetween + 1; // +1 因为第一周是1，不是0

        logger.debug("Week number for customer {} at target Sunday {}: {}", customerId, targetSunday, weekNumber);

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

    private void runCustomerAnalysisWithType(Employee employee, String customer, ZonedDateTime fromTime, ZonedDateTime toTime, String reportType, String reportName) {
        var messages = getMessages(employee, customer, fromTime, toTime);
        var evaluationDetail = typedReportAnalyser.runAnalysisForCustomer(employee, customer, messages, reportType);
        if (evaluationDetail != null) {
            evaluationDetail.setEmployeeId(employee.getId());
            evaluationDetail.setEmployeeQwId(employee.getQwId());
            evaluationDetail.setCustomerId(customer);
            evaluationDetail.setCustomerName(customer);
            evaluationDetail.setEvalTime(ZonedDateTime.now().toString());
            evaluationDetail.setEvalPeriod(reportName);
            evaluationDetail.setEvalType(reportType);
            evaluationDetail.setChatStartTime(fromTime);
            evaluationDetail.setChatEndTime(toTime);

            // 检查是否已存在记录，如果存在则替换（使用已有的ID）
            var existingDetail = evaluationDetailRepository.findByEmployeeIdAndCustomerIdAndEvalTypeAndEvalPeriod(
                employee.getId(), customer, reportType, reportName);
            if (existingDetail.isPresent()) {
                evaluationDetail.setId(existingDetail.get().getId());
            }

            evaluationDetailRepository.save(evaluationDetail);
        }
    }

    private List<WxChatMessage> getMessages(Employee employee, String customerId, ZonedDateTime fromTime, ZonedDateTime endTime) {
        return wxChatMessageRepository.findChatBetweenEmployeeAndCustomer(employee.getQwId(), customerId, fromTime, endTime);
    }

}
