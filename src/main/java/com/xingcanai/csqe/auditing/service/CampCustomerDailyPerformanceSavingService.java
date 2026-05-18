package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformance;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CampCustomerDailyPerformanceSavingService {

    private static final Logger logger = LoggerFactory.getLogger(CampCustomerDailyPerformanceSavingService.class);

    @Autowired
    private CampCustomerDailyPerformanceRepository campCustomerDailyPerformanceRepository;

    /**
     * 批量保存学员每日业绩（upsert 语义：主键相同则更新）。
     */
    @Transactional
    public int savePerformances(List<CampCustomerDailyPerformance> performances) {
        int savedCount = 0;
        for (CampCustomerDailyPerformance performance : performances) {
            try {
                campCustomerDailyPerformanceRepository.save(performance);
                savedCount++;
            } catch (Exception e) {
                logger.warn("Failed to save camp customer daily performance {}: {}",
                        performance.getId(), e.getMessage());
            }
        }
        return savedCount;
    }
}
