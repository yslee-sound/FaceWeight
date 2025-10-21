package com.sweetapps.faceweight.feature.about

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sweetapps.faceweight.core.ui.BaseActivity
import com.sweetapps.faceweight.core.ui.LocalSafeContentPadding

class AboutActivity : BaseActivity() {
    override fun getScreenTitle(): String = getString(R.string.about_title)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BaseScreen { AboutListScreen(onOpenLicenses = { openLicenses() }) } }
    }

    @Suppress("DEPRECATION")
    private fun openLicenses() {
        startActivity(Intent(this, AboutLicensesActivity::class.java))
        overridePendingTransition(0, 0)
    }
}

@Composable
private fun AboutListScreen(onOpenLicenses: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            @Suppress("DEPRECATION")
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            pi.versionName ?: "-"
        } catch (_: Throwable) {
            "-"
        }
    }

    val safePadding = LocalSafeContentPadding.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(safePadding)
    ) {
        // 흰색 카드 안에 리스트 아이템 묶기
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1) 버전 정보 (정보 표시 행)
                SimpleListRow(
                    title = stringResource(id = R.string.about_version_info),
                    trailing = {
                        Text(
                            text = versionName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                // 2) 오픈 라이선스 고지 (탭 시 이동)
                SimpleListRow(
                    title = stringResource(id = R.string.about_open_license_notice),
                    onClick = onOpenLicenses
                )
            }
        }
        // 아래 여백
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SimpleListRow(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}
