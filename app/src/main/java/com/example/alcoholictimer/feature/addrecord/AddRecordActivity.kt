package com.example.alcoholictimer.feature.addrecord

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoholictimer.core.ui.theme.AlcoholicTimerTheme
import com.example.alcoholictimer.core.data.RecordsDataLoader
import com.example.alcoholictimer.core.model.SobrietyRecord
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit
import com.example.alcoholictimer.feature.addrecord.components.TargetDaysBottomSheet

class AddRecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 앱은 라이트 모드 고정 정책: 다크 모드 진입 방지
            AlcoholicTimerTheme(darkTheme = false) {
                // 전역 배경을 연회색으로, 내부 주요 Surface는 흰색 유지
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
                    AddRecordScreen(
                        onSave = { record ->
                            val success = saveRecord(record)
                            if (success) {
                                Toast.makeText(this, "금주 기록이 추가되었습니다", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(this, "선택한 시간이 기존 기록과 겹칩니다", Toast.LENGTH_LONG).show()
                            }
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun saveRecord(record: SobrietyRecord): Boolean {
        return try {
            val currentRecords = RecordsDataLoader.loadSobrietyRecords(this).toMutableList()
            // 시간 겹침 방지
            val hasTimeConflict = currentRecords.any { existingRecord ->
                val newStart = record.startTime
                val newEnd = record.endTime
                val existingStart = existingRecord.startTime
                val existingEnd = existingRecord.endTime
                (newStart < existingEnd && newEnd > existingStart)
            }
            if (hasTimeConflict) return false
            currentRecords.add(record)
            currentRecords.sortByDescending { it.endTime }
            val sharedPref = getSharedPreferences("user_settings", MODE_PRIVATE)
            val jsonString = SobrietyRecord.toJsonArray(currentRecords)
            sharedPref.edit { putString("sobriety_records", jsonString) }
            true
        } catch (e: Exception) {
            Log.e("AddRecord", "기록 저장 실패", e)
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecordScreen(
    onSave: (SobrietyRecord) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var targetDays by remember { mutableStateOf("0") }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)) }
    var endDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var startTime by remember { mutableStateOf(Pair(9, 0)) }
    var endTime by remember { mutableStateOf(Pair(18, 0)) }

    // 목표 일수: 휠 피커 바텀시트 사용
    var showTargetSheet by remember { mutableStateOf(false) }
    var tempTarget by remember(targetDays) { mutableIntStateOf(targetDays.toIntOrNull()?.coerceIn(0, 999) ?: 0) }

    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())

    // 밀리초 계산(선택 날짜 + 시간)
    val startMillis by remember(startDate, startTime) {
        mutableLongStateOf(
            Calendar.getInstance().apply {
                timeInMillis = startDate
                set(Calendar.HOUR_OF_DAY, startTime.first)
                set(Calendar.MINUTE, startTime.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }
    val endMillis by remember(endDate, endTime) {
        mutableLongStateOf(
            Calendar.getInstance().apply {
                timeInMillis = endDate
                set(Calendar.HOUR_OF_DAY, endTime.first)
                set(Calendar.MINUTE, endTime.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }
    val nowMillis = System.currentTimeMillis()

    // 유효성 체크
    val isRangeInvalid = endMillis <= startMillis
    val isOngoing = endMillis > nowMillis
    val targetDaysInt = targetDays.toIntOrNull() ?: 0
    val isTargetValid = targetDays.isNotBlank() && targetDaysInt in 1..999

    // 실제 기간(일)과 완료 여부 계산
    val actualDays = remember(startMillis, endMillis) {
        ((endMillis - startMillis).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
    }
    val isCompleted = !isOngoing && !isRangeInvalid && isTargetValid && actualDays >= targetDaysInt

    fun pickDateThenTime(initialDateMillis: Long, initialHour: Int, initialMinute: Int, onPicked: (dateMillis: Long, hour: Int, minute: Int) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialDateMillis }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val pickedCal = Calendar.getInstance().apply { set(y, m, d) }
                TimePickerDialog(
                    context,
                    { _, h, min -> onPicked(pickedCal.timeInMillis, h, min) },
                    initialHour,
                    initialMinute,
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    TopAppBar(
                        title = { Text("금주 기록 추가", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onCancel) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                            }
                        }
                    )
                    HorizontalDivider(
                        thickness = 1.5.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        },
        // 전역 배경은 연회색으로 설정
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // 화면 배경도 연회색 유지
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 시작일 및 시간
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable {
                        pickDateThenTime(startDate, startTime.first, startTime.second) { newDate, h, m ->
                            startDate = newDate
                            startTime = h to m
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("시작일 및 시간", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text(
                    text = "${dateFormat.format(Date(startDate))} ${String.format(Locale.getDefault(), "%02d:%02d", startTime.first, startTime.second)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // 종료일 및 시간
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable {
                        pickDateThenTime(endDate, endTime.first, endTime.second) { newDate, h, m ->
                            endDate = newDate
                            endTime = h to m
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("종료일 및 시간", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text(
                    text = "${dateFormat.format(Date(endDate))} ${String.format(Locale.getDefault(), "%02d:%02d", endTime.first, endTime.second)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // 목표 일수 (행 형식 + 바텀시트 휠 피커)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable {
                        tempTarget = targetDays.toIntOrNull()?.coerceIn(0, 999) ?: 0
                        showTargetSheet = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("목표 일수", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text(
                    text = "${targetDays}일",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // 경고/안내
            if (isRangeInvalid) {
                Text("종료는 시작 이후여야 합니다", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            if (isOngoing) {
                Text("종료 시점은 현재 시각 이전이어야 합니다", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            if (!isTargetValid) {
                Text("목표 일수를 올바르게 입력하세요", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            // 저장/취소
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) { Text("취소") }

                Button(
                    onClick = {
                        if (!isRangeInvalid && !isOngoing && isTargetValid) {
                            val id = "rec_${System.currentTimeMillis()}"
                            val status = if (isCompleted) "성공" else "실패"
                            val record = SobrietyRecord(
                                id = id,
                                startTime = startMillis,
                                endTime = endMillis,
                                targetDays = targetDaysInt,
                                actualDays = actualDays,
                                isCompleted = isCompleted,
                                status = status,
                                createdAt = System.currentTimeMillis()
                            )
                            onSave(record)
                        }
                    },
                    enabled = !isRangeInvalid && !isOngoing && isTargetValid,
                    modifier = Modifier.weight(1f)
                ) { Text("저장") }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (showTargetSheet) {
            // 바텀시트는 핵심 대화형 요소이므로 흰색 Surface 유지
            com.example.alcoholictimer.feature.addrecord.components.TargetDaysBottomSheet(
                initialValue = tempTarget,
                onConfirm = { picked: Int ->
                    targetDays = picked.toString()
                    showTargetSheet = false
                },
                onDismiss = { showTargetSheet = false }
            )
        }
    }
}
