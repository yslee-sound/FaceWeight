package com.sweetapps.faceweight.feature.start

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.os.Build
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sweetapps.faceweight.core.ui.AppElevation
import com.sweetapps.faceweight.core.ui.BaseActivity
import com.sweetapps.faceweight.core.ui.StandardScreenWithBottomButton
import com.sweetapps.faceweight.core.util.AppUpdateManager
import com.sweetapps.faceweight.core.util.Constants
import com.google.android.play.core.appupdate.AppUpdateInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.material3.SnackbarResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import com.sweetapps.faceweight.core.ui.components.AppUpdateDialog
import androidx.core.graphics.drawable.toDrawable
import com.sweetapps.faceweight.R
import android.graphics.Color as AndroidColor

class StartActivity : BaseActivity() {
    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // 런처 액티비티에서만 스플래시 설치
        val splash = installSplashScreen()
        // Android 12+ 시스템 스플래시 종료 연출: 220ms 페이드 + 약간 확대 후 제거
        if (Build.VERSION.SDK_INT >= 31) {
            splash.setOnExitAnimationListener { provider ->
                val icon = provider.iconView
                icon.animate()
                    .alpha(0f)
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(220)
                    .withEndAction { provider.remove() }
                    .start()
            }
        }
        // 스플래시 최소 표시 시간 (예: 800ms)
        val splashStart = SystemClock.uptimeMillis()
        val minShowMillis = 800L
        splash.setKeepOnScreenCondition { Build.VERSION.SDK_INT >= 31 && SystemClock.uptimeMillis() - splashStart < minShowMillis }

        super.onCreate(savedInstanceState)
        Constants.initializeUserSettings(this)
        Constants.ensureInstallMarkerAndResetIfReinstalled(this)

        // 드로어 내비게이션 시 스플래시 생략 플래그
        val skipSplash = intent.getBooleanExtra("skip_splash", false)

        // 상태바/내비게이션 바 라이트 아이콘 적용 및 표시
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.show(WindowInsetsCompat.Type.navigationBars())

        // In-App Update 초기화
        appUpdateManager = AppUpdateManager(this)

        // 디버그 빌드 여부 (릴리스에서 데모 비활성화용)
        val isDebugBuild = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // 데모 모드 플래그: DEBUG에서만 인텐트 값 반영, RELEASE에서는 항상 false
        val demoUpdateUi = if (isDebugBuild) intent.getBooleanExtra("demo_update_ui", false) else false

        val launchContent = {
            // 남은 최소 오버레이 시간 계산 (API<31에서 setContent 지연 후엔 0일 수 있음)
            val elapsed = SystemClock.uptimeMillis() - splashStart
            val initialRemain = (minShowMillis - elapsed).coerceAtLeast(0L)
            // API 31+에서는 시스템 아이콘을 투명으로 두고 Compose 오버레이로 아이콘을 그린다
            val usesComposeOverlay = Build.VERSION.SDK_INT >= 31
            setContent {
                // 상단 시스템바 패딩은 적용, 하단은 개별 레이아웃에서 처리
                BaseScreen(applyBottomInsets = false, applySystemBars = true) {
                    StartScreenWithUpdate(
                        appUpdateManager,
                        demoMode = demoUpdateUi,
                        debugEnabled = isDebugBuild,
                        initialMinRemainMillis = if (skipSplash) 0L else initialRemain,
                        usesComposeOverlay = usesComposeOverlay,
                        onSplashFinished = {
                            // 스플래시 오버레이 종료 시, 창 배경(스플래시 레이어)을 제거하여 잔상/깜빡임 방지
                            window.setBackgroundDrawable(null)
                        }
                    )
                }
            }
            // 오버레이를 쓰지 않는 내부 네비게이션의 경우, 첫 프레임 직후 배경을 제거
            if (skipSplash && Build.VERSION.SDK_INT < 31) {
                window.decorView.post { window.setBackgroundDrawable(null) }
            }
        }

        if (Build.VERSION.SDK_INT < 31) {
            // API 30 이하: 테마 스플래시 아이콘 → 즉시 화이트 배경으로 덮고 setContent, 첫 프레임 이후 배경 제거
            window.setBackgroundDrawable(AndroidColor.WHITE.toDrawable())
            launchContent()
            window.decorView.post { window.setBackgroundDrawable(null) }
        } else {
            // API 31 이상: 시스템 SplashScreen이 유지 조건으로 제어됨
            launchContent()
        }
    }

    override fun getScreenTitle(): String = getString(R.string.faceweight_title)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartScreenWithUpdate(
    appUpdateManager: AppUpdateManager,
    demoMode: Boolean = false,
    debugEnabled: Boolean = false,
    initialMinRemainMillis: Long = 0L,
    usesComposeOverlay: Boolean = true,
    onSplashFinished: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Composable 컨텍스트에서 미리 문자열 평가
    val restartPromptText = stringResource(R.string.update_downloaded_restart_prompt)
    val actionRestartText = stringResource(R.string.action_restart)

    // 최소 오버레이 유지 상태: 초기 남은 시간이 있으면 true로 시작, 남은 시간 후 false로 전환
    var keepMinOverlay by remember { mutableStateOf(initialMinRemainMillis > 0L) }
    LaunchedEffect(initialMinRemainMillis) {
        if (initialMinRemainMillis > 0L) {
            delay(initialMinRemainMillis)
            keepMinOverlay = false
        }
    }

    // 업데이트 다이얼로그/체크 상태
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(true) }

    // DEBUG에서만 데모 활성화
    val demoEnabled = debugEnabled
    val demoActive = demoEnabled && demoMode

    // 업데이트 정보/표시 버전명 상태 보관
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var availableVersionName by remember { mutableStateOf("") }

    // 데모 트리거 함수 (UI만 시연)
    val triggerDemo: () -> Unit = {
        isCheckingUpdate = true
        scope.launch {
            delay(600)
            // 데모용 표시 버전명 세팅
            availableVersionName = "2.0.0"
            // 다이얼로그 표시
            showUpdateDialog = true
            isCheckingUpdate = false
        }
    }

    // 앱 시작 시 업데이트 확인 (데모 모드면 실제 체크 생략)
    if (!demoActive) {
        LaunchedEffect(Unit) {
            scope.launch {
                appUpdateManager.checkForUpdate(
                    forceCheck = false,
                    onUpdateAvailable = { info ->
                        // 업데이트 사용 가능: 정보 보관 후 다이얼로그 표시
                        updateInfo = info
                        availableVersionName = "v${info.availableVersionCode()}"
                        showUpdateDialog = true
                        isCheckingUpdate = false
                    },
                    onNoUpdate = { isCheckingUpdate = false }
                )
            }
        }
    } else {
        LaunchedEffect(Unit) { triggerDemo() }
    }

    // 업데이트 다운로드 완료 리스너
    LaunchedEffect(Unit) {
        appUpdateManager.registerInstallStateListener {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = restartPromptText,
                    actionLabel = actionRestartText,
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) {
                    appUpdateManager.completeFlexibleUpdate()
                }
            }
        }
    }
    DisposableEffect(Unit) { onDispose { appUpdateManager.unregisterInstallStateListener() } }

    // 업데이트 중/다이얼로그 표시 중에는 Run 화면으로의 자동 이동을 보류
    val gateNavigation = isCheckingUpdate || showUpdateDialog

    Box(modifier = Modifier.fillMaxSize()) {
        StartScreen(
            gateNavigation = gateNavigation,
            onDebugLongPress = if (demoEnabled) ({ triggerDemo() }) else null
        )

        // 스플래시 오버레이: 최소 유지 시간이 남아있거나, 업데이트 체크 중인 동안 표시 (다이얼로그 표시 시에는 숨김)
        val showSplashOverlay = usesComposeOverlay && (keepMinOverlay || isCheckingUpdate) && !showUpdateDialog

        // 오버레이가 사라지는 시점에 한 번 콜백 호출(창 배경 제거 등)
        LaunchedEffect(showSplashOverlay) {
            if (!showSplashOverlay) {
                onSplashFinished()
            }
        }

        AnimatedVisibility(
            visible = showSplashOverlay,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(durationMillis = 220)) + scaleOut(targetScale = 1.02f, animationSpec = tween(220))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(240.dp)
                )
            }
        }

        // 스낵바
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        // 업데이트 다이얼로그 렌더링 (실사용/데모 공통)
        AppUpdateDialog(
            isVisible = showUpdateDialog,
            versionName = if (availableVersionName.isNotBlank()) availableVersionName else "vNext",
            onUpdateClick = {
                if (demoActive) {
                    // 데모: 다운로드 완료 스낵바를 직접 노출해 흐름 시연
                    showUpdateDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = restartPromptText,
                            actionLabel = actionRestartText,
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                } else {
                    // 실사용: Flexible Update 시작
                    updateInfo?.let { appUpdateManager.startFlexibleUpdate(it) }
                    showUpdateDialog = false
                }
            },
            onDismiss = {
                showUpdateDialog = false
                appUpdateManager.markUserPostpone()
            },
            canDismiss = !appUpdateManager.isMaxPostponeReached()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StartScreen(gateNavigation: Boolean = false, onDebugLongPress: (() -> Unit)? = null) {
    val context = LocalContext.current
    // 상단 안내 카드 제거(목표기간 UI 삭제)
    Box(modifier = Modifier.fillMaxSize()) {
        StandardScreenWithBottomButton(
            topContent = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD), // down from CARD_HIGH
                    border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.faceweight_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = colorResource(id = R.color.color_title_primary),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // FaceWeight quick entry card (visible on Start screen)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.CARD),
                    border = BorderStroke(1.dp, colorResource(id = R.color.color_border_light))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.faceweight_card_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.faceweight_card_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorResource(id = R.color.color_hint_gray),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, com.sweetapps.faceweight.feature.faceweight.FaceWeightEntryActivity::class.java).apply {
                                        putExtra("entry_action", "camera")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.faceweight_action_camera))
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, com.sweetapps.faceweight.feature.faceweight.FaceWeightEntryActivity::class.java).apply {
                                        putExtra("entry_action", "gallery")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.faceweight_action_gallery))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(context, com.sweetapps.faceweight.feature.faceweight.FaceWeightEntryActivity::class.java).apply {
                                    putExtra("entry_action", "camera")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = stringResource(id = R.string.faceweight_entry_button))
                        }
                    }
                }
            },
            bottomButton = { }
        )
    }
}
