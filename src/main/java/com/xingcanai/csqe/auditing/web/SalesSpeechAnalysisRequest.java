package com.xingcanai.csqe.auditing.web;

import java.time.LocalDate;

public record SalesSpeechAnalysisRequest(
        String employeeId,
        LocalDate evalPeriod,
        Boolean regenerate) {

    public boolean shouldRegenerate() {
        return Boolean.TRUE.equals(regenerate);
    }
}

