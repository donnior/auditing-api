package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformanceRepository;
import com.xingcanai.csqe.auditing.entity.CampCustomerDailyPerformanceSummary;
import com.xingcanai.csqe.auditing.service.CampCustomerDailyPerformanceSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学员每日业绩同步控制器。
 */
@RestController
@RequestMapping("/camp-customer-daily-performance")
public class CampCustomerDailyPerformanceController {

    @Autowired
    private CampCustomerDailyPerformanceSyncService syncService;

    @Autowired
    private CampCustomerDailyPerformanceRepository repository;

    /**
     * 按员工和统计日期范围汇总每一期学员的 GMV/退款。
     */
    @GetMapping("/summary")
    public List<CampCustomerDailyPerformanceSummary> summary(
            @RequestParam String sysUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return repository.summarizeBySysUserIdAndStatDateRange(sysUserId, startDate, endDate);
    }

    /**
     * 手动触发学员每日业绩同步。
     */
    @PostMapping("/sync")
    public Map<String, Object> sync(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime updateStartTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime updateEndTime) {

        Map<String, Object> result = new HashMap<>();
        try {
            if (updateStartTime != null && updateEndTime != null) {
                syncService.syncByUpdateTimeRange(updateStartTime, updateEndTime);
                result.put("message", "按更新时间范围同步学员每日业绩成功");
            } else {
                syncService.incrementalSync();
                result.put("message", "增量同步学员每日业绩成功");
            }
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "同步失败：" + e.getMessage());
        }
        return result;
    }
}
