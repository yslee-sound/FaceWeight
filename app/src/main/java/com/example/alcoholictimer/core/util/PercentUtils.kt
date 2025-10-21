package com.example.alcoholictimer.core.util

import java.math.BigDecimal
import java.math.RoundingMode

object PercentUtils {
    fun roundPercent(percentValue: Double): Int =
        BigDecimal(percentValue).setScale(0, RoundingMode.HALF_UP).toInt()

    fun roundPercentFromRatio(ratio: Double): Int = roundPercent(ratio * 100.0)
}

