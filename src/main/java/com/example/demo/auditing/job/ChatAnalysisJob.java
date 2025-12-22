package com.example.demo.auditing.job;

import com.example.demo.auditing.service.ChatAnalysisService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * 聊天分析定时任务
 */
@Component
public class ChatAnalysisJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ChatAnalysisJob.class);

    @Autowired
    private ChatAnalysisService chatAnalysisService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Starting scheduled chat analysis job");

        try {
            // 分析前一天的聊天数据
            ZonedDateTime yesterday = ZonedDateTime.now().minusDays(1);

            //TODO
            // chatAnalysisService.analyzeEmployeeChatForDate(yesterday);

            logger.info("Chat analysis job completed successfully for date: {}",
                       yesterday.toLocalDate());

        } catch (Exception e) {
            logger.error("Chat analysis job failed: {}", e.getMessage(), e);
            throw new JobExecutionException("Chat analysis job failed", e);
        }
    }
}
