package com.sweetapps.faceweight.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sweetapps.faceweight.R

/**
 * 공통 카드 래퍼: elevation/shape/패딩/기본 색상을 디자인 토큰과 동기화.
 * onClick 이 null 이면 단순한 카드, 아니면 clickable 카드.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    elevation: Dp = AppElevation.CARD, // default lowered from CARD_HIGH
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)
    val resolvedBorder = border ?: BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            border = resolvedBorder
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = modifier.clickable { onClick() },
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            border = resolvedBorder
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}
