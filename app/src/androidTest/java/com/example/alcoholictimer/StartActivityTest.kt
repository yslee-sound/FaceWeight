package com.example.alcoholictimer

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.alcoholictimer.feature.start.StartActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Q01: 첫 실행 빈(초기) 상태 시나리오 자동화 초안.
 * 실제 앱은 "빈 목록" 안내 대신 최초 설정(StartActivity) 화면을 띄우므로
 * QA_CHECKLIST.md의 Q01 기대 결과를 현재 UX에 맞게 해석: 목표 기간 설정 카드 렌더링 확인.
 */
class StartActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<StartActivity>()

    @Before
    fun resetSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun firstLaunch_showsTargetPeriodSetupCard() {
        // 핵심 텍스트 요소들이 화면에 표시되는지 확인
        composeRule.onNodeWithText("목표 기간 설정").assertIsDisplayed()
        composeRule.onNodeWithText("금주할 목표 기간을 입력해주세요").assertIsDisplayed()
        // 기본값 30이 노출되는지(입력 필드)
        composeRule.onNodeWithText("30").assertIsDisplayed()
    }
}

