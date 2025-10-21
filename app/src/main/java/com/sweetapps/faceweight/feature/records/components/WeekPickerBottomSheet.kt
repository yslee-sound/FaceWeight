package com.sweetapps.faceweight.feature.records.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sweetapps.faceweight.core.ui.AppAlphas
import com.sweetapps.faceweight.core.ui.components.NumberPicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPickerBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onWeekPicked: (weekStart: Long, weekEnd: Long, displayText: String) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFF636E72).copy(alpha = AppAlphas.SurfaceTint), RoundedCornerShape(2.dp))
                )
            }
        ) { WeekPickerContent(onWeekPicked = onWeekPicked, onDismiss = onDismiss) }
    }
}

@Composable
internal fun WeekPickerContent(
    onWeekPicked: (weekStart: Long, weekEnd: Long, displayText: String) -> Unit,
    onDismiss: () -> Unit
) {
    val weekOptions = remember { generateWeekOptions() }
    var selectedWeekIndex by remember { mutableIntStateOf(weekOptions.size - 1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "주 선택",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        NumberPicker(
            value = selectedWeekIndex,
            onValueChange = { selectedWeekIndex = it },
            range = 0 until weekOptions.size,
            displayValues = weekOptions.map { it.displayText },
            modifier = Modifier.width(220.dp)
        )

        Button(
            onClick = {
                val selectedWeek = weekOptions[selectedWeekIndex]
                onWeekPicked(selectedWeek.startTime, selectedWeek.endTime, selectedWeek.displayText)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF74B9FF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) { Text(text = "선택", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class WeekOption(
    val startTime: Long,
    val endTime: Long,
    val displayText: String
)

private fun generateWeekOptions(): List<WeekOption> {
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }
    val options = mutableListOf<WeekOption>()

    val cal = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.SUNDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }

    for (i in 0 until 4) {
        val weekStart = cal.timeInMillis
        val calEnd = cal.clone() as Calendar
        calEnd.add(Calendar.DAY_OF_WEEK, 6)
        val weekEndInclusive = calEnd.timeInMillis + (24 * 60 * 60 * 1000L - 1)
        val startDate = dateFormat.format(Date(weekStart))
        val endDate = dateFormat.format(Date(calEnd.timeInMillis))
        val displayText = when (i) { 0 -> "이번 주"; 1 -> "지난 주"; else -> "$startDate ~ $endDate" }
        options.add(WeekOption(weekStart, weekEndInclusive, displayText))
        cal.add(Calendar.DAY_OF_YEAR, -7)
    }

    return options.reversed()
}

@Preview(showBackground = true)
@Composable
fun WeekPickerBottomSheetPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        WeekPickerContent(onWeekPicked = { _, _, _ -> }, onDismiss = { })
    }
}

@Preview(showBackground = true, name = "WeekPicker - Dark Mode")
@Composable
fun WeekPickerBottomSheetDarkPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        WeekPickerContent(onWeekPicked = { _, _, _ -> }, onDismiss = { })
    }
}
