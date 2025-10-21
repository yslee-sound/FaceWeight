package com.sweetapps.faceweight.feature.level

import androidx.compose.ui.graphics.Color

/**
 * 금주 레벨 정의를 위한 공통 객체
 */
object LevelDefinitions {
    data class LevelInfo(val name: String, val start: Int, val end: Int, val color: Color)

    val levels = listOf(
        LevelInfo("작심 7일", 0, 6, Color(0xFF4FC3F7)),      // 연한 하늘색
        LevelInfo("의지의 2주", 7, 13, Color(0xFF00ACC1)),    // 청록색
        LevelInfo("한달의 기적", 14, 29, Color(0xFF81C784)),   // 연두색
        LevelInfo("습관의 탄생", 30, 59, Color(0xFF43A047)),   // 밝은 초록
        LevelInfo("계속되는 도전", 60, 119, Color(0xFFFDD835)), // 노랑
        LevelInfo("거의 1년", 120, 239, Color(0xFFFB8C00)),   // 주황
        LevelInfo("금주 마스터", 240, 364, Color(0xFFE53935)), // 빨강
        LevelInfo("절제의 레전드", 365, Int.MAX_VALUE, Color(0xFF8E24AA)) // 보라
    )

    fun getLevelName(days: Int): String {
        return levels.firstOrNull { days in it.start..it.end }?.name ?: levels.first().name
    }

    fun getLevelInfo(days: Int): LevelInfo {
        return levels.firstOrNull { days in it.start..it.end } ?: levels.first()
    }
}

