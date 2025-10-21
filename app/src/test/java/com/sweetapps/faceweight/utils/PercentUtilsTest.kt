package com.sweetapps.faceweight.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PercentUtilsTest {
    @Test
    fun roundPercent_halfUp_behavior() {
        assertEquals(33, PercentUtils.roundPercent(33.4))
        assertEquals(34, PercentUtils.roundPercent(33.5)) // HALF_UP ties -> up
        assertEquals(-34, PercentUtils.roundPercent(-33.5)) // 음수도 절대값 기준 half up -> 더 작은(절댓값 큰) 쪽
    }

    @Test
    fun roundPercent_negative_values() {
        assertEquals(-10, PercentUtils.roundPercent(-9.6))
        assertEquals(-9, PercentUtils.roundPercent(-9.4))
    }

    @Test
    fun roundPercentFromRatio_basic() {
        // 0.876 -> 87.6% -> 88
        assertEquals(88, PercentUtils.roundPercentFromRatio(0.876))
        // 0.124 -> 12.4% -> 12
        assertEquals(12, PercentUtils.roundPercentFromRatio(0.124))
    }

    @Test
    fun roundPercentFromRatio_edge() {
        assertEquals(0, PercentUtils.roundPercentFromRatio(0.0))
        assertEquals(100, PercentUtils.roundPercentFromRatio(1.0))
    }
}
