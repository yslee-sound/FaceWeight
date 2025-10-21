package com.example.alcoholictimer.feature.records

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.alcoholictimer.core.ui.BaseActivity
import com.example.alcoholictimer.core.model.SobrietyRecord
import com.example.alcoholictimer.feature.detail.DetailActivity
import com.example.alcoholictimer.feature.records.components.RecordsScreen

class RecordsActivity : BaseActivity() {

    companion object {
        private const val TAG = "RecordsActivity"
    }

    private var refreshTrigger by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = density.fontScale * 0.9f)) {
                BaseScreen {
                    RecordsScreen(
                        externalRefreshTrigger = refreshTrigger,
                        onNavigateToDetail = { record -> handleCardClick(record) },
                        onNavigateToAllRecords = { navigateToAllRecords() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: 기록 화면이 다시 나타남 - 데이터 새로고침")
        refreshTrigger++
    }

    override fun getScreenTitle(): String = "금주 기록"

    private fun handleCardClick(record: SobrietyRecord) {
        Log.d(TAG, "===== 카드 클릭 시작 =====")
        Log.d(TAG, "카드 클릭: ${record.id}")
        Log.d(TAG, "actualDays=${record.actualDays}, targetDays=${record.targetDays}")
        Log.d(TAG, "startTime=${record.startTime}, endTime=${record.endTime}")
        Log.d(TAG, "isCompleted=${record.isCompleted}")

        try {
            if (record.actualDays < 0) {
                Log.e(TAG, "잘못된 기록 데이터: actualDays=${record.actualDays}")
                return
            }

            val safeTargetDays = if (record.targetDays <= 0) 30 else record.targetDays

            val intent = Intent(this@RecordsActivity, DetailActivity::class.java)
            intent.putExtra("start_time", record.startTime)
            intent.putExtra("end_time", record.endTime)
            intent.putExtra("target_days", safeTargetDays.toFloat())
            intent.putExtra("actual_days", record.actualDays)
            intent.putExtra("is_completed", record.isCompleted)

            Log.d(TAG, "Intent 데이터 전달: targetDays=$safeTargetDays, actualDays=${record.actualDays}")
            Log.d(TAG, "DetailActivity 호출...")
            startActivity(intent)
            Log.d(TAG, "startActivity 호출 완료")
            Log.d(TAG, "===== 카드 클릭 종료 =====")
        } catch (e: Exception) {
            Log.e(TAG, "CardDetail 화면 이동 중 오류", e)
            Log.e(TAG, "오류 스택트레이스: ${e.stackTraceToString()}")
        }
    }

    private fun navigateToAllRecords() {
        Log.d(TAG, "모든 기록 화면으로 이동")
        val intent = Intent(this@RecordsActivity, AllRecordsActivity::class.java)
        startActivity(intent)
    }
}
