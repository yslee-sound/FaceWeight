package com.sweetapps.faceweight.feature.faceweight

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sweetapps.faceweight.core.ui.BaseActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

class FaceWeightCaptureActivity : BaseActivity() {
    override fun getScreenTitle(): String = getString(com.sweetapps.faceweight.R.string.faceweight_title)

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: Executor

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            recreate() // restart to setup camera preview
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = ContextCompat.getMainExecutor(this)

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        setContent { CameraCaptureScreen() }
    }

    @Composable
    private fun CameraCaptureScreen() {
        val context = LocalContext.current
        var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

        // 상태: 렌즈 전환, 품질 탭
        var isFront by remember { mutableStateOf(true) }
        var qualityTab by remember { mutableStateOf(0) } // 0:기본,1:고화질,2:아이폰

        // 카메라 시작/재시작
        LaunchedEffect(previewViewRef, isFront) {
            val previewView = previewViewRef ?: return@LaunchedEffect
            startCamera(previewView, isFront)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단: 왼 알림, 중앙 품질 탭(3개), 우 카메라 전환
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
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "알림", tint = Color.White)
                    }

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
                    ) {
                        Icon(Icons.Filled.FlipCameraAndroid, contentDescription = "카메라 전환", tint = Color.White)
                    }
                }

                // 중단: 카메라 프리뷰
                AndroidPreviewView(
                    onViewReady = { previewViewRef = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // 하단: 5개 버튼(가운데 촬영 버튼)
                BottomFiveBar(
                    onTune = { Toast.makeText(context, "보정: 준비 중", Toast.LENGTH_SHORT).show() },
                    onEffect = { Toast.makeText(context, "이펙트: 준비 중", Toast.LENGTH_SHORT).show() },
                    onShutter = { takePhoto() },
                    onBeauty = { Toast.makeText(context, "뷰티: 준비 중", Toast.LENGTH_SHORT).show() },
                    onFilter = { Toast.makeText(context, "필터: 준비 중", Toast.LENGTH_SHORT).show() },
                )
            }
        }
    }

    @Composable
    private fun AndroidPreviewView(onViewReady: (PreviewView) -> Unit, modifier: Modifier = Modifier) {
        AndroidView(factory = { ctx: Context ->
            val previewView = PreviewView(ctx)
            previewView.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            onViewReady(previewView)
            previewView
        }, modifier = modifier)
    }

    private fun startCamera(previewView: PreviewView, isFront: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (_: Exception) {
                Toast.makeText(this, "카메라 초기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }, cameraExecutor)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run { Toast.makeText(this, "카메라가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show(); return }

        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri: Uri = FileProvider.getUriForFile(this@FaceWeightCaptureActivity, "${applicationContext.packageName}.fileprovider", photoFile)
                // 전달 후 엔트리 화면으로 이동
                val intent = Intent(this@FaceWeightCaptureActivity, FaceWeightEntryActivity::class.java).apply {
                    putExtra("entry_action", "camera")
                    putExtra("image_uri", savedUri.toString())
                }
                // grant permission to receiver
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
                finish()
            }

            override fun onError(exception: ImageCaptureException) {
                runOnUiThread { Toast.makeText(this@FaceWeightCaptureActivity, "사진 저장 실패: ${exception.message}", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun createImageFile(): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val filename = "FW_IMG_${sdf.format(Date())}.jpg"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, filename)
    }

    // ---------- Small UI pieces ----------

    @Composable
    private fun TopQualitySelector(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier,
            color = Color.Black.copy(alpha = 0.25f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                modifier = Modifier
                    .clickable { onClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun BottomFiveBar(
        onTune: () -> Unit,
        onEffect: () -> Unit,
        onShutter: () -> Unit,
        onBeauty: () -> Unit,
        onFilter: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(icon = Icons.Filled.Tune, label = "보정", onClick = onTune)
            BottomItem(icon = Icons.Filled.AutoAwesome, label = "이펙트", onClick = onEffect)

            // 중앙 촬영 버튼 (강조)
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onShutter() },
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

            BottomItem(icon = Icons.Filled.FaceRetouchingNatural, label = "뷰티", onClick = onBeauty)
            BottomItem(icon = Icons.Filled.FilterAlt, label = "필터", onClick = onFilter)
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
            ) {
                Icon(icon, contentDescription = label, tint = Color.White)
            }
            Spacer(Modifier.height(6.dp))
            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }

    private fun toast(ctx: Context, msg: String) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }
}
