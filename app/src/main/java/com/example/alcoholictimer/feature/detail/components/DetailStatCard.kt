package com.example.alcoholictimer.feature.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alcoholictimer.core.ui.AppCard
import com.example.alcoholictimer.core.ui.AppElevation

@Composable
fun DetailStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified
) {
    AppCard(
        modifier = modifier,
        elevation = AppElevation.CARD, // lowered from CARD_HIGH
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        val resolvedValueColor = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        Text(
            text = value,
            color = resolvedValueColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
        )
    }
}
