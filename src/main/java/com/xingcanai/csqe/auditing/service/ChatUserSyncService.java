package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.entity.WxCardUser;
import com.xingcanai.csqe.auditing.entity.WxCardUserRepository;
import com.xingcanai.csqe.auditing.external.ChatDataApiClient;
import com.xingcanai.csqe.auditing.external.ChatUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 在读学员（卡片用户）同步服务
 */
@Service
public class ChatUserSyncService {

    private static final Logger logger = LoggerFactory.getLogger(ChatUserSyncService.class);

    @Autowired
    private ChatDataApiClient chatDataApiClient;

    @Autowired
    private WxCardUserRepository wxCardUserRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ChatUserSyncService self;

    /**
     * 按更新时间范围同步学员数据
     */
    public void syncChatUsersByUpdateTimeRange(ZonedDateTime updateStartTime, ZonedDateTime updateEndTime) {
        ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
        updateStartTime = updateStartTime.withZoneSameInstant(chinaZone);
        updateEndTime = updateEndTime.withZoneSameInstant(chinaZone);

        logger.info("Syncing chat users from updateTime {} to {}", updateStartTime, updateEndTime);

        try {
            int page = 1;
            int limit = 100;
            boolean hasMore = true;
            int totalSaved = 0;

            while (hasMore) {
                ChatUserResponse response = chatDataApiClient.fetchChatUser(page, limit, updateStartTime, updateEndTime).block();

                if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
                    logger.error("Failed to fetch chat users: response is null or invalid");
                    break;
                }

                List<ChatUserResponse.ChatUserItem> items = response.getData().getList();
                if (items == null || items.isEmpty()) {
                    hasMore = false;
                    break;
                }

                List<WxCardUser> entities = convertToEntities(items);
                int saved = self.saveChatUsers(entities);
                totalSaved += saved;

                logger.info("Synced chat users page {}, {} items, saved {}", page, items.size(), saved);

                if (items.size() < limit) {
                    hasMore = false;
                } else {
                    page++;
                }

                if (page > 1000) {
                    logger.warn("Reached safety limit of 1000 pages, stopping chat user sync");
                    break;
                }
            }

            logger.info("Chat user sync completed. Total saved: {}", totalSaved);
        } catch (Exception e) {
            logger.error("Error during chat user sync: {}", e.getMessage(), e);
        }
    }

    /**
     * 增量同步（从本地最新 updateTime 往后拉取）
     */
    public void incrementalSyncChatUsers() {
        logger.info("Starting incremental chat user sync");
        ZonedDateTime startTime = getStartTime();
        ZonedDateTime endTime = ZonedDateTime.now();
        syncChatUsersByUpdateTimeRange(startTime, endTime);
    }

    private ZonedDateTime getStartTime() {
        WxCardUser lastByUpdate = wxCardUserRepository.findTopByOrderByUpdateTimeDesc();
        if (lastByUpdate != null && lastByUpdate.getUpdateTime() != null) {
            logger.info("Use last chat user updateTime: {}", lastByUpdate.getUpdateTime());
            return lastByUpdate.getUpdateTime();
        }

        WxCardUser lastByCreate = wxCardUserRepository.findTopByOrderByCreateTimeDesc();
        if (lastByCreate != null && lastByCreate.getCreateTime() != null) {
            logger.info("Use last chat user createTime: {}", lastByCreate.getCreateTime());
            return lastByCreate.getCreateTime();
        }

        return ZonedDateTime.now().minusDays(30);
    }

    private List<WxCardUser> convertToEntities(List<ChatUserResponse.ChatUserItem> items) {
        List<WxCardUser> entities = new ArrayList<>();

        for (ChatUserResponse.ChatUserItem item : items) {
            if (item == null || item.getExternalUserid() == null || item.getExternalUserid().isBlank()) {
                continue;
            }

            WxCardUser entity = new WxCardUser();
            entity.setExternalUserid(item.getExternalUserid());
            entity.setExternalName(item.getExternalName());
            entity.setEmployeeQwid(item.getUserid());
            entity.setId(item.getUserid() + "_" + item.getExternalUserid());
            entity.setCardName(item.getCardName());
            entity.setStartTime(item.getStartTime());
            entity.setCreateTime(item.getCreateTime());
            entity.setUpdateTime(item.getUpdateTime());
            entity.setWeekNumber(item.getWeekNumber());
            entities.add(entity);
        }

        return entities;
    }

    /**
     * 批量保存学员数据（upsert 语义：主键相同则更新）
     */
    @Transactional
    public int saveChatUsers(List<WxCardUser> users) {
        int savedCount = 0;
        for (WxCardUser user : users) {
            try {
                wxCardUserRepository.save(user);
                savedCount++;
            } catch (Exception e) {
                logger.warn("Failed to save chat user {}: {}", user.getExternalUserid(), e.getMessage());
            }
        }
        return savedCount;
    }
}
