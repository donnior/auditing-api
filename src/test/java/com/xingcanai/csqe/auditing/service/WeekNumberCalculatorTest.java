package com.xingcanai.csqe.auditing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

class WeekNumberCalculatorTest {

    @Test
    void calculateReportTypeByFullCampTag() {
        ZonedDateTime targetSaturday = ZonedDateTime.parse("2026-05-16T23:59:59+08:00");

        assertEquals(1, WeekNumberCalculator.calculateReportType("20260516", targetSaturday));
        assertEquals(2, WeekNumberCalculator.calculateReportType("20260509", targetSaturday));
        assertEquals(3, WeekNumberCalculator.calculateReportType("20260502", targetSaturday));
        assertEquals(4, WeekNumberCalculator.calculateReportType("20260425", targetSaturday));
    }

    @Test
    void calculateReportTypeByShortCampTag() {
        ZonedDateTime targetSaturday = ZonedDateTime.parse("2026-05-16T23:59:59+08:00");

        assertEquals(1, WeekNumberCalculator.calculateReportType("0516", targetSaturday));
        assertEquals(2, WeekNumberCalculator.calculateReportType("0509", targetSaturday));
    }

    @Test
    void returnZeroWhenCampTagDoesNotAlign() {
        ZonedDateTime targetSaturday = ZonedDateTime.parse("2026-05-16T23:59:59+08:00");

        assertEquals(0, WeekNumberCalculator.calculateReportType("20260510", targetSaturday));
        assertEquals(0, WeekNumberCalculator.calculateReportType("", targetSaturday));
    }
}
