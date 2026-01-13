package com.xingcanai.csqe.auditing.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.auditing.entity.EvaluationDetailRepository;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;

/**
 * 聊天分析服务
 */
public abstract class AbstractChatAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractChatAnalysisService.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(16);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WxChatMessageRepository wxChatMessageRepository;

    @Autowired
    private TypedReportAnalyser typedReportAnalyser;

    @Autowired
    private EvaluationDetailRepository evaluationDetailRepository;

    public abstract void runAnalysis();

    public abstract void runAnalysis(String targetDate);


    protected void runCustomerAnalysisWithType(Employee employee, String customer, ZonedDateTime fromTime, ZonedDateTime toTime, String reportType, String reportName, String bizDate) {
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
            evaluationDetail.setBizDate(bizDate);

            // 检查是否已存在记录，如果存在则替换（使用已有的ID）
            var existingDetail = evaluationDetailRepository.findByEmployeeIdAndCustomerIdAndEvalTypeAndEvalPeriod(
                employee.getId(), customer, reportType, reportName);
            if (existingDetail.isPresent()) {
                evaluationDetail.setId(existingDetail.get().getId());
            }
            evaluationDetailRepository.save(evaluationDetail);
        }
    }

    protected List<WxChatMessage> getMessages(Employee employee, String customerId, ZonedDateTime fromTime, ZonedDateTime endTime) {
        long start = System.currentTimeMillis();
        var messages = wxChatMessageRepository.findChatBetweenEmployeeAndCustomer(employee.getQwId(), customerId, fromTime, endTime);
        long end = System.currentTimeMillis();
        System.out.println("getMessages time: " + (end - start));
        return messages;
    }

    protected boolean isReportTypeSupported(String reportType) {
        return typedReportAnalyser.getReportTypes().contains(reportType);
    }

    protected List<Employee> getActiveEmployees() {
        var employees = employeeRepository.findAll();
        return employees.stream().filter(employee -> employee.getStatus() == Employee.STATUS_ACTIVE).toList();
    }

}
