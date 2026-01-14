package com.xingcanai.csqe.auditing.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EvaluationDetail;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.util.Strings;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TypedReportAnalyser {

    public static final String thirdWeekReportAgentId = "";
    public static final String forthWeekReportAgentId = "";  // 7589909383191543817
    public static final String firstWeekReportAgentId = "7589575763905577006";
    public static final String secondWeekReportAgentId = "7589575763905577006";
    public static final String within48HoursReportAgentId = "7586960559527510035";

    public static final String ReportTypeForFirstWeek = "FIRST_WEEK";
    public static final String ReportTypeForSecondWeek = "SECOND_WEEK";
    public static final String ReportTypeForThirdWeek = "THIRD_WEEK";
    public static final String ReportTypeForFourthWeek = "FOURTH_WEEK";
    public static final String ReportTypeForWithin48Hours = "WITHIN_48_HOURS";

    @Autowired
    private Coze coze;

    @Autowired
    private SimpleResponseEvaluationParser simpleResponseEvaluationParser;

    public EvaluationDetail runAnalysisForCustomer(Employee employee, String customerId, List<WxChatMessage> messages, String reportType) {
        log.info("Run [{}] report for employee [{}] and customer [{}]", reportType, employee.getQwId(), customerId);
        var reportAgentId = switch (reportType) {
            case ReportTypeForFirstWeek -> firstWeekReportAgentId;
            case ReportTypeForSecondWeek -> secondWeekReportAgentId;
            case ReportTypeForThirdWeek -> thirdWeekReportAgentId;
            case ReportTypeForFourthWeek -> forthWeekReportAgentId;
            case ReportTypeForWithin48Hours -> within48HoursReportAgentId;
            default -> throw new IllegalArgumentException("Invalid report type: " + reportType);
        };
        if (Strings.isNotEmpty(reportAgentId)) {
            var messagesPrompt = messagesPrompt(employee, messages);
            log.info("Call agent {} with prompt: {}", reportAgentId, messagesPrompt.substring(0, 50)+"...");
            var response = coze.chat(reportAgentId, messagesPrompt);
            log.debug("Agent response: {}", response);
            return parseResponse(response, reportType);
        } else {
            log.info("No agent id for report type: {}", reportType);
            return null;
        }
    }

    public EvaluationDetail parseResponse(String response, String reportType) {
        return simpleResponseEvaluationParser.parseResponse(response, reportType);
    }

    private String messagesPrompt(Employee employee, List<WxChatMessage> messages) {
        return messages
                .stream()
                .map(message -> message.getMsgTime() + " (" + message.getMsgTime().getDayOfWeek().toString() + ") | "
                        + (message.getFromId().equals(employee.getQwId()) ? "客服: " : "家长: ") + message.getContent())
                .collect(Collectors.joining("\n"));
    }

    public List<String> getReportTypes() {
        return List.of(ReportTypeForFirstWeek, ReportTypeForSecondWeek, ReportTypeForThirdWeek, ReportTypeForFourthWeek, ReportTypeForWithin48Hours);
    }

}
