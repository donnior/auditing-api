package com.xingcanai.csqe.auditing.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SalesSpeechAnalysisRepositoryDataJpaTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Autowired
    private WxChatMessageRepository chatMessageRepository;

    @Autowired
    private CampCustomerDailyPerformanceRepository performanceRepository;

    @Autowired
    private SalesSpeechAnalysisRepository analysisRepository;

    @Test
    void directChatQueryUsesHalfOpenSevenDayBoundaryAndStableOrder() {
        String employeeId = "sales-qw";
        ZonedDateTime start = ZonedDateTime.of(2026, 7, 12, 0, 0, 0, 0, ZONE_ID);
        ZonedDateTime endExclusive = ZonedDateTime.of(2026, 7, 19, 0, 0, 0, 0, ZONE_ID);

        chatMessageRepository.saveAll(List.of(
                message("included-start", employeeId, "customer-a", 1, start, 1L),
                message("same-time-seq-3", "customer-a", employeeId, 1, start.plusHours(1), 3L),
                message("same-time-seq-2", employeeId, "customer-a", 1, start.plusHours(1), 2L),
                message("excluded-end", employeeId, "customer-a", 1, endExclusive, 4L),
                message("excluded-type", employeeId, "customer-a", 2, start.plusHours(2), 5L),
                message("other-sales", "other-sales", "customer-a", 1, start.plusHours(3), 6L)));

        List<WxChatMessage> messages =
                chatMessageRepository.findDirectMessagesByEmployeeAndTimeRange(
                        employeeId,
                        start,
                        endExclusive);

        assertEquals(
                List.of("included-start", "same-time-seq-2", "same-time-seq-3"),
                messages.stream().map(WxChatMessage::getMsgId).toList());
    }

    @Test
    void performanceQueryReturnsEveryDailyRowWithoutAggregation() {
        String employeeId = "sales-qw";
        performanceRepository.saveAll(List.of(
                performance("p1", employeeId, LocalDate.of(2026, 7, 12), "customer-b", "二期", "100"),
                performance("p2", employeeId, LocalDate.of(2026, 7, 12), "customer-a", "一期", "50"),
                performance("p3", employeeId, LocalDate.of(2026, 7, 18), "customer-a", "一期", "75"),
                performance("outside", employeeId, LocalDate.of(2026, 7, 11), "customer-a", "一期", "999"),
                performance("other-sales", "other-sales", LocalDate.of(2026, 7, 15), "customer-a", "一期", "999")));

        List<CampCustomerDailyPerformance> rows =
                performanceRepository.findRawBySysUserIdAndStatDateRange(
                        employeeId,
                        LocalDate.of(2026, 7, 12),
                        LocalDate.of(2026, 7, 18));

        assertEquals(List.of("p2", "p1", "p3"), rows.stream().map(CampCustomerDailyPerformance::getId).toList());
        assertEquals(
                List.of("50", "100", "75"),
                rows.stream()
                        .map(CampCustomerDailyPerformance::getGmvAmount)
                        .map(BigDecimal::stripTrailingZeros)
                        .map(BigDecimal::toPlainString)
                        .toList());
    }

    @Test
    void latestAnalysisQueryIgnoresPromptVersionAndUsesUpdateTime() {
        LocalDate evalPeriod = LocalDate.of(2026, 7, 18);
        ZonedDateTime now = ZonedDateTime.of(
                2026, 7, 23, 12, 0, 0, 0, ZONE_ID);
        analysisRepository.saveAllAndFlush(List.of(
                analysis(
                        "latest-updated",
                        "employee-1",
                        evalPeriod,
                        "v1.0.0",
                        now.minusDays(2),
                        now),
                analysis(
                        "latest-created",
                        "employee-1",
                        evalPeriod,
                        "v2.0.0",
                        now.minusDays(1),
                        now.minusHours(1)),
                analysis(
                        "other-employee",
                        "employee-2",
                        evalPeriod,
                        "v3.0.0",
                        now,
                        now.plusHours(1))));

        SalesSpeechAnalysis latest = analysisRepository
                .findFirstByEmployeeIdAndEvalPeriodOrderByUpdateTimeDescCreateTimeDesc(
                        "employee-1",
                        evalPeriod)
                .orElseThrow();

        assertEquals("latest-updated", latest.getId());
        assertEquals("v1.0.0", latest.getPromptVersion());
    }

    private WxChatMessage message(
            String id,
            String fromId,
            String acceptId,
            int acceptType,
            ZonedDateTime time,
            long sequence) {
        WxChatMessage message = new WxChatMessage();
        message.setMsgId(id);
        message.setFromId(fromId);
        message.setAcceptId(acceptId);
        message.setAcceptType(acceptType);
        message.setMsgType("text");
        message.setContent(id);
        message.setDataSeq(sequence);
        message.setMsgTime(time);
        return message;
    }

    private CampCustomerDailyPerformance performance(
            String id,
            String employeeId,
            LocalDate date,
            String customerId,
            String campTag,
            String gmv) {
        CampCustomerDailyPerformance performance = new CampCustomerDailyPerformance();
        performance.setId(id);
        performance.setStatDate(date);
        performance.setCampTag(campTag);
        performance.setSysUserId(employeeId);
        performance.setExternalUserid(customerId);
        performance.setGmvAmount(new BigDecimal(gmv));
        performance.setRefundAmount(BigDecimal.ZERO);
        return performance;
    }

    private SalesSpeechAnalysis analysis(
            String id,
            String employeeId,
            LocalDate evalPeriod,
            String promptVersion,
            ZonedDateTime createTime,
            ZonedDateTime updateTime) {
        SalesSpeechAnalysis analysis = new SalesSpeechAnalysis();
        analysis.setId(id);
        analysis.setEmployeeId(employeeId);
        analysis.setEmployeeQwId("qw-" + employeeId);
        analysis.setEvalPeriod(evalPeriod);
        analysis.setPeriodStartTime(evalPeriod.minusDays(6).atStartOfDay(ZONE_ID));
        analysis.setPeriodEndTime(evalPeriod.plusDays(1).atStartOfDay(ZONE_ID));
        analysis.setStatus(SalesSpeechAnalysisStatus.COMPLETED);
        analysis.setPromptVersion(promptVersion);
        analysis.setRequestedBy("admin");
        analysis.setCreateTime(createTime);
        analysis.setUpdateTime(updateTime);
        analysis.setCompletedTime(updateTime);
        return analysis;
    }
}
