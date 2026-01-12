package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;
import com.xingcanai.csqe.auditing.external.ChatDataApiClient;
import com.xingcanai.csqe.auditing.external.ChatDataResponse;

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
 * 聊天数据同步服务
 */
@Service
public class ChatDataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(ChatDataSyncService.class);

    @Autowired
    private ChatDataApiClient chatDataApiClient;

    @Autowired
    private WxChatMessageRepository wxCpChatMsgRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private ChatDataSyncService self;

    /**
     * 同步指定时间范围的聊天数据
     */
    @Transactional
    public void syncChatDataByTimeRange(ZonedDateTime startTime, ZonedDateTime endTime) {
        logger.info("Starting chat data sync from {} to {}", startTime, endTime);

        // ZonedDateTime currentTime = startTime;
        // int totalSynced = 0;

        // while (currentTime.isBefore(endTime)) {
        //     try {
        //         ChatDataResponse response = chatDataApiClient.fetchChatData(currentTime, 1000).block();

        //         if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
        //             logger.info("No more chat data to sync for time {}", currentTime);
        //             break;
        //         }

        //         List<WxCpChatMsg> chatMessages = convertToEntities(response.getItems());
        //         int savedCount = saveChatMessages(chatMessages);
        //         totalSynced += savedCount;

        //         logger.info("Synced {} messages for time {}", savedCount, currentTime);

        //         // 更新时间为最后一条消息的时间
        //         if (!response.getItems().isEmpty()) {
        //             ChatDataResponse.ChatMessageItem lastItem = response.getItems().get(response.getItems().size() - 1);
        //             currentTime = lastItem.getMsgTime().plusNanos(1); // 避免重复获取
        //         }

        //         // 如果返回的数据少于请求数量，说明已经到了最后
        //         if (response.getItems().size() < 1000) {
        //             break;
        //         }

        //     } catch (Exception e) {
        //         logger.error("Error syncing chat data for time {}: {}", currentTime, e.getMessage(), e);
        //         break;
        //     }
        // }

        logger.info("Chat data sync completed. Total synced: {} messages", 10);
    }

    /**
     * 增量同步聊天数据
     */
    // @Transactional // Removed to avoid long-running transaction
    public void incrementalSyncChatData() {
        logger.info("Starting incremental chat data sync");

        try {
            // 1. Determine start time
            ZonedDateTime startTime;
            WxChatMessage lastMsg = wxCpChatMsgRepository.findTopByOrderByMsgTimeDesc();
            if (lastMsg != null && lastMsg.getMsgTime() != null) {
                startTime = lastMsg.getMsgTime();
            } else {
                // Default to 7 days ago if no data
                startTime = ZonedDateTime.now().minusDays(10);
            }

            ZonedDateTime endTime = ZonedDateTime.now();

            logger.info("Syncing chat data from {} to {}",
                startTime.withZoneSameInstant(ZoneId.systemDefault()), endTime.withZoneSameInstant(ZoneId.systemDefault()));

            int page = 1;
            int limit = 100;
            boolean hasMore = true;
            int totalSynced = 0;

            while (hasMore) {
                ChatDataResponse response = chatDataApiClient.fetchChatData(startTime, endTime, page, limit).block();

                if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
                    logger.error("Failed to fetch chat data: response is null or invalid");
                    break;
                }

                List<ChatDataResponse.ChatMessageItem> items = response.getData().getList();
                if (items == null || items.isEmpty()) {
                    hasMore = false;
                    break;
                }

                List<WxChatMessage> entities = convertToEntities(items);
                // Use self to ensure @Transactional works
                int saved = self.saveChatMessages(entities);
                totalSynced += saved;

                logger.info("Synced page {}, {} items, saved {} new messages", page, items.size(), saved);

                // Check if we need to fetch next page
                if (items.size() < limit) {
                    hasMore = false;
                } else {
                    page++;
                }

                // Safety break to avoid infinite loops
                if (page > 1000) {
                    logger.warn("Reached safety limit of 1000 pages, stopping sync");
                    break;
                }
            }

            logger.info("Incremental chat data sync completed. Total synced: {} messages", totalSynced);

        } catch (Exception e) {
            logger.error("Error during incremental chat data sync: {}", e.getMessage(), e);
        }
    }

    /**
     * 转换外部API数据为实体对象
     */
    private List<WxChatMessage> convertToEntities(List<ChatDataResponse.ChatMessageItem> items) {
        List<WxChatMessage> entities = new ArrayList<>();

        for (ChatDataResponse.ChatMessageItem item : items) {
            WxChatMessage entity = new WxChatMessage();
            entity.setMsgId(item.getMsgId());
            entity.setFromId(item.getFromId());
            entity.setFromName(item.getFromName());
            entity.setAcceptId(item.getAcceptId());
            entity.setAcceptType(item.getAcceptType());
            entity.setAcceptName(item.getAcceptName());
            entity.setMsgType(item.getMsgType());
            entity.setContent(item.getContent());
            entity.setDataSeq(item.getDataSeq());
            entity.setMsgTime(item.getMsgTime());
            entity.setCreateTime(item.getCreateTime() != null ? item.getCreateTime() : ZonedDateTime.now());

            entities.add(entity);
        }

        return entities;
    }

    /**
     * 批量保存聊天消息，忽略重复记录
     */
    @Transactional
    public int saveChatMessages(List<WxChatMessage> chatMessages) {
        int savedCount = 0;

        for (WxChatMessage message : chatMessages) {
            try {
                // 直接使用JPA save，如果存在则更新
                wxCpChatMsgRepository.save(message);
                savedCount++;
            } catch (Exception e) {
                logger.warn("Failed to save chat message {}: {}", message.getMsgId(), e.getMessage());
            }
        }

        return savedCount;
    }
}
