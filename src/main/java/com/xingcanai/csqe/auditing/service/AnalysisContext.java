package com.xingcanai.csqe.auditing.service;

import java.time.ZonedDateTime;
import java.util.List;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.WxChatMessage;

public record AnalysisContext(
    Employee employee,
    String customerId,
    String customerName,
    ZonedDateTime chatFromTime,
    ZonedDateTime chattoTime,
    String reportType,
    String reportPeriod,
    String bizDate,
    List<WxChatMessage> messages
) {

}
