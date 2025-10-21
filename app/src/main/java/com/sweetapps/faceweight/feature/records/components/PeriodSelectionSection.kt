package com.sweetapps.faceweight.feature.records.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sweetapps.faceweight.core.ui.AppElevation
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke
import android.os.SystemClock

@Composable
fun PeriodSelectionSection(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onPeriodClick: (String) -> Unit,
    selectedDetailPeriod: String,
    modifier: Modifier = Modifier
) {
    val periods = listOf("주", "월", "년", "전체")

    // 초간단 디바운스: 너무 빠른 연속 탭 무시
    var lastClickAt by remember { mutableStateOf(0L) }
    val debounceMs = 250L

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
            border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                periods.forEach { period ->
                    val isSelected = period == selectedPeriod
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable {
                                val now = SystemClock.elapsedRealtime()
                                if (now - lastClickAt >= debounceMs) {
                                    lastClickAt = now
                                    onPeriodSelected(period)
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF74B9FF) else Color.Transparent
                    ) {
                        Text(
                            text = period,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF636E72)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (selectedPeriod == "전체") Modifier else Modifier.clickable {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastClickAt >= debounceMs) {
                        lastClickAt = now
                        onPeriodClick(selectedPeriod)
                    }
                }),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
            border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getCurrentPeriodText(selectedPeriod, selectedDetailPeriod),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C3E50)
                )
                if (selectedPeriod != "전체") {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "세부 기간 선택",
                        tint = Color(0xFF74B9FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getCurrentPeriodText(selectedPeriod: String, selectedDetailPeriod: String): String {
    val calendar = Calendar.getInstance()

    return when (selectedPeriod) {
        "주" -> if (selectedDetailPeriod.isNotEmpty()) selectedDetailPeriod else "이번 주"
        "월" -> if (selectedDetailPeriod.isNotEmpty()) selectedDetailPeriod else SimpleDateFormat("yyyy년 M월", Locale.getDefault()).format(calendar.time)
        "년" -> if (selectedDetailPeriod.isNotEmpty()) selectedDetailPeriod else SimpleDateFormat("yyyy년", Locale.getDefault()).format(calendar.time)
        else -> "전체"
    }
}
