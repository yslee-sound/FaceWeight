package com.sweetapps.faceweight.feature.records.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sweetapps.faceweight.core.model.SobrietyRecord
import com.sweetapps.faceweight.core.util.PercentUtils
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun StatisticsCardsSection(
    records: List<SobrietyRecord>,
    selectedPeriod: String,
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    fun parseWeekRange(range: String): Pair<Long, Long>? {
        val regex = Regex("(\\d{1,2})-(\\d{1,2}) ~ (\\d{1,2})-(\\d{1,2})")
        val match = regex.find(range)
        if (match != null) {
            val (startMonth, startDay, endMonth, endDay) = match.destructured
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val startCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, startMonth.toInt() - 1)
                set(Calendar.DAY_OF_MONTH, startDay.toInt())
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, endMonth.toInt() - 1)
                set(Calendar.DAY_OF_MONTH, endDay.toInt())
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return startCal.timeInMillis to endCal.timeInMillis
        }
        return null
    }

    fun parseMonthRange(range: String): Pair<Int, Int>? {
        val yearMonthRegex = Regex("(\\d{4})[.-]?(\\d{1,2})")
        val onlyMonthRegex = Regex("(\\d{1,2})월")
        yearMonthRegex.find(range)?.let {
            val (year, month) = it.destructured
            return year.toInt() to month.toInt()
        }
        onlyMonthRegex.find(range)?.let {
            val (month) = it.destructured
            val year = Calendar.getInstance().get(Calendar.YEAR)
            return year to month.toInt()
        }
        return null
    }

    val weekRange = parseWeekRange(selectedRange)
    val (weekStart, weekEnd) = weekRange ?: (null to null)
    val monthRange = parseMonthRange(selectedRange)
    val (selectedYear, selectedMonth) = monthRange ?: (null to null)

    val filteredRecords = when {
        selectedPeriod == "월간" && selectedYear != null && selectedMonth != null -> {
            records.filter { record ->
                val cal = Calendar.getInstance().apply { timeInMillis = record.startTime }
                cal.get(Calendar.YEAR) == selectedYear && (cal.get(Calendar.MONTH) + 1) == selectedMonth
            }
        }
        weekStart != null && weekEnd != null -> {
            records.filter { it.endTime >= weekStart && it.startTime <= weekEnd }
        }
        else -> records
    }

    val totalDays = filteredRecords.sumOf { record ->
        if (weekStart != null && weekEnd != null) {
            val overlapStart = max(record.startTime, weekStart)
            val overlapEnd = min(record.endTime, weekEnd)
            if (overlapStart < overlapEnd) {
                ((overlapEnd - overlapStart) / (24 * 60 * 60 * 1000f)).roundToInt()
            } else 0
        } else {
            val duration = record.endTime - record.startTime
            (duration / (24 * 60 * 60 * 1000f)).roundToInt()
        }
    }

    val totalAttempts = filteredRecords.count { record ->
        if (weekStart != null && weekEnd != null) {
            val overlapStart = max(record.startTime, weekStart)
            val overlapEnd = min(record.endTime, weekEnd)
            overlapStart < overlapEnd
        } else true
    }

    // 변경된 성공률 계산:
    // 주간(weekStart/end 존재)일 때: (해당 주 금주 성공 일수 합 / 7일) * 100
    //     - 금주 성공 일수: 주 범위와 기록 겹치는 일수 총합 (최대 7일로 cap)
    // 그 외(월간 등): 기존 로직(시도별 목표 진행률 평균) 유지
    val successRate = if (weekStart != null && weekEnd != null) {
        val weeklySuccessDays = filteredRecords.sumOf { record ->
            val overlapStart = max(record.startTime, weekStart)
            val overlapEnd = min(record.endTime, weekEnd)
            if (overlapStart < overlapEnd) {
                (overlapEnd - overlapStart) / (24 * 60 * 60 * 1000.0)
            } else 0.0
        }.coerceAtMost(7.0)
        PercentUtils.roundPercent((weeklySuccessDays / 7.0) * 100.0)
    } else if (totalAttempts > 0) {
        val totalProgressPercent = filteredRecords.sumOf { record ->
            val overlapStart = if (weekStart != null) max(record.startTime, weekStart) else record.startTime
            val overlapEnd = if (weekEnd != null) min(record.endTime, weekEnd) else record.endTime
            val actualDurationDays = if (overlapStart < overlapEnd) (overlapEnd - overlapStart) / (24 * 60 * 60 * 1000f) else 0f
            val progressPercent = if (record.targetDays > 0) {
                ((actualDurationDays / record.targetDays) * 100).coerceIn(0f, 100f)
            } else {
                record.percentage?.toFloat() ?: 0f
            }
            progressPercent.toDouble()
        }
        PercentUtils.roundPercent(totalProgressPercent / totalAttempts)
    } else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    enabled = selectedPeriod != "전체",
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { if (selectedPeriod != "전체") onRangeSelected(selectedRange) }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedRange,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (selectedPeriod != "전체") {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "\u25bc", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(title = "총 금주일", value = "${totalDays}일", modifier = Modifier.weight(1f))
            StatCard(title = "성공률", value = "${successRate}%", modifier = Modifier.weight(1f))
            StatCard(title = "시도 횟수", value = "${totalAttempts}회", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}
