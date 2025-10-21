package com.sweetapps.faceweight.feature.run

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import com.sweetapps.faceweight.core.ui.BaseActivity
import com.sweetapps.faceweight.core.ui.StandardScreenWithBottomButton
import com.sweetapps.faceweight.core.ui.LayoutConstants
import com.sweetapps.faceweight.core.util.FormatUtils
import com.sweetapps.faceweight.feature.start.StartActivity
import com.sweetapps.faceweight.core.ui.AppElevation

class QuitActivity : BaseActivity() {
    override fun getScreenTitle(): String = getString(R.string.quit_title)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BaseScreen(applyBottomInsets = false) { QuitScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuitScreen() {
    val context = LocalContext.current
    val activity = context as? QuitActivity
    val intent = activity?.intent
    val elapsedDays = intent?.getIntExtra("elapsed_days", 0) ?: 0
    val elapsedHours = intent?.getIntExtra("elapsed_hours", 0) ?: 0
    val elapsedMinutes = intent?.getIntExtra("elapsed_minutes", 0) ?: 0
    val savedMoney = intent?.getDoubleExtra("saved_money", 0.0) ?: 0.0
    val savedHours = intent?.getDoubleExtra("saved_hours", 0.0) ?: 0.0
    val lifeGainDays = intent?.getDoubleExtra("life_gain_days", 0.0) ?: 0.0
    val sharedPref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    val targetDays = sharedPref.getFloat("target_days", 30f)

    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    StandardScreenWithBottomButton(
        topContent = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LayoutConstants.CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD), // lowered from CARD_HIGH
                border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light)) // added for depth after elevation reduction
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(LayoutConstants.CARD_PADDING),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1f)) {
                        Text("🤔", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    Text(
                        text = stringResource(id = R.string.quit_confirm_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.quit_confirm_subtitle),
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
            StatisticsCardsSection(
                elapsedDays = elapsedDays,
                elapsedHours = elapsedHours,
                elapsedMinutes = elapsedMinutes,
                savedMoney = savedMoney,
                savedHours = savedHours,
                lifeGainDays = lifeGainDays
            )
        },
        bottomButton = {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(106.dp)) {
                        CircularProgressIndicator(
                            progress = { 1f }, modifier = Modifier.size(106.dp),
                            color = Color(0xFFE0E0E0), strokeWidth = 4.dp, trackColor = Color.Transparent
                        )
                        if (isPressed) {
                            CircularProgressIndicator(
                                progress = { progress }, modifier = Modifier.size(106.dp),
                                color = Color(0xFFD32F2F), strokeWidth = 4.dp, trackColor = Color.Transparent
                            )
                        }
                        Card(
                            modifier = Modifier.size(96.dp).pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(); isPressed = true; progress = 0f
                                    val job = coroutineScope.launch {
                                        val duration = 1500L
                                        val startMs = System.currentTimeMillis()
                                        while (progress < 1f && isPressed) {
                                            val elapsed = System.currentTimeMillis() - startMs
                                            progress = (elapsed.toFloat() / duration).coerceAtMost(1f)
                                            delay(16)
                                        }
                                        if (progress >= 1f && isPressed) {
                                            saveCompletedRecord(
                                                context = context,
                                                startTime = System.currentTimeMillis() - (elapsedDays * 24L * 60 * 60 * 1000),
                                                endTime = System.currentTimeMillis(),
                                                targetDays = targetDays,
                                                actualDays = elapsedDays
                                            )
                                            sharedPref.edit {
                                                remove("start_time")
                                                putBoolean("timer_completed", true)
                                            }
                                            val intent = Intent(context, StartActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                    waitForUpOrCancellation(); isPressed = false; job.cancel()
                                    coroutineScope.launch {
                                        while (progress > 0f) { progress = (progress - 0.1f).coerceAtLeast(0f); delay(16) }
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = if (isPressed) Color(0xFFD32F2F) else Color(0xFFE53935)),
                            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD_HIGH)
                        ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.cd_stop), tint = Color.White, modifier = Modifier.size(48.dp))
                        } }
                    }
                    Card(
                        onClick = { (context as? QuitActivity)?.finish() },
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD_HIGH)
                    ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.cd_continue), tint = Color.White, modifier = Modifier.size(48.dp))
                    } }
                }
            }
        }
    )
}

@Composable
fun StatisticsCardsSection(
    elapsedDays: Int,
    elapsedHours: Int,
    elapsedMinutes: Int,
    savedMoney: Double,
    savedHours: Double,
    lifeGainDays: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LayoutConstants.STAT_ROW_SPACING)
    ) {
        val totalDaysDecimal = elapsedDays.toDouble() + (elapsedHours.toDouble() / 24.0) + (elapsedMinutes.toDouble() / (24.0 * 60.0))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LayoutConstants.STAT_ROW_SPACING)
        ) {
            com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                value = String.format(Locale.getDefault(), "%.1f일", totalDaysDecimal),
                label = stringResource(id = R.string.stat_total_days),
                modifier = Modifier.weight(1f),
                valueColor = colorResource(id = R.color.color_indicator_days)
            )
            com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                value = String.format(Locale.getDefault(), "%,.0f원", savedMoney),
                label = stringResource(id = R.string.stat_saved_money_short),
                modifier = Modifier.weight(1f),
                valueColor = colorResource(id = R.color.color_indicator_money)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LayoutConstants.STAT_ROW_SPACING)
        ) {
            com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                value = String.format(Locale.getDefault(), "%.1f시간", savedHours),
                label = stringResource(id = R.string.stat_saved_hours_short),
                modifier = Modifier.weight(1f),
                valueColor = colorResource(id = R.color.color_indicator_hours)
            )
            com.sweetapps.faceweight.feature.detail.components.DetailStatCard(
                value = FormatUtils.daysToDayHourString(lifeGainDays, 2),
                label = stringResource(id = R.string.indicator_title_life_gain),
                modifier = Modifier.weight(1f),
                valueColor = colorResource(id = R.color.color_indicator_life)
            )
        }
    }
}

private fun saveCompletedRecord(
    context: Context,
    startTime: Long,
    endTime: Long,
    targetDays: Float,
    actualDays: Int
) {
    try {
        val sharedPref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val record = JSONObject().apply {
            put("id", System.currentTimeMillis().toString())
            put("startTime", startTime)
            put("endTime", endTime)
            put("targetDays", targetDays.toInt())
            put("actualDays", actualDays)
            put("isCompleted", (actualDays.toFloat() / targetDays) >= 1f)
            put("status", if ((actualDays.toFloat() / targetDays) >= 1f) "완료" else "중지")
            put("createdAt", System.currentTimeMillis())
        }
        val recordsJson = sharedPref.getString("sobriety_records", "[]") ?: "[]"
        val recordsList = try { JSONArray(recordsJson) } catch (_: Exception) { JSONArray() }
        recordsList.put(record)
        sharedPref.edit { putString("sobriety_records", recordsList.toString()) }
    } catch (e: Exception) {
        Log.e("QuitActivity", "기록 저장 중 오류", e)
    }
}

@Preview(showBackground = true)
@Composable
fun QuitScreenPreview() { QuitScreen() }
