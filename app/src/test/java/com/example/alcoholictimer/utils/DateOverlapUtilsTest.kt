package com.example.alcoholictimer.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class DateOverlapUtilsTest {

    private val tz = TimeZone.getTimeZone("Asia/Seoul")

    @Test
    fun sumOfThreeRecords_inSeptember2025_equals_2_4_days() {
        val (start, end) = DateOverlapUtils.monthRange(2025, 9, tz)

        // A: 2025-09-30 12:00 ~ 2025-10-01 00:00  (약 12시간 → 0.5일, 경계 1ms 제외 오차 허용)
        val aStart = DateOverlapUtils.ms(2025, 9, 30, 12, 0, tz)
        val aEnd = DateOverlapUtils.ms(2025, 10, 1, 0, 0, tz)
        val a = DateOverlapUtils.overlapDays(aStart, aEnd, start, end)

        // B: 2025-09-30 00:00 ~ 2025-09-30 12:00 (12시간 → 0.5일)
        val bStart = DateOverlapUtils.ms(2025, 9, 30, 0, 0, tz)
        val bEnd = DateOverlapUtils.ms(2025, 9, 30, 12, 0, tz)
        val b = DateOverlapUtils.overlapDays(bStart, bEnd, start, end)

        // C: 2025-09-15 00:00 ~ 2025-09-16 09:36 (33.6시간 → 1.4일)
        val cStart = DateOverlapUtils.ms(2025, 9, 15, 0, 0, tz)
        val cEnd = DateOverlapUtils.ms(2025, 9, 16, 9, 36, tz)
        val c = DateOverlapUtils.overlapDays(cStart, cEnd, start, end)

        val total = a + b + c
        assertEquals(2.4, total, 1e-3) // 경계 1ms 오차 감안
    }

    @Test
    fun monthStartBoundary_overlap_isTwoHours() {
        val (start, end) = DateOverlapUtils.monthRange(2025, 9, tz)
        // 2025-08-31 23:00 ~ 2025-09-01 02:00 → 9월 내 겹침 2시간 = 2/24일
        val s = DateOverlapUtils.ms(2025, 8, 31, 23, 0, tz)
        val e = DateOverlapUtils.ms(2025, 9, 1, 2, 0, tz)
        val d = DateOverlapUtils.overlapDays(s, e, start, end)
        assertEquals(2.0 / 24.0, d, 1e-6)
    }

    @Test
    fun monthEndBoundary_overlap_isHalfDay() {
        val (start, end) = DateOverlapUtils.monthRange(2025, 9, tz)
        // 2025-09-30 12:00 ~ 2025-10-01 12:00 → 9월 내 겹침 약 12시간
        val s = DateOverlapUtils.ms(2025, 9, 30, 12, 0, tz)
        val e = DateOverlapUtils.ms(2025, 10, 1, 12, 0, tz)
        val d = DateOverlapUtils.overlapDays(s, e, start, end)
        assertEquals(0.5, d, 1e-3) // 경계 1ms 오차 감안
    }

    @Test
    fun noOverlap_returnsZero() {
        val (start, end) = DateOverlapUtils.monthRange(2025, 9, tz)
        // 10월 기록은 9월과 겹치지 않음
        val s = DateOverlapUtils.ms(2025, 10, 1, 0, 0, tz)
        val e = DateOverlapUtils.ms(2025, 10, 2, 0, 0, tz)
        val d = DateOverlapUtils.overlapDays(s, e, start, end)
        assertEquals(0.0, d, 0.0)
    }
}
