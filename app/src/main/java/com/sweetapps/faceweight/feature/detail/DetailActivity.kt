package com.sweetapps.faceweight.feature.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sweetapps.faceweight.R
import com.sweetapps.faceweight.core.ui.AppElevation
import com.sweetapps.faceweight.core.ui.LayoutConstants
import com.sweetapps.faceweight.core.ui.theme.AmberSecondaryLight
import com.sweetapps.faceweight.core.ui.theme.BluePrimaryLight
import com.sweetapps.faceweight.core.ui.theme.AlcoholicTimerTheme
import com.sweetapps.faceweight.core.util.Constants
import com.sweetapps.faceweight.core.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.core.content.edit // SharedPreferences 확장 함수 import 복구

class DetailActivity : ComponentActivity() {

    companion object {
        private const val TAG = "DetailActivity"

        fun start(
            context: Context,
            startTime: Long,
            endTime: Long,
            targetDays: Float,
            actualDays: Int,
            isCompleted: Boolean
        ) {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("start_time", startTime)
                putExtra("end_time", endTime)
                putExtra("target_days", targetDays)
                putExtra("actual_days", actualDays)
                putExtra("is_completed", isCompleted)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "===== DetailActivity onCreate 시작 =====")

        try {
            val startTime = intent.getLongExtra("start_time", 0L)
            val endTime = intent.getLongExtra("end_time", System.currentTimeMillis())
            val targetDays = intent.getFloatExtra("target_days", 30f)
            val actualDays = intent.getIntExtra("actual_days", 0)
            val isCompleted = intent.getBooleanExtra("is_completed", false)

            Log.d(TAG, "수신된 데이터: startTime=$startTime, endTime=$endTime, targetDays=$targetDays, actualDays=$actualDays, isCompleted=$isCompleted")

            if (actualDays < 0) {
                Log.e(TAG, "잘못된 데이터: actualDays=$actualDays")
                finish()
                return
            }

            val safeTargetDays = if (targetDays <= 0) 30f else targetDays
            val safeActualDays = if (actualDays <= 0) 1 else actualDays

            Log.d(TAG, "안전한 값들: targetDays=$safeTargetDays, actualDays=$safeActualDays")

            setContent {
                AlcoholicTimerTheme(darkTheme = false) {
                    DetailScreen(
                        startTime = startTime,
                        endTime = endTime,
                        targetDays = safeTargetDays,
                        actualDays = safeActualDays,
                        isCompleted = isCompleted,
                        onBack = { finish() }
                    )
                }
            }
            Log.d(TAG, "===== DetailActivity onCreate 완료 =====")
        } catch (e: Exception) {
            Log.e(TAG, "DetailActivity 초기화 중 오류", e)
            Log.e(TAG, "오류 스택트레이스: ${e.stackTraceToString()}")
            finish()
        }
    }
}

@Composable
fun DetailScreen(
    startTime: Long,
    endTime: Long,
    targetDays: Float,
    actualDays: Int,
    isCompleted: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val accentColor = if (isCompleted) BluePrimaryLight else AmberSecondaryLight

    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd - a h:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    val displayDateTime = if (startTime > 0) {
        dateTimeFormat.format(Date(startTime))
    } else {
        val nowFormatted = SimpleDateFormat("a h:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date())
        stringResource(id = R.string.detail_today_time, nowFormatted)
    }

    val totalDurationMillis = if (startTime > 0) endTime - startTime else actualDays * 24L * 60 * 60 * 1000L
    val totalHours = totalDurationMillis / (60 * 60 * 1000.0)
    val totalDays = totalHours / 24.0

    val (selectedCost, selectedFrequency, selectedDuration) = Constants.getUserSettings(context)

    val costVal = when(selectedCost) {
        "저" -> 10000
        "중" -> 40000
        "고" -> 70000
        else -> 40000
    }

    val freqVal = when(selectedFrequency) {
        "주 1회 이하" -> 1.0
        "주 2~3회" -> 2.5
        "주 4회 이상" -> 5.0
        else -> 2.5
    }

    val drinkHoursVal = when(selectedDuration) {
        "짧음" -> 2
        "보통" -> 4
        "길게" -> 6
        else -> 4
    }

    val hangoverHoursVal = 5

    val exactWeeks = totalHours / (24.0 * 7.0)
    val savedMoney = (exactWeeks * freqVal * costVal).roundToInt()
    val savedHoursExact = (exactWeeks * freqVal * (drinkHoursVal + hangoverHoursVal))

    val achievementRate = ((totalDays / targetDays) * 100.0).let { rate -> if (rate > 100) 100.0 else rate }
    val lifeExpectancyIncrease = totalDays / 30.0

    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = density.fontScale * 0.9f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 전역 배경을 연회색으로 변경
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .windowInsetsPadding(
                    // 하단은 전역에서 처리하지 않음: 수평만 적용
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onBack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(id = R.string.cd_navigate_back),
                                tint = Color(0xFF2D3748),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
                            val base = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            val scaled = base.copy(fontSize = (base.fontSize.value * 1.3f).sp)
                            Text(
                                text = stringResource(id = R.string.detail_title),
                                style = scaled,
                                color = Color(0xFF2D3748),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(id = R.string.dialog_delete_title),
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
                    border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "${stringResource(id = R.string.detail_start_label)} $displayDateTime",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF718096)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${stringResource(id = R.string.detail_end_label)} ${dateTimeFormat.format(Date(endTime))}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF718096)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f", totalDays),
                                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = accentColor
                                )
                                Text(
                                    text = stringResource(id = R.string.unit_day),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF718096)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.detail_progress_rate),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF718096)
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f%%", achievementRate),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (achievementRate / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = accentColor,
                                trackColor = Color(0xFFE2E8F0)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(id = R.string.detail_progress_current, String.format(Locale.getDefault(), "%.1f", totalDays)),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF718096)
                                )
                                Text(
                                    text = stringResource(id = R.string.detail_progress_target, targetDays.toInt()),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF718096)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LayoutConstants.STAT_ROW_SPACING)
                ) {
                    com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                        value = String.format(Locale.getDefault(), "%.1f일", totalDays),
                        label = stringResource(id = R.string.stat_total_days),
                        modifier = Modifier.weight(1f),
                        valueColor = colorResource(id = R.color.color_indicator_days)
                    )
                    com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                        value = String.format(Locale.getDefault(), "%,.0f원", savedMoney.toDouble()),
                        label = stringResource(id = R.string.stat_saved_money_short),
                        modifier = Modifier.weight(1f),
                        valueColor = colorResource(id = R.color.color_indicator_money)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LayoutConstants.STAT_ROW_SPACING)
                ) {
                    com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                        value = String.format(Locale.getDefault(), "%.1f시간", savedHoursExact),
                        label = stringResource(id = R.string.stat_saved_hours_short),
                        modifier = Modifier.weight(1f),
                        valueColor = colorResource(id = R.color.color_indicator_hours)
                    )
                    com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                        value = FormatUtils.daysToDayHourString(lifeExpectancyIncrease, 2),
                        label = stringResource(id = R.string.indicator_title_life_gain),
                        modifier = Modifier.weight(1f),
                        valueColor = colorResource(id = R.color.color_indicator_life)
                    )
                }

                // 하단 여백 축소
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(
                            stringResource(id = R.string.dialog_delete_title),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    },
                    text = {
                        Text(
                            stringResource(id = R.string.dialog_delete_message),
                            color = Color(0xFF4A5568)
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                try {
                                    deleteRecord(context, startTime, endTime)
                                } catch (e: Exception) {
                                    Log.e("DetailActivity", "삭제 중 오류", e)
                                }
                                val activity = (context as? DetailActivity)
                                activity?.setResult(Activity.RESULT_OK)
                                activity?.finish()
                            }
                        ) {
                            Text(stringResource(id = R.string.dialog_delete_confirm), color = Color(0xFFE53E3E), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(id = R.string.dialog_cancel), color = Color(0xFF718096))
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

private fun deleteRecord(context: Context, startTime: Long, endTime: Long) {
    val sharedPref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    val jsonString = sharedPref.getString("sobriety_records", null) ?: return
    try {
        val originalArray = org.json.JSONArray(jsonString)
        val newArray = org.json.JSONArray()
        var removedCount = 0
        for (i in 0 until originalArray.length()) {
            val obj = originalArray.getJSONObject(i)
            // 저장 시 사용된 camelCase 우선, 혹시 남아있을 수 있는 snake_case fallback
            val s = if (obj.has("startTime")) obj.optLong("startTime", -1) else obj.optLong("start_time", -1)
            val e = if (obj.has("endTime")) obj.optLong("endTime", -1) else obj.optLong("end_time", -1)
            if (s == startTime && e == endTime) {
                removedCount++
            } else {
                newArray.put(obj)
            }
        }
        if (removedCount > 0) {
            sharedPref.edit { putString("sobriety_records", newArray.toString()) }
            Log.d("DetailActivity", "삭제 성공: ${removedCount}개 기록 제거 (start=$startTime, end=$endTime)")
        } else {
            Log.w("DetailActivity", "삭제 대상 기록을 찾지 못함 (start=$startTime, end=$endTime)")
        }
    } catch (e: Exception) {
        Log.e("DetailActivity", "기록 삭제 중 오류", e)
    }
}
