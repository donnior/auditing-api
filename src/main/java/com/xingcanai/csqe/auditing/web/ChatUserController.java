package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.service.ChatUserSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 学员（卡片用户）同步控制器
 */
@RestController
@RequestMapping("/chat-user")
public class ChatUserController {

    @Autowired
    private ChatUserSyncService chatUserSyncService;

    /**
     * 手动触发学员同步（可选传 updateStartTime/updateEndTime 做增量）
     */
    @PostMapping("/sync")
    public Map<String, Object> syncChatUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime updateStartTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime updateEndTime) {

        Map<String, Object> result = new HashMap<>();
        try {
            if (updateStartTime != null && updateEndTime != null) {
                chatUserSyncService.syncChatUsersByUpdateTimeRange(updateStartTime, updateEndTime);
                result.put("message", "按更新时间范围同步学员数据成功");
            } else {
                chatUserSyncService.incrementalSyncChatUsers();
                result.put("message", "增量同步学员数据成功");
            }
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "同步失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取同步状态（占位）
     */
    @GetMapping("/sync-status")
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastSyncTime", ZonedDateTime.now());
        status.put("status", "running");
        return status;
    }
}

