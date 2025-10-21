package com.example.alcoholictimer.feature.records

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.alcoholictimer.core.model.SobrietyRecord
import com.example.alcoholictimer.feature.detail.DetailActivity
import com.example.alcoholictimer.core.ui.BaseActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class AllRecordsActivity : BaseActivity() {

    companion object { private const val TAG = "AllRecordsActivity" }

    // Compose 위임 대신 일반 Int 상태로 관리 (빌드 에러 회피)
    private var externalRefreshTriggerState: Int = 0

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "DetailActivity RESULT_OK 수신 → 리스트 새로고침 트리거 증가")
            externalRefreshTriggerState = externalRefreshTriggerState + 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showDeleteAll = remember { mutableStateOf(false) }
            BaseScreen(showBackButton = true, onBackClick = { finish() }, topBarActions = {
                IconButton(onClick = { showDeleteAll.value = true }) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "모든 기록 삭제")
                }
            }) {
                com.example.alcoholictimer.feature.records.components.AllRecordsScreen(
                    externalRefreshTrigger = externalRefreshTriggerState,
                    onNavigateBack = { finish() },
                    onNavigateToDetail = { record -> handleRecordClick(record) },
                    externalDeleteDialog = showDeleteAll
                )
            }
        }
    }

    override fun getScreenTitle(): String = "모든 기록"

    private fun handleRecordClick(record: SobrietyRecord) {
        Log.d(TAG, "===== 기록 클릭 시작 =====")
        Log.d(TAG, "기록 클릭: ${record.id}")
        Log.d(TAG, "actualDays=${record.actualDays}, targetDays=${record.targetDays}")

        try {
            if (record.actualDays < 0) {
                Log.e(TAG, "잘못된 기록 데이터: actualDays=${record.actualDays}")
                return
            }
            val safeTargetDays = if (record.targetDays <= 0) 30 else record.targetDays
            val intent = Intent(this@AllRecordsActivity, DetailActivity::class.java).apply {
                putExtra("start_time", record.startTime)
                putExtra("end_time", record.endTime)
                putExtra("target_days", safeTargetDays.toFloat())
                putExtra("actual_days", record.actualDays)
                putExtra("is_completed", record.isCompleted)
            }
            Log.d(TAG, "DetailActivity 호출(결과 대기)...")
            detailLauncher.launch(intent)
            Log.d(TAG, "===== 기록 클릭 종료 =====")
        } catch (e: Exception) {
            Log.e(TAG, "DetailActivity 화면 이동 중 오류", e)
        }
    }
}
