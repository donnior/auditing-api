package com.xingcanai.csqe.auditing.job;

import com.xingcanai.csqe.auditing.service.CampCustomerDailyPerformanceSyncService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 学员每日业绩同步定时任务。
 */
@Component
public class CampCustomerDailyPerformanceSyncJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(CampCustomerDailyPerformanceSyncJob.class);

    @Autowired
    private CampCustomerDailyPerformanceSyncService syncService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Starting scheduled camp customer daily performance sync job");
        try {
            syncService.incrementalSync();
            logger.info("Camp customer daily performance sync job completed successfully");
        } catch (Exception e) {
            logger.error("Camp customer daily performance sync job failed: {}", e.getMessage(), e);
            throw new JobExecutionException("Camp customer daily performance sync job failed", e);
        }
    }
}
