package com.xingcanai.csqe.util;

import java.time.ZonedDateTime;

/**
 * 时间相关工具类（尽量集中维护非业务核心的时间/格式化逻辑）
 */
public final class DateTimes {

    private DateTimes() {}

    /**
     * 返回当前时间的 ISO_OFFSET 格式字符串（不包含 ZoneId，例如不会带 "[Asia/Shanghai]"）
     *
     * 示例: 2026-01-12T11:17:27.308099+08:00
     */
    public static String nowIsoOffsetString() {
        return ZonedDateTime.now().toOffsetDateTime().toString();
    }

    /**
     * 将 ZonedDateTime 转为 ISO_OFFSET 格式字符串（不包含 ZoneId，例如不会带 "[Asia/Shanghai]"）
     */
    public static String toIsoOffsetString(ZonedDateTime time) {
        if (time == null) return null;
        return time.toOffsetDateTime().toString();
    }
}
