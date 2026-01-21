package com.xingcanai.csqe.auditing.service;

import java.time.ZonedDateTime;

public record TimeRange(ZonedDateTime start, ZonedDateTime end) {

    public static TimeRange with(ZonedDateTime start, ZonedDateTime end) {
        return new TimeRange(start, end);
    }

    @Override
    public final String toString() {
        return "[start=" + start + ", end=" + end + "]";
    }

}
