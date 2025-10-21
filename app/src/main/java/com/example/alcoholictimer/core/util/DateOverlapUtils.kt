package com.example.alcoholictimer.core.util

import java.util.Calendar
import java.util.TimeZone

object DateOverlapUtils {
    const val DAY_MS: Long = 24L * 60L * 60L * 1000L

    @JvmStatic
    fun overlapDays(
        recordStart: Long,
        recordEnd: Long,
        periodStart: Long?,
        periodEnd: Long?
    ): Double {
        val safeStart = recordStart
        val safeEnd = recordEnd
        if (periodStart == null || periodEnd == null) {
            val duration = (safeEnd - safeStart).coerceAtLeast(0L)
            return duration / DAY_MS.toDouble()
        }
        val overlapStart = maxOf(safeStart, periodStart)
        val overlapEnd = minOf(safeEnd, periodEnd)
        val overlapMs = (overlapEnd - overlapStart).coerceAtLeast(0L)
        return overlapMs / DAY_MS.toDouble()
    }

    @JvmStatic
    fun monthRange(year: Int, month1to12: Int, timeZone: TimeZone = TimeZone.getDefault()): Pair<Long, Long> {
        val cal = Calendar.getInstance(timeZone)
        cal.set(year, month1to12 - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        val end = cal.timeInMillis
        return start to end
    }

    @JvmStatic
    fun ms(
        year: Int,
        month1to12: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val cal = Calendar.getInstance(timeZone)
        cal.set(year, month1to12 - 1, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

