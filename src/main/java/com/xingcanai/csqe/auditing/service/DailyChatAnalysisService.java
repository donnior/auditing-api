package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.auditing.entity.EvaluationDetailRepository;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;

/**
 * 聊天分析服务
 */
@Service
public class DailyChatAnalysisService {

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

    public void runAnalysis() {
        var now = ZonedDateTime.now();
        /**
         * 以当前时间的0点为基准运行分析，跑前一天的数据
         */
        var targetDate = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        runAnalysis(targetDate);
        // runAnalysis("2026-01-01");
    }

    /**
     * 以指定日期的0点基准运行分析，跑前一天的数据
     */
    public void runAnalysis(String targetDate) {
        var target = ZonedDateTime.parse(targetDate + "T00:01:00Z");
        runAnalysis(target);
    }

    public void runAnalysis(ZonedDateTime time) {
        var employees = employeeRepository.findAll();
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

            if(firstChatTime.isAfter(fromTime) && firstChatTime.isBefore(rangeEnd)) {
                CompletableFuture.runAsync(
                    () -> runCustomerAnalysisWithType(employee, customer, fromTime, firstChatTime.plusHours(48), TypedReportAnalyser.ReportTypeForWithin48Hours,reportName),
                    executorService
                ).exceptionally(ex -> {
                            logger.error("Error running customer analysis for employee {} and customer {}", employee.getQwId(), customer, ex);
                            return null;
                });

                // executorService.execute(() -> runCustomerAnalysisWithType(employee, customer, fromTime, firstChatTime.plusHours(48), TypedReportAnalyser.ReportTypeForWithin48Hours, reportName));
            }
        }
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
