@file:Suppress("unused")

package com.example.alcoholictimer.core.util

import android.content.Context
import androidx.core.content.edit
import java.io.File

object Constants {
    const val PREFS_NAME = "AlcoholicTimerPrefs"
    const val USER_SETTINGS_PREFS = "user_settings"
    const val PREF_KEY_TEST_MODE = "test_mode"
    const val PREF_TEST_MODE = "test_mode"
    const val PREF_START_TIME = "start_time"
    const val PREF_TARGET_DAYS = "target_days"
    const val PREF_RECORDS = "records"
    const val PREF_TIMER_COMPLETED = "timer_completed"
    const val PREF_SOBRIETY_RECORDS = "sobriety_records"

    const val PREF_SELECTED_COST = "selected_cost"
    const val PREF_SELECTED_FREQUENCY = "selected_frequency"
    const val PREF_SELECTED_DURATION = "selected_duration"
    const val PREF_SETTINGS_INITIALIZED = "settings_initialized"

    const val DEFAULT_COST = "저"
    const val DEFAULT_FREQUENCY = "주 1회 이하"
    const val DEFAULT_DURATION = "짧음"

    const val TEST_MODE_REAL = 0
    const val TEST_MODE_MINUTE = 1
    const val TEST_MODE_SECOND = 2

    var currentTestMode = TEST_MODE_REAL

    val isTestMode: Boolean get() = currentTestMode != TEST_MODE_REAL
    val isSecondTestMode: Boolean get() = currentTestMode == TEST_MODE_SECOND
    val isMinuteTestMode: Boolean get() = currentTestMode == TEST_MODE_MINUTE

    const val DAY_IN_MILLIS = 1000L * 60 * 60 * 24
    const val MINUTE_IN_MILLIS = 1000L * 60
    const val SECOND_IN_MILLIS = 1000L

    const val RESULT_SCREEN_DELAY = 2000
    const val DEFAULT_VALUE = 2000
    const val DEFAULT_HANGOVER_HOURS = 5

    val LEVEL_TIME_UNIT_MILLIS: Long get() = DAY_IN_MILLIS
    val LEVEL_TIME_UNIT_TEXT: String get() = "일"

    const val STATUS_COMPLETED = "완료"

    fun keyCurrentIndicator(startTime: Long): String = "current_indicator_${startTime}"

    fun calculateLevelDays(elapsedTimeMillis: Long): Int = (elapsedTimeMillis / DAY_IN_MILLIS).toInt()
    fun calculateLevelDaysFloat(elapsedTimeMillis: Long): Float = (elapsedTimeMillis / DAY_IN_MILLIS.toFloat())

    fun init(context: Context) { currentTestMode = TEST_MODE_REAL }

    fun updateTestMode(mode: Int) { currentTestMode = TEST_MODE_REAL }

    fun initializeUserSettings(context: Context) {
        val sharedPref = context.getSharedPreferences(USER_SETTINGS_PREFS, Context.MODE_PRIVATE)
        val isInitialized = sharedPref.getBoolean(PREF_SETTINGS_INITIALIZED, false)
        if (!isInitialized) {
            sharedPref.edit {
                putString(PREF_SELECTED_COST, DEFAULT_COST)
                putString(PREF_SELECTED_FREQUENCY, DEFAULT_FREQUENCY)
                putString(PREF_SELECTED_DURATION, DEFAULT_DURATION)
                // 첫 실행: 혹시 백업/잔존 데이터로 start_time, target_days 등이 남아있어도 초기화
                remove(PREF_START_TIME)
                remove(PREF_TARGET_DAYS)
                putBoolean(PREF_TIMER_COMPLETED, false)
                putBoolean(PREF_SETTINGS_INITIALIZED, true)
            }
        } else {
            // 보정 로직: 비정상 상태(예: start_time 존재하지만 target_days 미설정, 혹은 future timestamp) 정리
            val startTime = sharedPref.getLong(PREF_START_TIME, 0L)
            val targetDays = sharedPref.getFloat(PREF_TARGET_DAYS, -1f)
            val now = System.currentTimeMillis()
            if (startTime > 0 && (targetDays <= 0f || startTime > now)) {
                sharedPref.edit {
                    remove(PREF_START_TIME)
                    remove(PREF_TARGET_DAYS)
                    putBoolean(PREF_TIMER_COMPLETED, false)
                }
            }
        }
    }

    fun getUserSettings(context: Context): Triple<String, String, String> {
        val sharedPref = context.getSharedPreferences(USER_SETTINGS_PREFS, Context.MODE_PRIVATE)
        val cost = sharedPref.getString(PREF_SELECTED_COST, DEFAULT_COST) ?: DEFAULT_COST
        val frequency = sharedPref.getString(PREF_SELECTED_FREQUENCY, DEFAULT_FREQUENCY) ?: DEFAULT_FREQUENCY
        val duration = sharedPref.getString(PREF_SELECTED_DURATION, DEFAULT_DURATION) ?: DEFAULT_DURATION
        return Triple(cost, frequency, duration)
    }

    private const val INSTALL_MARKER_NAME = "install_marker_v1"
    private const val FRESH_INSTALL_WINDOW_MILLIS = 60 * 60 * 1000L // 1시간 내 설치면 재설치로 간주

    fun ensureInstallMarkerAndResetIfReinstalled(context: Context) {
        val markerFile = File(context.noBackupFilesDir, INSTALL_MARKER_NAME)
        if (markerFile.exists()) return // 이미 한번 처리 끝

        val pm = context.packageManager
        val firstInstallTime = try {
            pm.getPackageInfo(context.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        val isRecentInstall = (System.currentTimeMillis() - firstInstallTime) < FRESH_INSTALL_WINDOW_MILLIS

        val sharedPref = context.getSharedPreferences(USER_SETTINGS_PREFS, Context.MODE_PRIVATE)
        if (isRecentInstall) {
            // 재설치 직후 복원된 진행 상태라면 모두 초기화
            sharedPref.edit {
                remove(PREF_START_TIME)
                remove(PREF_TARGET_DAYS)
                putBoolean(PREF_TIMER_COMPLETED, false)
            }
        }
        // marker 생성 (업데이트 첫 실행 시에도 생성되지만 wipe는 하지 않음)
        try { markerFile.writeText("1") } catch (_: Exception) { /* ignore */ }
    }
}
