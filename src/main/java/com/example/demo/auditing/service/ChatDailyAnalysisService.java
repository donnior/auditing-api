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
public class ChatDailyAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ChatAnalysisService.class);

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
        runAnalysis(ZonedDateTime.now());
    }

    public void runAnalysis(String targetDate) {
        runAnalysis(ZonedDateTime.parse(targetDate + "T00:00:00Z"));
    }

    public void runAnalysis(ZonedDateTime time) {
        // var lastSunday = time.with(DayOfWeek.SUNDAY);
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
                executorService.submit(() -> runCustomerAnalysisWithType(employee, customer, fromTime, firstChatTime.plusHours(48), TypedReportAnalyser.ReportTypeForWithin48Hours, reportName));
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
            evaluationDetailRepository.save(evaluationDetail);
        }
    }

    private List<WxChatMessage> getMessages(Employee employee, String customerId, ZonedDateTime fromTime, ZonedDateTime endTime) {
        return wxChatMessageRepository.findChatBetweenEmployeeAndCustomer(employee.getQwId(), customerId, fromTime, endTime);
    }

}
