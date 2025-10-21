package com.example.alcoholictimer.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties

// 앱 테마
import com.example.alcoholictimer.core.ui.theme.AlcoholicTimerTheme

/**
 * 앱 업데이트 다이얼로그
 * Flexible Update 방식으로 사용자에게 업데이트를 안내합니다.
 */
@Composable
fun AppUpdateDialog(
    isVisible: Boolean,
    versionName: String,
    updateMessage: String = "새로운 기능과 개선사항이 포함되어 있습니다.",
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
    canDismiss: Boolean = true
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = {
            if (canDismiss) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "업데이트",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 제목
                Text(
                    text = "새 버전이 있습니다! 🎉",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 버전 정보
                Text(
                    text = "버전 $versionName",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 업데이트 내용
                Text(
                    text = updateMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 항상 노출, 필요 시 비활성화
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = canDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("나중에")
                    }

                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("업데이트")
                    }
                }
            }
        }
    }
}

/**
 * 업데이트 다운로드 완료 스낵바
 */
@Composable
fun UpdateDownloadedSnackbar(
    snackbarHostState: SnackbarHostState,
    onInstallClick: () -> Unit
) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = onInstallClick) {
                        Text("다시 시작")
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(data.visuals.message)
            }
        }
    )
}

// === Previews ===

@Preview(name = "AppUpdateDialog - Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "AppUpdateDialog - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_AppUpdateDialog_Default() {
    AlcoholicTimerTheme(applySystemBars = false) {
        AppUpdateDialog(
            isVisible = true,
            versionName = "1.2.3",
            updateMessage = "- 버그 수정\n- 성능 개선\n- 신규 디자인 적용",
            onUpdateClick = {},
            onDismiss = {},
            canDismiss = true
        )
    }
}

@Preview(name = "AppUpdateDialog - 강제 업데이트", showBackground = true)
@Composable
private fun Preview_AppUpdateDialog_Force() {
    AlcoholicTimerTheme(applySystemBars = false) {
        AppUpdateDialog(
            isVisible = true,
            versionName = "2.0.0",
            updateMessage = "보안 강화를 위한 필수 업데이트입니다.",
            onUpdateClick = {},
            onDismiss = {},
            canDismiss = false
        )
    }
}

@Preview(name = "UpdateDownloadedSnackbar - Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "UpdateDownloadedSnackbar - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_UpdateDownloadedSnackbar() {
    AlcoholicTimerTheme(applySystemBars = false) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "업데이트가 다운로드되었습니다. 다시 시작하여 설치하세요.",
                withDismissAction = true
            )
        }
        Scaffold(
            snackbarHost = {
                UpdateDownloadedSnackbar(
                    snackbarHostState = snackbarHostState,
                    onInstallClick = {}
                )
            }
        ) { inner ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(inner))
        }
    }
}
