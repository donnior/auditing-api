package com.xingcanai.csqe.auditing.service;

import java.time.ZonedDateTime;

public class DateTimeUtils {

    public static ZonedDateTime asStartOfDay(ZonedDateTime time) {
        return time.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static ZonedDateTime asEndOfDay(ZonedDateTime time) {
        return time.withHour(23).withMinute(59).withSecond(59).withNano(9999);
    }


}
