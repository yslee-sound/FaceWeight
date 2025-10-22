package com.sweetapps.faceweight.feature.faceweight

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sweetapps.faceweight.R
import com.sweetapps.faceweight.core.ui.BaseActivity
import com.sweetapps.faceweight.core.ui.LocalSafeContentPadding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.math.abs
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

class FaceWeightEntryActivity : BaseActivity() {
    override fun getScreenTitle(): String = getString(com.sweetapps.faceweight.R.string.faceweight_title)

    // CameraX
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: Executor
    private var isCameraPermissionGranted = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isCameraPermissionGranted = granted
        if (granted) recreate() else Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = ContextCompat.getMainExecutor(this)

        val entryAction = intent?.getStringExtra("entry_action")
        val imageUri = intent?.getStringExtra("image_uri")

        // 권한 확인 (카메라 UI가 기본 표시되므로 선체크)
        isCameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!isCameraPermissionGranted && imageUri.isNullOrBlank()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            // return 제거: 즉시 UI를 그리되, 권한 승인 후 recreate()로 카메라 시작
        }

        setContent {
            BaseScreen {
                // imageUri 또는 gallery로 진입 시 기존 결과 화면 유지, 그 외에는 카메라 메인 화면
                if (!imageUri.isNullOrBlank() || entryAction == "gallery" || entryAction == "camera_result") {
                    FaceWeightResultScreen(entryAction, imageUri)
                } else {
                    FaceWeightCameraScreen()
                }
            }
        }
    }

    private fun requestCameraPermissionAgain() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    @Composable
    private fun FaceWeightCameraScreen() {
        val context = LocalContext.current
        var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
        var isFront by remember { mutableStateOf(true) }
        var qualityTab by remember { mutableStateOf(0) }
        val hasPermission = isCameraPermissionGranted

        // 카메라 시작/재시작 (권한 있을 때만)
        LaunchedEffect(previewViewRef, isFront, hasPermission) {
            if (!hasPermission) return@LaunchedEffect
            val pv = previewViewRef ?: return@LaunchedEffect
            startCamera(pv, isFront)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 바: 좌 알림, 중앙 3개 선택, 우 카메라 전환
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { Toast.makeText(context, "알림: 준비 중", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                ) { Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White) }

                TopQualitySelector(
                    selected = qualityTab,
                    onSelect = { qualityTab = it },
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = { isFront = !isFront },
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterEnd)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                ) { Icon(Icons.Filled.FlipCameraAndroid, contentDescription = null, tint = Color.White) }
            }

            // 중단: 권한 여부에 따라 프리뷰 or 안내
            if (hasPermission) {
                AndroidView(factory = { ctx ->
                    PreviewView(ctx).also { previewViewRef = it }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("카메라 권한이 필요합니다.", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { requestCameraPermissionAgain() }) {
                            Text("권한 다시 요청")
                        }
                    }
                }
            }

            // 하단: 5개 버튼(가운데 셔터)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomItem(Icons.Filled.Tune, "보정") { toast(context, "보정: 준비 중") }
                BottomItem(Icons.Filled.AutoAwesome, "이펙트") { toast(context, "이펙트: 준비 중") }

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { takePhotoAndNavigate() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    )
                }

                BottomItem(Icons.Filled.FaceRetouchingNatural, "뷰티") { toast(context, "뷰티: 준비 중") }
                BottomItem(Icons.Filled.FilterAlt, "필터") { toast(context, "필터: 준비 중") }
            }
        }
    }

    private fun startCamera(previewView: PreviewView, isFront: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            val selector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, selector, preview, imageCapture)
            } catch (_: Exception) {
                Toast.makeText(this, "카메라 초기화 실패", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndNavigate() {
        val capture = imageCapture ?: run { Toast.makeText(this, "카메라 준비 중", Toast.LENGTH_SHORT).show(); return }
        val photoFile = createImageFile()
        val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        capture.takePicture(output, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri: Uri = FileProvider.getUriForFile(this@FaceWeightEntryActivity, "${applicationContext.packageName}.fileprovider", photoFile)
                val intent = Intent(this@FaceWeightEntryActivity, FaceWeightEntryActivity::class.java).apply {
                    putExtra("entry_action", "camera_result")
                    putExtra("image_uri", savedUri.toString())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
                finish()
            }
            override fun onError(exception: ImageCaptureException) {
                runOnUiThread { Toast.makeText(this@FaceWeightEntryActivity, "사진 저장 실패: ${exception.message}", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun createImageFile(): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val filename = "FW_IMG_${sdf.format(Date())}.jpg"
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (dir != null && !dir.exists()) dir.mkdirs()
        return File(dir, filename)
    }
}

@Composable
private fun FaceWeightResultScreen(entryAction: String?, imageUri: String?) {
    val safePadding = LocalSafeContentPadding.current
    val context = LocalContext.current
    val actionLabel = when (entryAction) {
        "camera" -> context.getString(R.string.faceweight_entry_camera_selected)
        "gallery" -> context.getString(R.string.faceweight_entry_gallery_selected)
        "camera_result" -> context.getString(R.string.faceweight_entry_camera_selected)
        else -> context.getString(R.string.faceweight_entry_none_selected)
    }

    // 기존 결과 UI 유지
    val offsets = listOf(-10, -5, 0, 5, 10)
    var selectedIndex by remember { mutableStateOf(2) }

    val (sampleName, baseWeight, similarity) = remember(imageUri) {
        if (!imageUri.isNullOrBlank()) {
            val h = abs(imageUri.hashCode())
            val name = "샘플 #" + ((h % 100) + 1)
            val weight = 50 + (h % 36)
            val sim = 60 + ((h / 7) % 36)
            Triple(name, weight, sim)
        } else {
            Triple("샘플 #001", 65, 85)
        }
    }

    val selectedOffset = offsets[selectedIndex]
    val simulatedWeight = baseWeight + selectedOffset
    val scaleTarget = remember(selectedOffset) { 1f + (selectedOffset.toFloat() * 0.006f) }
    val animatedScale by animateFloatAsState(targetValue = scaleTarget)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(safePadding),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = context.getString(R.string.faceweight_title) + " (베타)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = context.getString(R.string.faceweight_card_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = actionLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier
                .weight(1f)
                .height(260.dp)
                .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            ) {
                if (!imageUri.isNullOrBlank()) {
                    AndroidView(factory = { ctx ->
                        ImageView(ctx).apply {
                            adjustViewBounds = true
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setImageURI(Uri.parse(imageUri))
                        }
                    }, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "이미지 없음", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Surface(modifier = Modifier.width(160.dp).height(260.dp), shape = MaterialTheme.shapes.medium, tonalElevation = 0.dp) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = context.getString(R.string.faceweight_matched_label), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!imageUri.isNullOrBlank()) {
                        AndroidView(factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                setImageURI(Uri.parse(imageUri))
                            }
                        }, modifier = Modifier.fillMaxWidth().height(100.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = sampleName, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = String.format(context.getString(R.string.faceweight_similarity_format), similarity), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = context.getString(R.string.faceweight_simulated_label), style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "%d kg".format(simulatedWeight), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            offsets.forEachIndexed { idx, off ->
                val label = when (off) {
                    -10 -> context.getString(R.string.faceweight_simulate_minus10)
                    -5 -> context.getString(R.string.faceweight_simulate_minus5)
                    0 -> context.getString(R.string.faceweight_simulate_original)
                    5 -> context.getString(R.string.faceweight_simulate_plus5)
                    10 -> context.getString(R.string.faceweight_simulate_plus10)
                    else -> "$off"
                }
                val isSelected = idx == selectedIndex
                Button(onClick = { selectedIndex = idx }, colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                    Text(text = label, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = context.getString(R.string.faceweight_simulate_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TopQualitySelector(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.25f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            QualityChip("기본", selected == 0) { onSelect(0) }
            QualityChip("고화질", selected == 1) { onSelect(1) }
            QualityChip("아이폰", selected == 2) { onSelect(2) }
        }
    }
}

@Composable
private fun QualityChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color.White else Color.Transparent,
        contentColor = if (selected) Color.Black else Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BottomItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = label, tint = Color.White) }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

private fun toast(ctx: Context, msg: String) { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() }
