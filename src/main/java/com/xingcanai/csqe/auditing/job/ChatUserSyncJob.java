package com.xingcanai.csqe.auditing.job;

import com.xingcanai.csqe.auditing.service.ChatUserSyncService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 学员（卡片用户）同步定时任务
 */
@Component
public class ChatUserSyncJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ChatUserSyncJob.class);

    @Autowired
    private ChatUserSyncService chatUserSyncService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Starting scheduled chat user sync job");
        try {
            chatUserSyncService.incrementalSyncChatUsers();
            logger.info("Chat user sync job completed successfully");
        } catch (Exception e) {
            logger.error("Chat user sync job failed: {}", e.getMessage(), e);
            throw new JobExecutionException("Chat user sync job failed", e);
        }
    }
}

