package com.example.alcoholictimer.feature.addrecord.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alcoholictimer.core.ui.components.NumberPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetDaysBottomSheet(
    initialValue: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val clamped = initialValue.coerceIn(0, 999)
    var hundreds by remember(clamped) { mutableIntStateOf(clamped / 100) }
    var tens by remember(clamped) { mutableIntStateOf((clamped / 10) % 10) }
    var ones by remember(clamped) { mutableIntStateOf(clamped % 10) }

    val current by remember { derivedStateOf { hundreds * 100 + tens * 10 + ones } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("목표 일수 선택", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                NumberPicker(
                    value = hundreds,
                    onValueChange = { hundreds = it.coerceIn(0, 9) },
                    range = 0..9,
                    modifier = Modifier.width(64.dp)
                )
                Spacer(Modifier.width(12.dp))
                NumberPicker(
                    value = tens,
                    onValueChange = { tens = it.coerceIn(0, 9) },
                    range = 0..9,
                    modifier = Modifier.width(64.dp)
                )
                Spacer(Modifier.width(12.dp))
                NumberPicker(
                    value = ones,
                    onValueChange = { ones = it.coerceIn(0, 9) },
                    range = 0..9,
                    modifier = Modifier.width(64.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("일", style = MaterialTheme.typography.titleMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("취소") }

                Button(
                    onClick = { onConfirm(current) },
                    modifier = Modifier.weight(1f)
                ) { Text("확인") }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
