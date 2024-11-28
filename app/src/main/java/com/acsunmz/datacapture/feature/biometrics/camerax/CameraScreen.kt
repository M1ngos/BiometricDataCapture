package com.acsunmz.datacapture.feature.biometrics.camerax

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
import com.acsunmz.datacapture.core.presentation.navigation.Destinations
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

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
        CameraPreview(navController)
    }
}

@Composable
fun CameraPreview(
    navController: NavHostController,

    ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var faceDetected by remember { mutableStateOf(false) }

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
                        it.setAnalyzer(cameraExecutor, FaceAnalyzer { detected ->
                            faceDetected = detected
                        })
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
                        imageCapture,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // UI overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (faceDetected) "Face Detected!" else "No Face Detected",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = {
                    navController.popBackStack()
                    navController.navigate(Destinations.SignatureScreenWrapper)
                }
            ) {
                Text("Take Photo")
            }
        }
    }
}

class FaceAnalyzer(private val onFaceDetected: (Boolean) -> Unit) : ImageAnalysis.Analyzer {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    onFaceDetected(faces.isNotEmpty())
                }
                .addOnFailureListener { e ->
                    Log.e("FaceAnalyzer", "Face detection failed", e)
                    onFaceDetected(false)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
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

// Extension function to check camera permission
private fun Context.hasRequiredPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}