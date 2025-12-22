package com.example.demo.auditing.job;

import com.example.demo.auditing.service.ChatDataSyncService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 聊天数据同步定时任务
 */
@Component
public class ChatDataSyncJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ChatDataSyncJob.class);

    @Autowired
    private ChatDataSyncService chatDataSyncService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Starting scheduled chat data sync job");

        try {
            // 执行增量同步
            chatDataSyncService.incrementalSyncChatData();

            logger.info("Chat data sync job completed successfully");

        } catch (Exception e) {
            logger.error("Chat data sync job failed: {}", e.getMessage(), e);
            throw new JobExecutionException("Chat data sync job failed", e);
        }
    }
}
