package com.xingcanai.csqe.auditing.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xingcanai.csqe.auditing.entity.WxChatMessage;
import com.xingcanai.csqe.auditing.entity.WxChatMessageRepository;

@Service
public class ChatMessageSavingService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageSavingService.class);

    @Autowired
    private WxChatMessageRepository wxChatMessageRepository;

    /**
     * 批量保存聊天消息，忽略重复记录
     */
    @Transactional
    public int saveChatMessages(List<WxChatMessage> chatMessages) {
        int savedCount = 0;

        for (WxChatMessage message : chatMessages) {
            try {
                // 直接使用JPA save，如果存在则更新
                wxChatMessageRepository.save(message);
                savedCount++;
            } catch (Exception e) {
                logger.warn("Failed to save chat message {}: {}", message.getMsgId(), e.getMessage());
            }
        }

        return savedCount;
    }

}
