package com.xingcanai.csqe.auditing.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算“目标周日”对应的周报类型（第1~4周）。
 */
public final class WeekNumberCalculator {

    private static final Logger logger = LoggerFactory.getLogger(WeekNumberCalculator.class);
    private static final DateTimeFormatter FULL_CAMP_TAG_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter SHORT_CAMP_TAG_FORMATTER = DateTimeFormatter.ofPattern("MMdd");

    private WeekNumberCalculator() {
    }

    public static int calculateReportType(ZonedDateTime firstChatTime, ZonedDateTime targetSunday) {
        // 找到第一条聊天记录所在周的周五结束时间（周五23:59:59）
        ZonedDateTime fridayEnd = DateTimeUtils.asEndOfDay(firstChatTime.with(DayOfWeek.FRIDAY));

        // 确定第一周周日：with(DayOfWeek.SUNDAY) 会找到本周的周日（如果当前是周日则返回当前日期，否则向后找到周日）
        ZonedDateTime firstWeekSunday = firstChatTime.with(DayOfWeek.SUNDAY);

        if (firstChatTime.isAfter(fridayEnd)) {
            firstWeekSunday = firstWeekSunday.plusWeeks(1);
        }

        // 计算目标周日相对于第一周周日是第几周
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
                firstWeekSunday.toLocalDate(),
                targetSunday.toLocalDate());

        int weekNumber = (int) weeksBetween + 1; // +1 因为第一周是1，不是0
        return weekNumber;
    }

    public static int calculateReportType(String campTag, ZonedDateTime targetPeriodEnd) {
        LocalDate campDate = parseCampDate(campTag, targetPeriodEnd.toLocalDate());
        if (campDate == null) {
            logger.warn("Unable to calculate report type because campTag is invalid: {}", campTag);
            return 0;
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                campDate,
                targetPeriodEnd.toLocalDate());

        if (daysBetween < 0 || daysBetween % 7 != 0) {
            logger.warn("campTag {} does not align with target period end {}", campTag, targetPeriodEnd.toLocalDate());
            return 0;
        }

        return (int) (daysBetween / 7) + 1;
    }

    private static LocalDate parseCampDate(String campTag, LocalDate targetPeriodEnd) {
        if (campTag == null || campTag.isBlank()) {
            return null;
        }

        String value = campTag.trim();
        try {
            if (value.matches("\\d{8}")) {
                return LocalDate.parse(value, FULL_CAMP_TAG_FORMATTER);
            }
            if (value.matches("\\d{4}")) {
                MonthDay monthDay = MonthDay.parse(value, SHORT_CAMP_TAG_FORMATTER);
                LocalDate date = monthDay.atYear(targetPeriodEnd.getYear());
                if (date.isAfter(targetPeriodEnd.plusDays(7))) {
                    date = date.minusYears(1);
                }
                return date;
            }
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse campTag {}: {}", campTag, e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        ZonedDateTime target = parse("2026-01-18T23:59:59+08:00");
        var calculator = new WeekNumberCalculator();

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
            "2026-01-17T13:59:59+08:00",
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
