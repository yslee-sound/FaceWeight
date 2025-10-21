package com.example.alcoholictimer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: String = "",
    displayValues: List<String> = range.map { it.toString() }
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = value - range.first)
    val coroutineScope = rememberCoroutineScope()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val itemHeight = 48.dp
    val visibleItemsCount = 5
    val visibleItemsMiddle = visibleItemsCount / 2

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val currentIndex = listState.firstVisibleItemIndex
            val currentValue = range.first + currentIndex
            if (currentValue != value && currentValue in range) {
                onValueChange(currentValue)
            }
        }
    }

    LaunchedEffect(value) {
        val targetIndex = value - range.first
        if (targetIndex != listState.firstVisibleItemIndex) {
            coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
        }
    }

    Box(
        // 고정 너비(100.dp)를 제거하여 호출부에서 modifier로 너비를 제어할 수 있게 함
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(itemHeight).background(
                Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp)
            )
        )
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * visibleItemsMiddle)
        ) {
            items(range.count()) { index ->
                val itemValue = range.first + index
                val displayValue = if (displayValues.isNotEmpty() && index < displayValues.size) {
                    displayValues[index]
                } else itemValue.toString()
                val isSelected = itemValue == value
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayValue,
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(if (isSelected) 1.0f else 0.6f)
                    )
                }
            }
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 20.dp)
            )
        }
    }
}
