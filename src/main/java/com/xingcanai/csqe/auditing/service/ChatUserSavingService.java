package com.xingcanai.csqe.auditing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xingcanai.csqe.auditing.entity.WxCardUser;
import com.xingcanai.csqe.auditing.entity.WxCardUserRepository;

import java.util.List;

@Service
public class ChatUserSavingService {

    private static final Logger logger = LoggerFactory.getLogger(ChatUserSavingService.class);

    @Autowired
    private WxCardUserRepository wxCardUserRepository;

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
