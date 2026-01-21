package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算“目标周日”对应的周报类型（第1~4周）。
 */
public final class WeeklyChatReportTypeCalculator {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyChatReportTypeCalculator.class);

    private WeeklyChatReportTypeCalculator() {
    }

    public static int calculateReportType(ZonedDateTime firstChatTime, ZonedDateTime targetSunday) {
        // 找到第一条聊天记录所在周的周四结束时间（周四23:59:59）
        ZonedDateTime thursdayEnd = firstChatTime.with(DayOfWeek.THURSDAY)
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // 确定第一周周日：with(DayOfWeek.SUNDAY) 会找到本周的周日（如果当前是周日则返回当前日期，否则向后找到周日）
        ZonedDateTime firstWeekSunday = firstChatTime.with(DayOfWeek.SUNDAY);

        if (firstChatTime.isAfter(thursdayEnd)) {
            firstWeekSunday = firstWeekSunday.plusWeeks(1);
        }


        // // 如果第一条聊天记录在周四之后（周五、周六），需要向后找到下一周的周四
        // if (firstChatTime.getDayOfWeek().getValue() > DayOfWeek.THURSDAY.getValue()) {
        //     thursdayEnd = thursdayEnd.plusWeeks(1);
        // }

        // 计算目标周日相对于第一周周日是第几周
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
                firstWeekSunday.toLocalDate(),
                targetSunday.toLocalDate());

        int weekNumber = (int) weeksBetween + 1; // +1 因为第一周是1，不是0
        return weekNumber;
    }

    public static void main(String[] args) {
        ZonedDateTime target = parse("2026-01-18T23:59:59+08:00");
        var calculator = new WeeklyChatReportTypeCalculator();

        String[] times = new String[] {
            "2025-12-18T13:59:59+08:00",
            "2025-12-25T13:59:59+08:00",
            "2025-12-26T13:59:59+08:00",
            "2026-01-01T13:59:59+08:00",
            "2026-01-02T13:59:59+08:00",
            "2026-01-03T13:59:59+08:00",
            "2026-01-04T13:59:59+08:00",
            "2026-01-07T13:59:59+08:00",
            "2026-01-08T13:59:59+08:00",
            "2026-01-09T13:59:59+08:00",
            "2026-01-10T13:59:59+08:00",
            "2026-01-11T13:59:59+08:00",
            "2026-01-12T13:59:59+08:00",
            "2026-01-13T13:59:59+08:00",
            "2026-01-14T13:59:59+08:00",
            "2026-01-15T13:59:59+08:00",
            "2026-01-16T13:59:59+08:00",
            "2026-02-17T13:59:59+08:00",
            "2026-02-18T13:59:59+08:00",
        };

        Arrays.stream(times).forEach(t -> {
            ZonedDateTime time = parse(t);
            System.out.println(t + " ->  " + calculator.calculateReportType(time, target));
        });
    }

    private static ZonedDateTime parse(String str) {
        return ZonedDateTime.parse(str);
    }
}
