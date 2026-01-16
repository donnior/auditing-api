package com.xingcanai.csqe.auditing.entity;

public enum WeeklyReportDetailMetric {
    TOTAL_CUSTOMERS("total_customers"),
    MATERIAL_SEND("material_send"),
    COURSE_REMIND("course_remind"),
    HOMEWORK_PUBLISH("homework_publish"),
    WEEK_MATERIAL_SEND("week_material_send"),
    SUNDAY_LINK_SEND("sunday_link_send"),
    FEEDBACK_TRACK("feedback_track"),
    RISK_WORD_TRIGGER("risk_word_trigger"),
    INTRODUCE_TEACHER("introduce_teacher"),
    INTRODUCE_COURSE("introduce_course"),
    INTRODUCE_SCHEDULE("introduce_schedule"),
    INTRODUCE_COURSE_TIME("introduce_course_time"),
    ORDER_CHECK("order_check"),
    INTRODUCE_COMPLETED("introduce_completed");

    private final String paramValue;

    WeeklyReportDetailMetric(String paramValue) {
        this.paramValue = paramValue;
    }

    public static WeeklyReportDetailMetric fromParam(String param) {
        if (param == null || param.isBlank()) {
            throw new IllegalArgumentException("metric 参数不能为空");
        }
        for (WeeklyReportDetailMetric metric : values()) {
            if (metric.paramValue.equalsIgnoreCase(param)) {
                return metric;
            }
        }
        throw new IllegalArgumentException("未知的 metric 参数: " + param);
    }
}
