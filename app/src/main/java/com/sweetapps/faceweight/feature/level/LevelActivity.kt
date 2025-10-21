package com.sweetapps.faceweight.feature.level

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweetapps.faceweight.core.ui.AppElevation
import com.sweetapps.faceweight.core.ui.BaseActivity
import com.sweetapps.faceweight.core.util.Constants
import com.sweetapps.faceweight.core.data.RecordsDataLoader
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import com.sweetapps.faceweight.core.ui.LocalSafeContentPadding

class LevelActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseScreen { LevelScreen() }
        }
    }

    override fun getScreenTitle(): String = "금주 레벨"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelScreen() {
    val context = LocalContext.current

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val sharedPref = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    val startTime = sharedPref.getLong("start_time", 0L)

    val currentElapsedTime = if (startTime > 0) currentTime - startTime else 0L

    val pastRecords = RecordsDataLoader.loadSobrietyRecords(context)
    val totalPastDuration = pastRecords.sumOf { record -> (record.endTime - record.startTime) }

    val totalElapsedTime = totalPastDuration + currentElapsedTime
    // 추가: 총 경과 일수(소수점 포함) 계산
    val totalElapsedDaysFloat = totalElapsedTime / Constants.DAY_IN_MILLIS.toFloat()

    val levelDays = Constants.calculateLevelDays(totalElapsedTime)
    val currentLevel = LevelDefinitions.getLevelInfo(levelDays)

    // BaseScreen에서 제공하는 하단 안전 패딩(LocalSafeContentPadding)을 그대로 소비
    val safePadding = LocalSafeContentPadding.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .padding(safePadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 변경: float 경과 일수 전달
        CurrentLevelCard(currentLevel = currentLevel, currentDays = levelDays, elapsedDaysFloat = totalElapsedDaysFloat, startTime = startTime)
        LevelListCard(currentLevel = currentLevel, currentDays = levelDays)
        // 하단 여백 추가 제거: BaseScreen이 safe area까지 처리
    }
}

@Composable
private fun CurrentLevelCard(
    currentLevel: LevelDefinitions.LevelInfo,
    currentDays: Int,
    elapsedDaysFloat: Float,
    startTime: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
        border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(colors = listOf(currentLevel.color.copy(alpha = 0.8f), currentLevel.color))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = currentLevel.name.take(2), style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentLevel.name,
                style = MaterialTheme.typography.headlineLarge.copy(color = currentLevel.color),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text = "$currentDays", style = MaterialTheme.typography.headlineLarge.copy(color = Color(0xFF1976D2)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "일차", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF666666)))
            }

            val nextLevel = getNextLevel(currentLevel)
            if (nextLevel != null) {
                Spacer(modifier = Modifier.height(24.dp))

                // 변경: 정수 일수 대신 실수 일수 기반 진행률 계산
                val progress = if (nextLevel.start > currentLevel.start) {
                    val progressInLevel = elapsedDaysFloat - currentLevel.start
                    val totalNeeded = (nextLevel.start - currentLevel.start).toFloat()
                    if (totalNeeded > 0f) (progressInLevel / totalNeeded).coerceIn(0f, 1f) else 0f
                } else 0f

                // 추가: 남은 시간(일+시간) 문자열 생성
                val remainingDaysFloat = (nextLevel.start - elapsedDaysFloat).coerceAtLeast(0f)
                val remainingDaysInt = kotlin.math.floor(remainingDaysFloat.toDouble()).toInt()
                val remainingHoursInt = kotlin.math.floor(((remainingDaysFloat - remainingDaysInt) * 24f).toDouble()).toInt()
                val remainingText = when {
                    remainingDaysInt > 0 && remainingHoursInt > 0 -> "${remainingDaysInt}일 ${remainingHoursInt}시간 남음"
                    remainingDaysInt > 0 -> "${remainingDaysInt}일 남음"
                    remainingHoursInt > 0 -> "${remainingHoursInt}시간 남음"
                    else -> "곧 레벨업"
                }

                ProgressToNextLevel(
                    currentLevel = currentLevel,
                    nextLevel = nextLevel,
                    progress = progress,
                    remainingDays = (nextLevel.start - currentDays).coerceAtLeast(0),
                    remainingText = remainingText,
                    isSobrietyActive = startTime > 0
                )
            }
        }
    }
}

@Composable
private fun ProgressToNextLevel(
    currentLevel: LevelDefinitions.LevelInfo,
    nextLevel: LevelDefinitions.LevelInfo,
    progress: Float,
    remainingDays: Int,
    remainingText: String,
    isSobrietyActive: Boolean
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(remainingDays, isSobrietyActive) {
        if (remainingDays > 0 && isSobrietyActive) {
            while (true) {
                delay(1000)
                isVisible = !isVisible
            }
        } else {
            isVisible = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = tween(durationMillis = 500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "indicator_blink"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(text = "다음 레벨까지", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium, color = Color(0xFF666666)))
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (remainingDays > 0 && isSobrietyActive) currentLevel.color.copy(alpha = alpha) else Color(0xFF999999))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 1000), label = "progress")
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(Brush.horizontalGradient(colors = listOf(nextLevel.color.copy(alpha = 0.7f), nextLevel.color)))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = String.format(Locale.getDefault(), "%.1f%%", progress * 100), style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF999999)))
            Text(text = remainingText, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF999999)))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) { }
    }
}

@Composable
private fun LevelListCard(currentLevel: LevelDefinitions.LevelInfo, currentDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
        border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "전체 레벨",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF333333)),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LevelDefinitions.levels.forEach { level ->
                LevelItem(
                    level = level,
                    isCurrent = level == currentLevel,
                    isAchieved = currentDays >= level.start,
                    isNext = level == getNextLevel(currentLevel)
                )

                if (level != LevelDefinitions.levels.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun LevelItem(
    level: LevelDefinitions.LevelInfo,
    isCurrent: Boolean,
    isAchieved: Boolean,
    isNext: Boolean
) {
    // 내부 리스트 아이템에서는 그림자를 사용하지 않는다(중첩 음영에 의한 두꺼운 회색띠 방지)
    val itemElevation = AppElevation.ZERO
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrent -> level.color.copy(alpha = 0.1f)
                isAchieved -> level.color.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            isCurrent -> BorderStroke(1.5.dp, level.color)
            isAchieved -> BorderStroke(1.dp, level.color.copy(alpha = 0.6f))
            else -> BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = itemElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isAchieved) level.color else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.name.take(1),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = if (isAchieved) Color.White else Color(0xFF757575))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.name,
                    style = (if (isCurrent) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleMedium)
                        .copy(color = if (isAchieved) level.color else Color(0xFF757575))
                )

                val rangeText = if (level.end == Int.MAX_VALUE) "${level.start}일 이상" else "${level.start}~${level.end}일"
                Text(text = rangeText, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF666666)))
            }

            if (isCurrent) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = "현재 레벨", tint = level.color, modifier = Modifier.size(20.dp))
            } else if (isAchieved) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "달성 완료", tint = level.color, modifier = Modifier.size(20.dp))
            } else {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = "미달성", tint = Color(0xFFBDBDBD), modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun getNextLevel(currentLevel: LevelDefinitions.LevelInfo): LevelDefinitions.LevelInfo? {
    val currentIndex = LevelDefinitions.levels.indexOf(currentLevel)
    return if (currentIndex < LevelDefinitions.levels.size - 1) LevelDefinitions.levels[currentIndex + 1] else null
}

@Preview(showBackground = true, name = "LevelScreen - 기본", widthDp = 360, heightDp = 800)
@Composable
fun LevelScreenPreview() {
    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) { LevelScreen() }
    }
}
