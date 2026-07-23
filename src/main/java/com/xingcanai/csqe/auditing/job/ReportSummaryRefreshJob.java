package com.xingcanai.csqe.auditing.job;

import com.xingcanai.csqe.auditing.service.ReportSummaryRefreshService;
import com.xingcanai.csqe.auditing.service.ReportSummaryRefreshStatus;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportSummaryRefreshJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReportSummaryRefreshJob.class);

    @Autowired
    private ReportSummaryRefreshService reportSummaryRefreshService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var result = reportSummaryRefreshService.refresh();
        if (ReportSummaryRefreshStatus.FAILED.equals(result.status())) {
            logger.error("Report summary refresh job failed in {} ms", result.durationMs());
            return;
        }

        logger.info("Report summary refresh job finished with status {} in {} ms",
                result.status(),
                result.durationMs());
    }
}
