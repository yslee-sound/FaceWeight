package com.example.alcoholictimer.core.util

import java.util.Locale
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

object FormatUtils {
    @JvmStatic
    fun daysToDayHourString(days: Double, decimals: Int = 2, locale: Locale = Locale.getDefault()): String {
        val safeDays = if (days.isNaN() || days.isInfinite()) 0.0 else days.coerceAtLeast(0.0)
        var dayInt = floor(safeDays).toInt()
        val frac = safeDays - dayInt
        val hoursRaw = frac * 24.0
        val scale = 10.0.pow(decimals)
        var hoursRounded = round(hoursRaw * scale) / scale
        if (hoursRounded >= 24.0) {
            dayInt += 1
            hoursRounded = 0.0
        }
        return if (dayInt == 0) {
            String.format(locale, "%.${decimals}f시간", hoursRounded)
        } else {
            String.format(locale, "%d일 %.${decimals}f시간", dayInt, hoursRounded)
        }
    }
}

