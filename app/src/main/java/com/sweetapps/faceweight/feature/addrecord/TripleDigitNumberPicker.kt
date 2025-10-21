package com.sweetapps.faceweight.feature.addrecord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.sweetapps.faceweight.core.ui.components.NumberPicker

/**
 * 3개의 세로 휠을 가로로 배치해 0..999 범위를 구성하는 숫자 선택기.
 * - value: 현재 값 (0..999)
 * - onValueChange: 휠 변경 시 즉시 콜백 (유효 범위 밖이면 내부에서 보정하지 않음)
 * - unitLabel: 우측에 표시할 단위 텍스트 (예: "일")
 */
@Composable
fun TripleDigitNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    unitLabel: String? = null
) {
    val clamped = value.coerceIn(0, 999)
    var hundreds by remember(clamped) { mutableIntStateOf(clamped / 100) }
    var tens by remember(clamped) { mutableIntStateOf((clamped / 10) % 10) }
    var ones by remember(clamped) { mutableIntStateOf(clamped % 10) }

    fun apply(h: Int = hundreds, t: Int = tens, o: Int = ones) {
        val combined = (h.coerceIn(0, 9) * 100) + (t.coerceIn(0, 9) * 10) + o.coerceIn(0, 9)
        onValueChange(combined)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NumberPicker(
            value = hundreds,
            onValueChange = { v -> hundreds = v; apply(h = v) },
            range = 0..9,
            modifier = Modifier.width(80.dp)
        )
        NumberPicker(
            value = tens,
            onValueChange = { v -> tens = v; apply(t = v) },
            range = 0..9,
            modifier = Modifier.width(80.dp)
        )
        NumberPicker(
            value = ones,
            onValueChange = { v -> ones = v; apply(o = v) },
            range = 0..9,
            modifier = Modifier.width(80.dp)
        )
        if (unitLabel != null) {
            Spacer(Modifier.width(4.dp))
            Text(unitLabel)
        }
    }
}
