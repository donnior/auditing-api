package com.example.demo.auditing.config;

import com.example.demo.auditing.job.ChatDataSyncJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz调度器配置
 */
@Configuration
public class QuartzSchedulerConfig {

    /**
     * 聊天数据同步任务配置
     */
    @Bean
    public JobDetail chatDataSyncJobDetail() {
        return JobBuilder.newJob(ChatDataSyncJob.class)
                .withIdentity("chatDataSyncJob", "syncGroup")
                .withDescription("Chat data synchronization job")
                .storeDurably()
                .build();
    }

    /**
     * 聊天数据同步任务触发器 - 每小时执行一次
     */
    @Bean
    public Trigger chatDataSyncJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(chatDataSyncJobDetail())
                .withIdentity("chatDataSyncTrigger", "syncGroup")
                .withDescription("Trigger for chat data sync job")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?")) // 每小时整点执行
                .build();
    }

    // /**
    //  * 聊天分析任务配置
    //  */
    // @Bean
    // public JobDetail chatAnalysisJobDetail() {
    //     return JobBuilder.newJob(ChatAnalysisJob.class)
    //             .withIdentity("chatAnalysisJob", "analysisGroup")
    //             .withDescription("Chat analysis job")
    //             .storeDurably()
    //             .build();
    // }

    // /**
    //  * 聊天分析任务触发器 - 每天凌晨2点执行
    //  */
    // @Bean
    // public Trigger chatAnalysisJobTrigger() {
    //     return TriggerBuilder.newTrigger()
    //             .forJob(chatAnalysisJobDetail())
    //             .withIdentity("chatAnalysisTrigger", "analysisGroup")
    //             .withDescription("Trigger for chat analysis job")
    //             .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?")) // 每天凌晨2点执行
    //             .build();
    // }
}
