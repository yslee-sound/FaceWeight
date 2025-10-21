package com.sweetapps.faceweight.feature.records.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sweetapps.faceweight.core.data.RecordsDataLoader
import com.sweetapps.faceweight.core.model.SobrietyRecord
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import com.sweetapps.faceweight.core.ui.LocalSafeContentPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRecordsScreen(
    externalRefreshTrigger: Int = 0,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (SobrietyRecord) -> Unit = {},
    fontScale: Float = 1.06f,
    externalDeleteDialog: MutableState<Boolean>? = null
) {
    val context = LocalContext.current
    var records by remember { mutableStateOf<List<SobrietyRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableIntStateOf(0) }
    // 외부에서 제어 가능한 삭제 다이얼로그 상태(없으면 내부에서 생성)
    val ownDialog = remember { mutableStateOf(false) }
    val dialogState = externalDeleteDialog ?: ownDialog

    val loadRecords: () -> Unit = remember {
        {
            isLoading = true
            loadError = null
            try {
                val loadedRecords = RecordsDataLoader.loadSobrietyRecords(context)
                records = loadedRecords.sortedByDescending { it.startTime }
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                loadError = e.message ?: "unknown"
            }
        }
    }

    LaunchedEffect(retryTrigger) { loadRecords() }
    LaunchedEffect(externalRefreshTrigger) { loadRecords() }

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = LocalDensity.current.fontScale * fontScale
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 외부 액션버튼(상단 앱바)이 제공되지 않는 경우에만 내부 X 버튼 노출
            if (externalDeleteDialog == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { dialogState.value = true },
                        enabled = !isLoading && records.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(id = R.string.cd_delete_all_records),
                            tint = if (!isLoading && records.isNotEmpty()) Color(0xFFE53E3E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                }
                loadError != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.error_loading_records),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { retryTrigger++ }) { Text(text = stringResource(id = R.string.retry)) }
                    }
                }
                records.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { AllRecordsEmptyState() }
                }
                else -> {
                    val safePadding = LocalSafeContentPadding.current
                    val bottomPadding = safePadding.calculateBottomPadding() + 12.dp

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = bottomPadding)
                    ) {
                        items(items = records, key = { it.id }) { record ->
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                RecordSummaryCard(
                                    record = record,
                                    onClick = { onNavigateToDetail(record) },
                                    compact = false,
                                    headerIconSizeDp = 56.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (dialogState.value) {
            AlertDialog(
                onDismissRequest = { dialogState.value = false },
                title = {
                    Text(
                        text = stringResource(id = R.string.all_records_delete_title),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                },
                text = { Text(text = stringResource(id = R.string.all_records_delete_message), color = Color(0xFF4A5568)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogState.value = false
                            val success = RecordsDataLoader.clearAllRecords(context)
                            if (success) onNavigateBack()
                        }
                    ) { Text(stringResource(id = R.string.dialog_delete_confirm)) }
                },
                dismissButton = { TextButton(onClick = { dialogState.value = false }) { Text(stringResource(id = R.string.dialog_cancel)) } }
            )
        }
    }
}

@Composable
fun AllRecordsEmptyState() {
    val emptyCd = stringResource(id = R.string.empty_records_cd)
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp).semantics { contentDescription = emptyCd },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📝", fontSize = 48.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.empty_records_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.empty_records_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllRecordsScreenPreview() { AllRecordsScreen() }
