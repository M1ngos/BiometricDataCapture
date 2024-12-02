package com.acsunmz.datacapture.feature.biometrics.camerax

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.webkit.PermissionRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors
import kotlin.math.abs

class LivenessDetector : ImageAnalysis.Analyzer {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    private var lastBlinkTime = 0L
    private var blinkCount = 0
    private var lastHeadPose = Triple(0f, 0f, 0f)
    private var hasDetectedMovement = false
    private var startTime = System.currentTimeMillis()

    private val requiredBlinkCount = 2
    private val maxCheckDuration = 10000L // 10 seconds
    private val minHeadMovement = 5f // degrees

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]

                        // Check for blinking
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1f

                        // Consider it a blink if both eyes are mostly closed
                        if (leftEyeOpen < 0.2 && rightEyeOpen < 0.2) {
                            val currentTime = System.currentTimeMillis()
                            // Ensure it's not counting the same blink
                            if (currentTime - lastBlinkTime > 500) {
                                blinkCount++
                                lastBlinkTime = currentTime
                            }
                        }

                        // Check for head movement
                        val currentHeadPose = Triple(
                            face.headEulerAngleX,
                            face.headEulerAngleY,
                            face.headEulerAngleZ
                        )

                        if (!hasDetectedMovement) {
                            val movement = abs(currentHeadPose.first - lastHeadPose.first) +
                                    abs(currentHeadPose.second - lastHeadPose.second) +
                                    abs(currentHeadPose.third - lastHeadPose.third)

                            if (movement > minHeadMovement) {
                                hasDetectedMovement = true
                            }
                        }

                        lastHeadPose = currentHeadPose

                        // Update liveness result
                        updateLivenessResult()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LivenessDetector", "Face detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun updateLivenessResult() {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - startTime

        val isLive = blinkCount >= requiredBlinkCount && hasDetectedMovement
        val isTimeout = timeElapsed > maxCheckDuration

        livenessState.value = when {
            isTimeout -> LivenessState.Timeout
            isLive -> LivenessState.Live
            else -> LivenessState.Checking(
                blinkCount = blinkCount,
                hasMovement = hasDetectedMovement,
                timeRemaining = (maxCheckDuration - timeElapsed) / 1000
            )
        }
    }

    fun reset() {
        blinkCount = 0
        hasDetectedMovement = false
        startTime = System.currentTimeMillis()
        lastBlinkTime = 0L
        lastHeadPose = Triple(0f, 0f, 0f)
        livenessState.value = LivenessState.Checking(0, false, maxCheckDuration / 1000)
    }

    companion object {
        val livenessState = MutableStateFlow<LivenessState>(
            LivenessState.Checking(0, false, 10)
        )
    }
}

sealed class LivenessState {
    data class Checking(
        val blinkCount: Int,
        val hasMovement: Boolean,
        val timeRemaining: Long
    ) : LivenessState()
    object Live : LivenessState()
    object Timeout : LivenessState()
}

@Composable
fun CameraScreen(
    navController: NavHostController,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasCameraPermission = context.hasRequiredPermissions()
    }

    if (!hasCameraPermission) {
        PermissionRequest { granted ->
            hasCameraPermission = granted
        }
    } else {
        LivenessCheckPreview(navController)
    }
}

@Composable
fun LivenessCheckPreview(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val livenessDetector = remember { LivenessDetector() }

    val livenessState by LivenessDetector.livenessState.collectAsState(
        LivenessState.Checking(0, false, 10)
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, livenessDetector)
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("LivenessCheck", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = livenessState) {
                is LivenessState.Checking -> {
                    Text("Blinks detected: ${state.blinkCount}/2")
                    Text("Head movement: ${if (state.hasMovement) "Detected" else "Not detected"}")
                    Text("Time remaining: ${state.timeRemaining}s")
                }
                LivenessState.Live -> {
                    Text("Liveness Confirmed!")
                    Button(
                        onClick = {
                            navController.navigate("next_screen")
                        }
                    ) {
                        Text("Continue")
                    }
                }
                LivenessState.Timeout -> {
                    Text("Timeout - Please try again")
                    Button(
                        onClick = {
                            livenessDetector.reset()
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequest(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Requesting camera permission...")
    }
}

private fun Context.hasRequiredPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}