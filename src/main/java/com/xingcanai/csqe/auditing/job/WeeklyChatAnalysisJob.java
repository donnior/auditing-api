package com.xingcanai.csqe.auditing.job;

import com.xingcanai.csqe.auditing.service.WeeklyChatAnalysisService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 聊天分析定时任务
 */
@Component
public class WeeklyChatAnalysisJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyChatAnalysisJob.class);

    @Autowired
    private WeeklyChatAnalysisService weeklyChatAnalysisService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Starting scheduled chat analysis job");

        try {
            weeklyChatAnalysisService.runAnalysis();
            logger.info("Weekly chat analysis job started");

        } catch (Exception e) {
            logger.error("Chat analysis job failed: {}", e.getMessage(), e);
            throw new JobExecutionException("Chat analysis job failed", e);
        }
    }
}
