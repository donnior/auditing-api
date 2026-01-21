package com.xingcanai.csqe.auditing.job;

import com.xingcanai.csqe.auditing.service.DailyChatAnalysisServiceV2;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DailyChatAnalysisJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(DailyChatAnalysisJob.class);

    @Autowired
    private DailyChatAnalysisServiceV2 dailyChatAnalysisService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            dailyChatAnalysisService.runAnalysis();
            logger.info("Daily chat analysis job started");

        } catch (Exception e) {
            logger.error("Chat analysis job failed: {}", e.getMessage(), e);
            throw new JobExecutionException("Chat analysis job failed", e);
        }
    }
}
