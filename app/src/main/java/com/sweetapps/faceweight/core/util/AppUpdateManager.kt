package com.sweetapps.faceweight.core.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.InstallStateUpdatedListener
import kotlinx.coroutines.tasks.await

/**
 * In-App Update 관리 클래스
 *
 * Google Play의 In-App Update API를 사용하여 앱 업데이트를 관리합니다.
 * - Flexible Update: 사용자가 선택 가능한 업데이트 (기본)
 * - Immediate Update: 중요 업데이트 시 강제 업데이트
 */
class AppUpdateManager(private val activity: ComponentActivity) {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(activity.applicationContext)
    }

    private var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private var installStateListener: InstallStateUpdatedListener? = null

    companion object {
        private const val TAG = "AppUpdateManager"
        private const val UPDATE_CHECK_PREFS = "app_update_prefs"
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check_time"
        private const val KEY_UPDATE_POSTPONED_COUNT = "update_postponed_count"
        private const val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24시간
        private const val MAX_POSTPONE_COUNT = 3 // 최대 3번까지 연기 가능
        private const val REQUEST_CODE_FLEXIBLE_UPDATE = 1001
        private const val REQUEST_CODE_IMMEDIATE_UPDATE = 1002
    }

    /**
     * Activity에서 onCreate()에서 호출하여 업데이트 결과를 처리할 launcher를 등록합니다.
     */
    fun registerUpdateLauncher(onUpdateSuccess: () -> Unit = {}, onUpdateFailed: () -> Unit = {}) {
        updateResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "업데이트 승인됨")
                    onUpdateSuccess()
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "업데이트 취소됨")
                    incrementPostponeCount()
                    onUpdateFailed()
                }
                else -> {
                    Log.d(TAG, "업데이트 실패: ${result.resultCode}")
                    onUpdateFailed()
                }
            }
        }
    }

    /** Play Store(com.android.vending) 존재 여부 */
    private fun hasPlayStore(context: Context = activity): Boolean {
        val pm = context.packageManager
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo("com.android.vending", PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo("com.android.vending", 0)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 앱 업데이트 확인 및 실행
     *
     * @param forceCheck 강제로 확인 (시간 제한 무시)
     * @param onUpdateAvailable 업데이트가 있을 때 콜백
     * @param onNoUpdate 업데이트가 없을 때 콜백
     */
    suspend fun checkForUpdate(
        forceCheck: Boolean = false,
        onUpdateAvailable: (AppUpdateInfo) -> Unit = {},
        onNoUpdate: () -> Unit = {}
    ) {
        try {
            // Play Store 없는 기기에서는 In-App Update 비대상 → 즉시 스킵
            if (!hasPlayStore()) {
                Log.w(TAG, "Play Store not found. Skipping in-app update check.")
                onNoUpdate()
                return
            }

            // 시간 제한 확인 (24시간마다 1회)
            if (!forceCheck && !shouldCheckForUpdate()) {
                Log.d(TAG, "업데이트 확인 시간이 아직 안 됨")
                onNoUpdate()
                return
            }

            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()

            // 마지막 확인 시간 업데이트
            updateLastCheckTime()

            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                    Log.d(TAG, "업데이트 사용 가능: 버전 ${appUpdateInfo.availableVersionCode()}")
                    onUpdateAvailable(appUpdateInfo)
                }
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    Log.d(TAG, "업데이트 진행 중")
                    // Immediate Update가 중단되었을 경우 재개
                    startImmediateUpdate(appUpdateInfo)
                }
                else -> {
                    Log.d(TAG, "업데이트 없음")
                    onNoUpdate()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "업데이트 확인 실패 또는 Play Core 미가용. Degrade to no update.", e)
            onNoUpdate()
        }
    }

    /**
     * Flexible Update 시작
     * 사용자가 선택할 수 있으며, 백그라운드에서 다운로드됩니다.
     */
    fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    updateOptions,
                    REQUEST_CODE_FLEXIBLE_UPDATE
                )
                Log.d(TAG, "Flexible Update 시작")
            } else {
                Log.w(TAG, "Flexible Update가 허용되지 않음")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Flexible Update 시작 실패", e)
        }
    }

    /**
     * Immediate Update 시작
     * 업데이트가 완료될 때까지 앱 사용이 차단됩니다.
     */
    fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activity,
                    updateOptions,
                    REQUEST_CODE_IMMEDIATE_UPDATE
                )
                Log.d(TAG, "Immediate Update 시작")
            } else {
                Log.w(TAG, "Immediate Update가 허용되지 않음")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Immediate Update 시작 실패", e)
        }
    }

    /**
     * Flexible Update 다운로드 완료 시 설치 완료
     */
    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Flexible Update 설치 상태 리스너 등록
     */
    fun registerInstallStateListener(onDownloaded: () -> Unit) {
        val listener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    Log.d(TAG, "업데이트 다운로드 완료")
                    onDownloaded()
                }
                InstallStatus.DOWNLOADING -> {
                    val total = state.totalBytesToDownload()
                    val downloaded = state.bytesDownloaded()
                    if (total > 0) {
                        val progress = downloaded * 100 / total
                        Log.d(TAG, "다운로드 중: ${progress}% (${downloaded}/${total})")
                    } else {
                        Log.d(TAG, "다운로드 중: ${downloaded} bytes")
                    }
                }
                InstallStatus.FAILED -> {
                    Log.e(TAG, "업데이트 다운로드 실패: ${state.installErrorCode()}")
                }
                else -> {
                    Log.d(TAG, "설치 상태: ${state.installStatus()}")
                }
            }
        }
        installStateListener = listener
        appUpdateManager.registerListener(listener)
    }

    /**
     * 설치 상태 리스너 등록 해제
     */
    fun unregisterInstallStateListener() {
        installStateListener?.let { appUpdateManager.unregisterListener(it) }
        installStateListener = null
    }

    /**
     * 업데이트를 확인해야 하는지 판단 (24시간 제한)
     */
    private fun shouldCheckForUpdate(): Boolean {
        val prefs = activity.getSharedPreferences(UPDATE_CHECK_PREFS, Context.MODE_PRIVATE)
        val lastCheckTime = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastCheckTime) >= UPDATE_CHECK_INTERVAL
    }

    /**
     * 마지막 업데이트 확인 시간 저장
     */
    private fun updateLastCheckTime() {
        val prefs = activity.getSharedPreferences(UPDATE_CHECK_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_UPDATE_CHECK, System.currentTimeMillis()).apply()
    }

    /**
     * 업데이트 연기 횟수 증가
     */
    private fun incrementPostponeCount() {
        val prefs = activity.getSharedPreferences(UPDATE_CHECK_PREFS, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_UPDATE_POSTPONED_COUNT, 0)
        prefs.edit().putInt(KEY_UPDATE_POSTPONED_COUNT, count + 1).apply()
    }

    /**
     * 사용자가 업데이트를 미룬 경우(다이얼로그 닫기 등) 연기 횟수를 증가시킵니다.
     * Flexible/Immediate 업데이트 플로우 외부(UI)에서 호출합니다.
     */
    fun markUserPostpone() {
        incrementPostponeCount()
    }

    /**
     * 업데이트 연기 횟수 가져오기
     */
    fun getPostponeCount(): Int {
        val prefs = activity.getSharedPreferences(UPDATE_CHECK_PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_UPDATE_POSTPONED_COUNT, 0)
    }

    /**
     * 연기 횟수 초과 여부 확인
     */
    fun isMaxPostponeReached(): Boolean {
        return getPostponeCount() >= MAX_POSTPONE_COUNT
    }

    /**
     * 연기 횟수 초기화
     */
    fun resetPostponeCount() {
        val prefs = activity.getSharedPreferences(UPDATE_CHECK_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_UPDATE_POSTPONED_COUNT, 0).apply()
    }
}
