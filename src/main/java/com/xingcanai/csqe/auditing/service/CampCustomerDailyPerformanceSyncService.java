package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformance;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformanceRepository;
import com.xingcanai.csqe.auditing.external.CampCustomerDailyPerformanceResponse;
import com.xingcanai.csqe.auditing.external.ChatDataApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学员每日业绩同步服务。
 */
@Service
public class CampCustomerDailyPerformanceSyncService {

    private static final Logger logger = LoggerFactory.getLogger(CampCustomerDailyPerformanceSyncService.class);
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_PAGE = 1000;

    @Autowired
    private ChatDataApiClient chatDataApiClient;

    @Autowired
    private CampCustomerDailyPerformanceRepository campCustomerDailyPerformanceRepository;

    @Autowired
    private CampCustomerDailyPerformanceSavingService savingService;

    /**
     * 按更新时间范围同步学员每日业绩。
     */
    public void syncByUpdateTimeRange(ZonedDateTime updateStartTime, ZonedDateTime updateEndTime) {
        updateStartTime = updateStartTime.withZoneSameInstant(CHINA_ZONE);
        updateEndTime = updateEndTime.withZoneSameInstant(CHINA_ZONE);

        logger.info("Syncing camp customer daily performance from updateTime {} to {}", updateStartTime, updateEndTime);

        try {
            int page = 1;
            int totalSaved = 0;
            boolean hasMore = true;

            while (hasMore) {
                CampCustomerDailyPerformanceResponse response = chatDataApiClient
                        .fetchCampCustomerDailyPerformance(page, DEFAULT_LIMIT, updateStartTime, updateEndTime)
                        .block();

                if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
                    logger.error("Failed to fetch camp customer daily performance: response is null or invalid");
                    break;
                }

                List<CampCustomerDailyPerformanceResponse.CampCustomerDailyPerformanceItem> items = response.getData().getList();
                if (items == null || items.isEmpty()) {
                    hasMore = false;
                    break;
                }

                List<CampCustomerDailyPerformance> entities = convertToEntities(items);
                int saved = savingService.savePerformances(entities);
                totalSaved += saved;

                logger.info("Synced camp customer daily performance page {}, {} items, saved {}",
                        page, items.size(), saved);

                if (items.size() < DEFAULT_LIMIT) {
                    hasMore = false;
                } else {
                    page++;
                }

                if (page > MAX_PAGE) {
                    logger.warn("Reached safety limit of {} pages, stopping camp customer daily performance sync", MAX_PAGE);
                    break;
                }
            }

            logger.info("Camp customer daily performance sync completed. Total saved: {}", totalSaved);
        } catch (Exception e) {
            logger.error("Error during camp customer daily performance sync: {}", e.getMessage(), e);
        }
    }

    /**
     * 增量同步：从本地最大 statDate 往前回溯 3 天，避免第三方更新窗口遗漏。
     */
    public void incrementalSync() {
        logger.info("Starting incremental camp customer daily performance sync");
        ZonedDateTime startTime = getStartTime();
        ZonedDateTime endTime = ZonedDateTime.now(CHINA_ZONE);
        syncByUpdateTimeRange(startTime, endTime);
    }

    private ZonedDateTime getStartTime() {
        CampCustomerDailyPerformance latest = campCustomerDailyPerformanceRepository.findTopByOrderByStatDateDesc();
        if (latest != null && latest.getStatDate() != null) {
            ZonedDateTime startTime = latest.getStatDate().minusDays(3).atStartOfDay(CHINA_ZONE);
            logger.info("Use latest performance statDate with 3 days lookback: {}", startTime);
            return startTime;
        }

        ZonedDateTime startTime = ZonedDateTime.now(CHINA_ZONE).minusDays(30);
        logger.info("No local performance data found, use default startTime: {}", startTime);
        return startTime;
    }

    private List<CampCustomerDailyPerformance> convertToEntities(
            List<CampCustomerDailyPerformanceResponse.CampCustomerDailyPerformanceItem> items) {
        List<CampCustomerDailyPerformance> entities = new ArrayList<>();
        ZonedDateTime syncTime = ZonedDateTime.now(CHINA_ZONE);

        for (CampCustomerDailyPerformanceResponse.CampCustomerDailyPerformanceItem item : items) {
            if (item == null || item.getStatDate() == null
                    || isBlank(item.getSysUserId())
                    || isBlank(item.getExternalUserid())) {
                continue;
            }

            String campTag = item.getCampTag() == null ? "" : item.getCampTag();

            CampCustomerDailyPerformance entity = new CampCustomerDailyPerformance();
            entity.setId(buildId(item, campTag));
            entity.setStatDate(item.getStatDate());
            entity.setCampTag(campTag);
            entity.setSysUserId(item.getSysUserId());
            entity.setSalesName(item.getSalesName());
            entity.setGroupName(item.getGroupName());
            entity.setExternalUserid(item.getExternalUserid());
            entity.setExternalName(item.getExternalName());
            entity.setGmvAmount(defaultAmount(item.getGmvAmount()));
            entity.setRefundAmount(defaultAmount(item.getRefundAmount()));
            entity.setSyncTime(syncTime);
            entities.add(entity);
        }

        return entities;
    }

    private String buildId(CampCustomerDailyPerformanceResponse.CampCustomerDailyPerformanceItem item, String campTag) {
        return item.getStatDate() + "_" + item.getSysUserId() + "_" + item.getExternalUserid() + "_" + campTag;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
