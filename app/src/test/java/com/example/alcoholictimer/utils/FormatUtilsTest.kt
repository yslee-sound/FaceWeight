package com.example.alcoholictimer.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class FormatUtilsTest {
    @Test
    fun daysToDayHour_zero() {
        val s = FormatUtils.daysToDayHourString(0.0, 2, Locale.KOREA)
        assertEquals("0.00시간", s)
    }

    @Test
    fun daysToDayHour_subDay_onlyHours() {
        val s = FormatUtils.daysToDayHourString(0.25, 2, Locale.KOREA)
        // 0.25일 = 6시간
        assertEquals("6.00시간", s)
    }

    @Test
    fun daysToDayHour_simple() {
        val s = FormatUtils.daysToDayHourString(1.5, 2, Locale.KOREA)
        assertEquals("1일 12.00시간", s)
    }

    @Test
    fun daysToDayHour_roundUpToNextDay() {
        val s = FormatUtils.daysToDayHourString(1.999999, 2, Locale.KOREA)
        assertEquals("2일 0.00시간", s)
    }

    @Test
    fun daysToDayHour_highPrecisionNotCarry() {
        val s = FormatUtils.daysToDayHourString(2.999, 2, Locale.KOREA)
        assertEquals("2일 23.98시간", s)
    }

    @Test
    fun daysToDayHour_invalidInput() {
        val s1 = FormatUtils.daysToDayHourString(Double.NaN, 2, Locale.KOREA)
        val s2 = FormatUtils.daysToDayHourString(Double.POSITIVE_INFINITY, 2, Locale.KOREA)
        val s3 = FormatUtils.daysToDayHourString(-5.0, 2, Locale.KOREA)
        assertEquals("0.00시간", s1)
        assertEquals("0.00시간", s2)
        assertEquals("0.00시간", s3)
    }
}
