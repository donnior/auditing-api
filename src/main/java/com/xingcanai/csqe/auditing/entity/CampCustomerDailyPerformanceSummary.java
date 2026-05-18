package com.xingcanai.csqe.auditing.entity;

import java.math.BigDecimal;

public interface CampCustomerDailyPerformanceSummary {

    String getCampTag();

    BigDecimal getGmvAmount();

    BigDecimal getRefundAmount();

    Long getRecordCount();
}
