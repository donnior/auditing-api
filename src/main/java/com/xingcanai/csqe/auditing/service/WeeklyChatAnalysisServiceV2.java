package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EvaluationDetailRepository;
import com.xingcanai.csqe.auditing.entity.WxCardUser;
import com.xingcanai.csqe.auditing.entity.WxCardUserRepository;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;

/**
 * 聊天分析服务
 */
@Service
public class WeeklyChatAnalysisServiceV2 extends AbstractChatAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyChatAnalysisServiceV2.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private static final DateTimeFormatter CAMP_TAG_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter SHORT_CAMP_TAG_FORMATTER = DateTimeFormatter.ofPattern("MMdd");

    @Autowired
    private WxCardUserRepository wxCardUserRepository;

    @Autowired
    private WxChatMessageRepository wxChatMessageRepository;

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

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
     * 指定时间的上一个业务周末（周六）作为分析目标
     * @param time
     */
    private void doRunAnalysis(ZonedDateTime simulatedRunningTime) {
        var targetWeekend = simulatedRunningTime.with(DayOfWeek.SATURDAY).minusWeeks(1);
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
                if (!hasChatInRange(employee, customer, chatRangeStart, targetSundayEndTime)) {
                    deleteExistingDetail(employee, customer, reportType, reportPeriod);
                    logger.debug("Skip weekly analysis because no chat found for employee {} customer {} reportType {} period {} between {} and {}",
                            employee.getQwId(),
                            customer.getExternalUserid(),
                            reportType,
                            reportPeriod,
                            chatRangeStart,
                            targetSundayEndTime);
                    continue;
                }
                executorService.submit(() -> runCustomerAnalysisWithType(employee, customer, chatRangeStart, targetSundayEndTime, reportType, reportPeriod, bizDate));
            }
        }
    }

    private boolean hasChatInRange(Employee employee, WxCardUser customer, ZonedDateTime startTime, ZonedDateTime endTime) {
        return wxChatMessageRepository.countChatBetweenEmployeeAndCustomer(
                employee.getQwId(),
                customer.getExternalUserid(),
                startTime,
                endTime) > 0;
    }

    private void deleteExistingDetail(Employee employee, WxCardUser customer, String reportType, String reportPeriod) {
        long deleted = evaluationDetailRepository.deleteByEmployeeIdAndCustomerIdAndEvalTypeAndEvalPeriod(
                employee.getId(),
                customer.getExternalUserid(),
                reportType,
                reportPeriod);
        if (deleted > 0) {
            logger.info("Deleted stale weekly analysis detail because no chat found: employee {} customer {} reportType {} period {}",
                    employee.getQwId(),
                    customer.getExternalUserid(),
                    reportType,
                    reportPeriod);
        }
    }


    private List<WxCardUser> getLatest4WeeksCustomers(Employee employee, ZonedDateTime targetSunday) {
        return wxCardUserRepository.findByEmployeeQwidAndCampTags(
                employee.getQwId(),
                getLatest4CampTags(targetSunday));
    }

    private List<String> getLatest4CampTags(ZonedDateTime targetPeriodEnd) {
        List<String> tags = new ArrayList<>();
        LocalDate targetDate = targetPeriodEnd.toLocalDate();
        for (int i = 0; i < 4; i++) {
            LocalDate campDate = targetDate.minusWeeks(i);
            tags.add(campDate.format(CAMP_TAG_FORMATTER));
            tags.add(campDate.format(SHORT_CAMP_TAG_FORMATTER));
        }
        return tags;
    }

    private ZonedDateTime getChatTimeRangeStart(WxCardUser customer, ZonedDateTime targetSundayEndTime, String reportType) {
        ZonedDateTime chatRangeStart = customer.getStartTime().minusMinutes(1);
        if (!TypedReportAnalyser.ReportTypeForFirstWeek.equals(reportType)) {
            chatRangeStart = targetSundayEndTime.minusDays(7);
        }
        return chatRangeStart;
    }


    private String getReportTypeForCustomer(Employee employee, WxCardUser customer, ZonedDateTime targetSunday) {
        int weekNumber =  WeekNumberCalculator.calculateReportType(customer.getCampTag(), targetSunday);
        logger.debug("Week number for employee {} customer {} campTag {} target {}: {}",
                employee.getQwId(),
                customer.getExternalUserid(),
                customer.getCampTag(),
                targetSunday,
                weekNumber);

        return switch (weekNumber) {
            case 1 -> TypedReportAnalyser.ReportTypeForFirstWeek;
            case 2 -> TypedReportAnalyser.ReportTypeForSecondWeek;
            case 3 -> TypedReportAnalyser.ReportTypeForThirdWeek;
            case 4 -> TypedReportAnalyser.ReportTypeForFourthWeek;
            default -> {
                logger.warn("campTag {} does not map to target period {}, returning empty",
                        customer.getCampTag(),
                        targetSunday.toLocalDate());
                yield ""; // 大于4周或小于1周，返回空
            }
        };
    }

}
