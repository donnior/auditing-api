package com.xingcanai.csqe.auditing.web;

import lombok.Data;

/**
 * 周报汇总查询参数
 */
@Data
public class WeeklyReportSummaryQueryParams {

    /**
     * 员工ID
     */
    private String employeeId;

    /**
     * 评估周期
     */
    private String evalPeriod;

    /**
     * 评估类型
     */
    private String evalType;
}
