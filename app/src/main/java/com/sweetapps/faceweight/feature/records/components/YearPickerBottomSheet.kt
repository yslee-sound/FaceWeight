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
import com.sweetapps.faceweight.core.model.SobrietyRecord
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearPickerBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onYearPicked: (year: Int) -> Unit,
    records: List<SobrietyRecord> = emptyList(),
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR)
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
        ) {
            YearPickerContent(
                onYearPicked = onYearPicked,
                onDismiss = onDismiss,
                records = records,
                initialYear = initialYear
            )
        }
    }
}

@Composable
internal fun YearPickerContent(
    onYearPicked: (year: Int) -> Unit,
    onDismiss: () -> Unit,
    records: List<SobrietyRecord>,
    initialYear: Int
) {
    val yearOptions = remember(records) {
        if (records.isEmpty()) emptyList() else run {
            val nowYear = Calendar.getInstance().get(Calendar.YEAR)
            val startYear = records.minByOrNull { it.startTime }?.let {
                Calendar.getInstance().apply { timeInMillis = it.startTime }.get(Calendar.YEAR)
            } ?: nowYear
            (startYear..nowYear).toList()
        }
    }

    val defaultIndex = remember(yearOptions, initialYear) {
        yearOptions.indexOf(initialYear).let { if (it >= 0) it else (yearOptions.size - 1).coerceAtLeast(0) }
    }
    var selectedIndex by remember(yearOptions) { mutableIntStateOf(defaultIndex) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "연도 선택",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val canSelect = yearOptions.isNotEmpty()

        if (canSelect) {
            NumberPicker(
                value = selectedIndex,
                onValueChange = { selectedIndex = it },
                range = 0 until yearOptions.size,
                displayValues = yearOptions.map { "${it}년" },
                modifier = Modifier.width(160.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                contentAlignment = Alignment.Center
            ) { Text(text = "표시할 항목이 없습니다", color = Color(0xFF636E72), fontSize = 14.sp) }
        }

        Button(
            onClick = {
                val year = yearOptions.getOrNull(selectedIndex)
                if (year != null) {
                    onYearPicked(year)
                    onDismiss()
                }
            },
            enabled = canSelect,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF74B9FF), contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) { Text(text = "선택", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun YearPickerBottomSheetPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        YearPickerContent(onYearPicked = { }, onDismiss = { }, records = emptyList(), initialYear = 2025)
    }
}

@Preview(showBackground = true, name = "YearPicker - Dark Mode")
@Composable
fun YearPickerBottomSheetDarkPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        YearPickerContent(onYearPicked = { }, onDismiss = { }, records = emptyList(), initialYear = 2025)
    }
}
