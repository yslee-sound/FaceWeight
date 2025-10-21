package com.example.alcoholictimer.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.MaterialTheme

private val MaxContentWidth: Dp = 600.dp

@Composable
fun StandardScreen(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // 기존 Color.White -> 전역 배경 연회색으로 변경
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(LayoutConstants.SCREEN_HORIZONTAL_PADDING)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(LayoutConstants.CARD_SPACING),
        content = content
    )
}

@Composable
fun StandardScreenWithBottomButton(
    topContent: @Composable ColumnScope.() -> Unit,
    bottomButton: @Composable () -> Unit,
    imePaddingEnabled: Boolean = true,
    // 새로 추가: 배경 위/콘텐츠 아래에 깔릴 장식 레이어
    backgroundDecoration: @Composable BoxScope.() -> Unit = {}
) {
    val rootModifier = Modifier
        .fillMaxSize()
        // 기존 Color.White -> 전역 배경 연회색으로 변경
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .then(if (imePaddingEnabled) Modifier.imePadding() else Modifier)

    val navBarPaddingValues = WindowInsets.navigationBars.asPaddingValues()
    val navBarBottom = navBarPaddingValues.calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val effectiveBottom = if (navBarBottom > imeBottom) navBarBottom else imeBottom

    val buttonSize = 96.dp
    val extraGap = 32.dp
    val reservedBottom = (buttonSize / 2) + extraGap + effectiveBottom

    Box(
        modifier = rootModifier
    ) {
        // 배경 장식 레이어(워터마크 등)
        backgroundDecoration()

        // Centered column with max width constraint
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = LayoutConstants.SCREEN_HORIZONTAL_PADDING,
                    end = LayoutConstants.SCREEN_HORIZONTAL_PADDING,
                    top = LayoutConstants.SCREEN_HORIZONTAL_PADDING,
                    bottom = reservedBottom
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LayoutConstants.CARD_SPACING)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = MaxContentWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LayoutConstants.CARD_SPACING),
                content = topContent
            )
        }

        // 버튼: 시스템 바 높이 또는 IME 높이 + 적당한 여백(24dp) 적용
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // 수평 패딩과 하단 패딩을 분리 적용하여 타입 혼동 방지
                .padding(horizontal = LayoutConstants.BOTTOM_BUTTON_HORIZONTAL_PADDING)
                .padding(bottom = effectiveBottom + 24.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = MaxContentWidth),
            contentAlignment = Alignment.Center
        ) {
            bottomButton()
        }
    }
}
