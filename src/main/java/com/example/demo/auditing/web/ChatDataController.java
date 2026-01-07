package com.example.demo.auditing.web;

import com.example.demo.auditing.service.ChatDataSyncService;
import com.example.demo.auditing.service.WeeklyChatAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天数据管理控制器
 */
@RestController
@RequestMapping("/chat-data")
public class ChatDataController {

    @Autowired
    private ChatDataSyncService chatDataSyncService;

    @Autowired
    private WeeklyChatAnalysisService weeklyChatAnalysisService;

    /**
     * 手动触发聊天数据同步
     */
    @PostMapping("/sync")
    public Map<String, Object> syncChatData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endTime) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (startTime != null && endTime != null) {
                // 按时间范围同步
                chatDataSyncService.syncChatDataByTimeRange(startTime, endTime);
                result.put("message", "按时间范围同步聊天数据成功");
            } else {
                // 增量同步
                chatDataSyncService.incrementalSyncChatData();
                result.put("message", "增量同步聊天数据成功");
            }
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "同步失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 手动触发聊天分析
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyzeChatData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime date) {

        Map<String, Object> result = new HashMap<>();


        return result;
    }

    /**
     * 获取同步状态
     */
    @GetMapping("/sync-status")
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        // 这里可以添加实际的状态检查逻辑
        status.put("lastSyncTime", ZonedDateTime.now());
        status.put("status", "running");
        return status;
    }
}
