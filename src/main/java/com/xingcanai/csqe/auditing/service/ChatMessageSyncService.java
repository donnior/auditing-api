package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;
import com.xingcanai.csqe.auditing.external.ChatDataApiClient;
import com.xingcanai.csqe.auditing.external.ChatDataResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天数据同步服务
 */
@Service
public class ChatMessageSyncService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageSyncService.class);

    @Autowired
    private ChatDataApiClient chatDataApiClient;

    @Autowired
    private WxChatMessageRepository wxCpChatMsgRepository;

    @Autowired
    private ChatMessageSavingService chatMessageSavingService;

    /**
     * 同步指定时间范围的聊天数据
     */
    public void syncChatDataByTimeRange(ZonedDateTime startTime, ZonedDateTime endTime) {
        ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
        startTime = startTime.withZoneSameInstant(chinaZone);
        endTime = endTime.withZoneSameInstant(chinaZone);

        logger.info("Syncing chat data from {} to {}", startTime, endTime);

        try {
            int page = 1;
            int limit = 100;
            boolean hasMore = true;
            int totalSynced = 0;

            while (hasMore) {
                ChatDataResponse response = chatDataApiClient.fetchChatData(startTime, endTime, page, limit).block();

                if (response == null || response.getCode() == null || response.getCode() != 200
                        || response.getData() == null) {
                    logger.error("Failed to fetch chat data: response is null or invalid");
                    break;
                }

                List<ChatDataResponse.ChatMessageItem> items = response.getData().getList();
                if (items == null || items.isEmpty()) {
                    hasMore = false;
                    break;
                }

                List<WxChatMessage> entities = convertToEntities(items);
                int saved = chatMessageSavingService.saveChatMessages(entities);
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

    public void incrementalSyncChatData() {
        logger.info("Starting incremental chat data sync");
        ZonedDateTime startTime = getStartTime();
        ZonedDateTime endTime = ZonedDateTime.now();
        syncChatDataByTimeRange(startTime, endTime);

    }

    private ZonedDateTime getStartTime() {
        WxChatMessage lastMsg = wxCpChatMsgRepository.findTopByOrderByMsgTimeDesc();
        if (lastMsg != null && lastMsg.getMsgTime() != null) {
            logger.info("Use last message time: {}", lastMsg.getMsgTime());
            return lastMsg.getMsgTime();
        }
        return ZonedDateTime.now().minusDays(14);
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


}
