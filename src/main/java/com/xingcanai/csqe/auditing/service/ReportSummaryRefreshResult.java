package com.xingcanai.csqe.auditing.service;

import java.time.Instant;

public record ReportSummaryRefreshResult(
        ReportSummaryRefreshStatus status,
        long durationMs,
        Instant timestamp) {
}
