package com.sweetapps.faceweight.feature.records.components

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.sweetapps.faceweight.core.ui.theme.AlcoholicTimerTheme
import com.sweetapps.faceweight.core.util.DateOverlapUtils
import com.sweetapps.faceweight.core.data.RecordsDataLoader
import com.sweetapps.faceweight.core.model.SobrietyRecord
import java.util.*
import com.sweetapps.faceweight.feature.addrecord.AddRecordActivity
import com.sweetapps.faceweight.core.util.PercentUtils
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import com.sweetapps.faceweight.core.ui.AppElevation
import com.sweetapps.faceweight.core.ui.LocalRequestGlobalLock
import com.sweetapps.faceweight.core.ui.LocalSafeContentPadding
import com.sweetapps.faceweight.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    externalRefreshTrigger: Int,
    onNavigateToDetail: (SobrietyRecord) -> Unit = {},
    onNavigateToAllRecords: () -> Unit = {},
    fontScale: Float = 1.06f
) {
    val context = LocalContext.current
    var records by remember { mutableStateOf<List<SobrietyRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentDate = Calendar.getInstance()
    val currentYear = currentDate.get(Calendar.YEAR)
    val currentMonth = currentDate.get(Calendar.MONTH) + 1

    var selectedPeriod by remember { mutableStateOf("월") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDetailPeriod by remember { mutableStateOf("${currentYear}년 ${currentMonth}월") }
    var selectedWeekRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    // 전역 입력 잠금 훅
    val requestGlobalLock = LocalRequestGlobalLock.current

    val loadRecords = {
        isLoading = true
        try {
            val loadedRecords = RecordsDataLoader.loadSobrietyRecords(context)
            records = loadedRecords
            Log.d("RecordsScreen", "기록 로딩 완료: ${loadedRecords.size}개")
        } catch (e: Exception) {
            Log.e("RecordsScreen", "기록 로딩 실패", e)
        } finally {
            isLoading = false
        }
    }

    val filteredRecords = remember(records, selectedPeriod, selectedDetailPeriod, selectedWeekRange) {
        when (selectedPeriod) {
            "주" -> {
                val range = selectedWeekRange ?: run {
                    val cal = Calendar.getInstance().apply {
                        firstDayOfWeek = Calendar.SUNDAY
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    }
                    val weekStart = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_WEEK, 6)
                    val weekEndInclusive = cal.timeInMillis + (24 * 60 * 60 * 1000L - 1)
                    weekStart to weekEndInclusive
                }
                records.filter { it.endTime >= range.first && it.startTime <= range.second }
            }
            "월" -> {
                val range: Pair<Long, Long> = if (selectedDetailPeriod.isNotEmpty()) {
                    val regex = Regex("(\\d{4})년 (\\d{1,2})월")
                    val match = regex.find(selectedDetailPeriod)
                    if (match != null) {
                        val year = match.groupValues[1].toInt()
                        val month = match.groupValues[2].toInt() - 1
                        val cal = Calendar.getInstance()
                        cal.set(year, month, 1, 0, 0, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val monthStart = cal.timeInMillis
                        cal.add(Calendar.MONTH, 1)
                        cal.add(Calendar.MILLISECOND, -1)
                        val monthEnd = cal.timeInMillis
                        monthStart to monthEnd
                    } else {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val monthStart = cal.timeInMillis
                        cal.add(Calendar.MONTH, 1)
                        cal.add(Calendar.MILLISECOND, -1)
                        val monthEnd = cal.timeInMillis
                        monthStart to monthEnd
                    }
                } else {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val monthStart = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val monthEnd = cal.timeInMillis
                    monthStart to monthEnd
                }
                records.filter { it.endTime >= range.first && it.startTime <= range.second }
            }
            "년" -> {
                val range: Pair<Long, Long> = if (selectedDetailPeriod.isNotEmpty()) {
                    val regex = Regex("(\\d{4})년")
                    val match = regex.find(selectedDetailPeriod)
                    if (match != null) {
                        val year = match.groupValues[1].toInt()
                        val cal = Calendar.getInstance()
                        cal.set(year, 0, 1, 0, 0, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val yearStart = cal.timeInMillis
                        cal.add(Calendar.YEAR, 1)
                        cal.add(Calendar.MILLISECOND, -1)
                        val yearEnd = cal.timeInMillis
                        yearStart to yearEnd
                    } else {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.MONTH, 0)
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val yearStart = cal.timeInMillis
                        cal.add(Calendar.YEAR, 1)
                        cal.add(Calendar.MILLISECOND, -1)
                        val yearEnd = cal.timeInMillis
                        yearStart to yearEnd
                    }
                } else {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.MONTH, 0)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val yearStart = cal.timeInMillis
                    cal.add(Calendar.YEAR, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val yearEnd = cal.timeInMillis
                    yearStart to yearEnd
                }
                records.filter { it.endTime >= range.first && it.startTime <= range.second }
            }
            else -> records
        }
    }

    val latestRecords = remember(records) {
        records.sortedByDescending { it.endTime }.take(5)
    }

    LaunchedEffect(externalRefreshTrigger) { loadRecords() }

    // 다중 탭 방지: AddRecordActivity 런치 진행 상태
    var isLaunchingAddRecord by remember { mutableStateOf(false) }

    val addRecordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 결과 수신 시에는 무조건 재활성화
        isLaunchingAddRecord = false
        if (result.resultCode == Activity.RESULT_OK) { loadRecords() }
    }

    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = LocalDensity.current.fontScale * fontScale)) {
        // BaseScreen에서 제공하는 하단 안전 패딩 사용
        val safePadding = LocalSafeContentPadding.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = safePadding,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PeriodSelectionSection(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { period: String ->
                            // 전역 입력 잠금: 전환/리컴포지션 안정화를 위해 짧게 차단
                            requestGlobalLock(250)
                            selectedPeriod = period
                            selectedDetailPeriod = ""
                        },
                        onPeriodClick = { _ ->
                            // 전역 입력 잠금: 바텀시트 등장 전 탭 전파를 흡수
                            requestGlobalLock(250)
                            showBottomSheet = true
                        },
                        selectedDetailPeriod = selectedDetailPeriod
                    )
                }
                // 기간 헤더: 로딩 여부와 무관하게 항상 노출(제목 + +버튼)
                item {
                    PeriodHeaderRow(
                        selectedPeriod = selectedPeriod,
                        onAddRecord = {
                            if (!isLaunchingAddRecord) {
                                // 전역 입력 잠금: 액티비티 전환 중 중복 탭 방지
                                requestGlobalLock(300)
                                isLaunchingAddRecord = true
                                val intent = Intent(context, AddRecordActivity::class.java)
                                addRecordLauncher.launch(intent)
                            }
                        },
                        enabled = !isLaunchingAddRecord
                    )
                }
                item {
                    if (!isLoading) {
                        PeriodStatisticsSection(
                            records = filteredRecords,
                            selectedPeriod = selectedPeriod,
                            selectedDetailPeriod = selectedDetailPeriod,
                            modifier = Modifier.padding(vertical = 8.dp),
                            weekRange = selectedWeekRange,
                            onAddRecord = {
                                val intent = Intent(context, AddRecordActivity::class.java)
                                addRecordLauncher.launch(intent)
                            }
                        )
                    }
                }
                item {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                    } else if (records.isEmpty()) {
                        EmptyRecordsState()
                    }
                }
                if (!isLoading && latestRecords.isNotEmpty()) {
                    items(items = latestRecords, key = { it.id }) { record ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RecordSummaryCard(
                                record = record,
                                compact = false,
                                headerIconSizeDp = 56.dp,
                                onClick = { onNavigateToDetail(record) }
                            )
                        }
                    }
                    if (records.isNotEmpty()) {
                        item {
                            Button(
                                onClick = onNavigateToAllRecords,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "모든 기록 보기 (${records.size}개)",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        when (selectedPeriod) {
            "주" -> {
                WeekPickerBottomSheet(
                    isVisible = true,
                    onDismiss = { showBottomSheet = false },
                    onWeekPicked = { weekStart, weekEnd, displayText ->
                        selectedDetailPeriod = displayText
                        selectedWeekRange = weekStart to weekEnd
                        showBottomSheet = false
                    }
                )
            }
            "월" -> {
                MonthPickerBottomSheet(
                    isVisible = true,
                    onDismiss = { showBottomSheet = false },
                    onMonthPicked = { year, month ->
                        selectedDetailPeriod = "${year}년 ${month}월"
                        showBottomSheet = false
                    },
                    records = records,
                    onYearPicked = { year ->
                        selectedPeriod = "년"
                        selectedDetailPeriod = "${year}년"
                        showBottomSheet = false
                    }
                )
            }
            "년" -> {
                val initialYearForPicker =
                    Regex("(\\d{4})년").find(selectedDetailPeriod)?.groupValues?.getOrNull(1)?.toIntOrNull()
                        ?: Calendar.getInstance().get(Calendar.YEAR)

                YearPickerBottomSheet(
                    isVisible = true,
                    onDismiss = { showBottomSheet = false },
                    onYearPicked = { year ->
                        selectedDetailPeriod = "${year}년"
                        showBottomSheet = false
                    },
                    records = records,
                    initialYear = initialYearForPicker
                )
            }
        }
    }
}

@Composable
private fun EmptyRecordsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "빈 상태",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "아직 금주 기록이 없습니다",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecordCard(
    record: SobrietyRecord,
    onClick: () -> Unit
) { RecordSummaryCard(record = record, onClick = onClick) }

@Composable
private fun PeriodHeaderRow(
    selectedPeriod: String,
    onAddRecord: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (selectedPeriod) {
                "주" -> "주 통계"
                "월" -> "월 통계"
                "년" -> "년 통계"
                else -> "전체 통계"
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            onClick = onAddRecord,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "금주 기록 추가",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

// ↓ 아래는 루트 파일의 하단 절반(PeriodStatisticsSection 등) 그대로 이식
@Suppress("UNUSED_PARAMETER")
@Composable
private fun PeriodStatisticsSection(
    records: List<SobrietyRecord>,
    selectedPeriod: String,
    selectedDetailPeriod: String,
    modifier: Modifier = Modifier,
    weekRange: Pair<Long, Long>? = null,
    onAddRecord: () -> Unit = {}
) {
    val totalRecords = records.size
    val periodRange: Pair<Long, Long>? = remember(selectedPeriod, selectedDetailPeriod, weekRange) {
        when (selectedPeriod) {
            "주" -> {
                weekRange ?: run {
                    val cal = Calendar.getInstance().apply {
                        firstDayOfWeek = Calendar.SUNDAY
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    }
                    val weekStart = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_WEEK, 6)
                    val weekEndInclusive = cal.timeInMillis + (24 * 60 * 60 * 1000L - 1)
                    weekStart to weekEndInclusive
                }
            }
            "월" -> {
                val regex = Regex("(\\d{4})년 (\\d{1,2})월")
                val match = regex.find(selectedDetailPeriod)
                if (match != null) {
                    val year = match.groupValues[1].toInt()
                    val month = match.groupValues[2].toInt() - 1
                    val cal = Calendar.getInstance()
                    cal.set(year, month, 1, 0, 0, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val end = cal.timeInMillis
                    start to end
                } else {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val end = cal.timeInMillis
                    start to end
                }
            }
            "년" -> {
                val regex = Regex("(\\d{4})년")
                val match = regex.find(selectedDetailPeriod)
                if (match != null) {
                    val year = match.groupValues[1].toInt()
                    val cal = Calendar.getInstance()
                    cal.set(year, 0, 1, 0, 0, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.YEAR, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val end = cal.timeInMillis
                    start to end
                } else {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.MONTH, 0)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.YEAR, 1)
                    cal.add(Calendar.MILLISECOND, -1)
                    val end = cal.timeInMillis
                    start to end
                }
            }
            else -> null
        }
    }

    fun overlappedDays(record: SobrietyRecord): Double {
        return if (periodRange == null) {
            DateOverlapUtils.overlapDays(record.startTime, record.endTime, null, null)
        } else {
            DateOverlapUtils.overlapDays(record.startTime, record.endTime, periodRange.first, periodRange.second)
        }
    }

    val successRate = if (totalRecords > 0) {
        if (selectedPeriod == "주" && periodRange != null) {
            val (periodStart, periodEnd) = periodRange
            val dayMillis = 24 * 60 * 60 * 1000.0
            val intervals = records.mapNotNull { record ->
                val s = max(record.startTime.toDouble(), periodStart.toDouble())
                val e = min(record.endTime.toDouble(), periodEnd.toDouble())
                if (s < e) s to e else null
            }.sortedBy { it.first }
            var mergedMs = 0.0
            var curStart = Double.NaN
            var curEnd = Double.NaN
            for ((s, e) in intervals) {
                if (curStart.isNaN()) {
                    curStart = s; curEnd = e
                } else if (s <= curEnd) {
                    if (e > curEnd) curEnd = e
                } else {
                    mergedMs += (curEnd - curStart)
                    curStart = s; curEnd = e
                }
            }
            if (!curStart.isNaN()) {
                mergedMs += (curEnd - curStart)
            }
            val periodDays = ((periodEnd - periodStart + 1) / dayMillis)
            val unionDays = (mergedMs / dayMillis).coerceAtMost(periodDays)
            val ratio = if (periodDays > 0) (unionDays / periodDays).coerceIn(0.0, 1.0) else 0.0
            PercentUtils.roundPercent(ratio * 100.0)
        } else {
            val totalProgressPercent = records.sumOf { record ->
                val actualDurationDays = overlappedDays(record).toFloat()
                val progressPercent = if (record.targetDays > 0) {
                    ((actualDurationDays / record.targetDays) * 100).coerceIn(0f, 100f)
                } else {
                    record.percentage?.toFloat() ?: ((actualDurationDays / 30f) * 100f).coerceIn(0f, 100f)
                }
                progressPercent.toDouble()
            }
            PercentUtils.roundPercent(totalProgressPercent / totalRecords)
        }
    } else 0

    val totalDaysDouble = records.sumOf { record -> overlappedDays(record) }
    val totalDaysDisplay = String.format(Locale.getDefault(), "%.1f", totalDaysDouble)

    val averageDaysDisplay = if (totalRecords > 0) {
        String.format(Locale.getDefault(), "%.1f", records.map { record -> overlappedDays(record) }.average())
    } else "0.0"

    val maxDaysDisplay = if (records.isNotEmpty()) {
        String.format(Locale.getDefault(), "%.1f", records.maxOf { record -> overlappedDays(record) })
    } else "0.0"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
        border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 헤더(Row + +버튼)는 PeriodHeaderRow로 이동하여 항상 노출되므로 여기서는 제거
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val statsScale = 1.3f
                StatisticItem(
                    title = "성공률\n ",
                    value = "$successRate%",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    titleScale = statsScale,
                    valueScale = statsScale
                )
                StatisticItem(
                    title = "평균\n지속일",
                    value = "${averageDaysDisplay}일",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    titleScale = statsScale,
                    valueScale = statsScale
                )
                StatisticItem(
                    title = "최대\n지속일",
                    value = "${maxDaysDisplay}일",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    titleScale = statsScale,
                    valueScale = statsScale
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "총 누적 금주일",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${totalDaysDisplay}일",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun AutoResizeSingleLineText(
    text: String,
    baseStyle: TextStyle,
    modifier: Modifier = Modifier,
    step: Float = 0.95f,
    color: Color? = null,
    textAlign: TextAlign? = null,
) {
    val minSpLocal = 10f
    var style by remember(text) { mutableStateOf(baseStyle) }
    var tried by remember(text) { mutableStateOf(0) }
    Text(
        text = text,
        style = style,
        color = color ?: style.color,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = modifier,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && tried < 20) {
                val current = style.fontSize.value
                val next = (current * step).coerceAtLeast(minSpLocal)
                if (next < current - 0.1f) {
                    style = style.copy(fontSize = next.sp, lineHeight = (next.sp * 1.1f))
                    tried++
                }
            }
        }
    )
}

@Composable
private fun StatisticItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    titleScale: Float = 1.0f,
    valueScale: Float = 1.0f
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 120.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            val valueBoxH = 40.dp
            // 두 줄(예: "평균\n지속일") 텍스트 하단 잘림 현상 대응: 고정 높이 여유 확보
            // 기존 44.dp -> 56.dp (라인 높이 * 2 + 하강부 여유 패딩)
            val minTitleHeight = 48.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(valueBoxH),
                contentAlignment = Alignment.Center
            ) {
                val base = MaterialTheme.typography.titleMedium
                val numSize = (base.fontSize * valueScale)
                val numStyle = base.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = numSize,
                    lineHeight = numSize * 1.1f,
                    platformStyle = PlatformTextStyle(includeFontPadding = true),
                    fontFeatureSettings = "tnum"
                )
                val unitStyle = base.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = base.fontSize * 0.9f,
                    lineHeight = base.fontSize * 1.1f,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                )
                val regex = Regex("^\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(.*)")
                val m = regex.find(value)
                if (m != null) {
                    val num = m.groupValues[1]
                    val unit = m.groupValues[2]
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        AutoResizeSingleLineText(
                            text = num,
                            baseStyle = numStyle,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.alignByBaseline().wrapContentWidth()
                        )
                        if (unit.isNotBlank()) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = unit,
                                style = unitStyle,
                                color = color,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    }
                } else {
                    AutoResizeSingleLineText(
                        text = value,
                        baseStyle = numStyle,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minTitleHeight),
                contentAlignment = Alignment.Center
            ) {
                val baseLabel = MaterialTheme.typography.labelMedium
                val scaledLabelFontSize = baseLabel.fontSize * titleScale
                val scaledLabelStyle = baseLabel.copy(
                    fontSize = scaledLabelFontSize,
                    lineHeight = scaledLabelFontSize * 1.28f,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
                Text(
                    text = title,
                    style = scaledLabelStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "금주 기록 화면 - 빈 상태")
@Preview(showBackground = true, name = "금주 기록 화면 - 데이터 있음", fontScale = 1.2f)
@Composable
fun PreviewRecordsScreen() {
    AlcoholicTimerTheme {
        RecordsScreen(externalRefreshTrigger = 0, onNavigateToDetail = {})
    }
}

@Preview(showBackground = true, name = "빈 상태")
@Composable
fun PreviewEmptyRecordsState() {
    AlcoholicTimerTheme { EmptyRecordsState() }
}

@Preview(showBackground = true, name = "기록 카드")
@Composable
fun PreviewRecordCard() {
    AlcoholicTimerTheme {
        RecordCard(
            record = SobrietyRecord(
                id = "sample",
                startTime = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L),
                endTime = System.currentTimeMillis(),
                targetDays = 30,
                actualDays = 10,
                isCompleted = false,
                status = "진행 중",
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}
