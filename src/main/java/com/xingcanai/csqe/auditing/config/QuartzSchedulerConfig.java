package com.xingcanai.csqe.auditing.config;

import com.xingcanai.csqe.auditing.job.WeeklyChatAnalysisJob;
import com.xingcanai.csqe.auditing.job.ChatDataSyncJob;
import com.xingcanai.csqe.auditing.job.DailyChatAnalysisJob;

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
     * 聊天数据同步任务触发器 - 每4小时执行一次
     */
    @Bean
    public Trigger chatDataSyncJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(chatDataSyncJobDetail())
                .withIdentity("chatDataSyncTrigger", "syncGroup")
                .withDescription("Trigger for chat data sync job")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0/4 * * ?")) // 每4小时整点执行
                .build();
    }

    /**
     * 每日聊天分析任务配置
     */
    @Bean
    public JobDetail dailyChatAnalysisJobDetail() {
        return JobBuilder.newJob(DailyChatAnalysisJob.class)
                .withIdentity("dailyChatAnalysisJob", "analysisGroup")
                .withDescription("Daily chat analysis job")
                .storeDurably()
                .build();
    }

    /**
     * 聊天分析任务触发器 - 每天10点执行
     */
    @Bean
    public Trigger dailyChatAnalysisJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(dailyChatAnalysisJobDetail())
                .withIdentity("dailyChatAnalysisTrigger", "analysisGroup")
                .withDescription("Trigger for daily chat analysis job")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 10 * * ?")) // 每天10点执行
                .build();
    }

    /**
     * 每周聊天分析任务配置
     */
    @Bean
    public JobDetail weeklyChatAnalysisJobDetail() {
        return JobBuilder.newJob(WeeklyChatAnalysisJob.class)
                .withIdentity("weeklyChatAnalysisJob", "analysisGroup")
                .withDescription("Weekly chat analysis job")
                .storeDurably()
                .build();
    }

    /**
     * 每周聊天分析任务触发器 - 每周一16点执行
     */
    @Bean
    public Trigger weeklyChatAnalysisJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(weeklyChatAnalysisJobDetail())
                .withIdentity("weeklyChatAnalysisTrigger", "analysisGroup")
                .withDescription("Trigger for weekly chat analysis job")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 12 ? * 1")) // 每周一12点执行
                .build();
    }

}
