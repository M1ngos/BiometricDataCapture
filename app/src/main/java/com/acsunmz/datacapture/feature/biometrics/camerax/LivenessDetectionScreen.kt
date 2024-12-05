package com.acsunmz.datacapture.feature.biometrics.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun LivenessDetectionScreen(
    viewModel: LivenessViewModel = viewModel(),
    onLivenessComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val coroutineScope = rememberCoroutineScope()

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
        }
    }

    // Request camera permission
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Stage progression logic
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }

    // Setup camera
    LaunchedEffect(Unit) {
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(context.display?.rotation ?: 0)
            .build()

        setupCameraPreview(
            context,
            lifecycleOwner,
            previewView,
            imageCapture!!,
            CameraSelector.DEFAULT_FRONT_CAMERA
        )
    }

    // Animated progress for current stage
    val animatedProgress by animateFloatAsState(
        targetValue = viewModel.stageProgress,
        label = "Stage Progress"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stage Instruction Text
        Text(
            text = when (viewModel.currentStage) {
                LivenessViewModel.LivenessStage.INITIAL -> "Prepare for Liveness Check"
                LivenessViewModel.LivenessStage.HEAD_PITCH_LEFT -> "Tilt Head Left"
                LivenessViewModel.LivenessStage.HEAD_PITCH_RIGHT -> "Tilt Head Right"
                LivenessViewModel.LivenessStage.SMILE -> "Please Smile"
                LivenessViewModel.LivenessStage.BLINK -> "Blink Both Eyes"
                LivenessViewModel.LivenessStage.COMPLETED -> "Liveness Verified!"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        // Progress Indicator
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Camera Preview
        AndroidView(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            factory = { previewView }
        )

        // Stage Management
        if (viewModel.currentStage != LivenessViewModel.LivenessStage.COMPLETED) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Capture image for current stage
                        imageCapture?.let { capture ->
                            val file = File(context.cacheDir, "liveness_stage_${viewModel.currentStage}.jpg")

                            capture.takePicture(
                                ImageCapture.OutputFileOptions.Builder(file).build(),
                                executor,
                                object : ImageCapture.OnImageSavedCallback {
                                    @SuppressLint("RestrictedApi")
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        coroutineScope.launch {
                                            // Convert file to bitmap using BitmapFactory
                                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)?.let { originalBitmap ->
                                                // Ensure correct orientation
                                                val matrix = Matrix()
                                                val exif = ExifInterface(file.absolutePath)
                                                val orientation = exif.getAttributeInt(
                                                    ExifInterface.TAG_ORIENTATION,
                                                    ExifInterface.ORIENTATION_NORMAL
                                                )

                                                when (orientation) {
                                                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                                                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                                                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                                                }

                                                Bitmap.createBitmap(
                                                    originalBitmap,
                                                    0,
                                                    0,
                                                    originalBitmap.width,
                                                    originalBitmap.height,
                                                    matrix,
                                                    true
                                                )
                                            }

                                            if (bitmap != null && viewModel.progressStage(bitmap)) {
                                                // Rest of your existing code...
                                                for (i in 1..10) {
                                                    viewModel.stageProgress = i / 10f
                                                    delay(100)
                                                }

                                                viewModel.advanceStage()

                                                if (viewModel.currentStage == LivenessViewModel.LivenessStage.COMPLETED) {
                                                    viewModel.uploadImage(file)
                                                    onLivenessComplete()
                                                }
                                            }
                                        }
                                    }


                                    override fun onError(exc: ImageCaptureException) {
                                        Log.e("ImageCapture", "Image capture failed", exc)
                                    }
                                }
                            )
                        }
                    }
                }
            ) {
                Text("Verify Stage")
            }
        }

        // Upload Status
        when (val status = viewModel.uploadStatus) {
            is LivenessViewModel.UploadStatus.Uploading -> {
                CircularProgressIndicator()
                Text("Uploading verification...")
            }
            is LivenessViewModel.UploadStatus.Success -> {
                Text(
                    text = status.message,
                    color = Color.Green
                )
            }
            is LivenessViewModel.UploadStatus.Error -> {
                Text(
                    text = status.message,
                    color = Color.Red
                )
            }
            else -> {} // Idle state
        }
    }
}

@Composable
fun warning() {
    Text("Camera permission is required", modifier = Modifier.padding(16.dp))
}

// Camera preview setup function (similar to previous examples)
private fun setupCameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    cameraSelector: CameraSelector
) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    try {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    } catch (exc: Exception) {
        Log.e("CameraSetup", "Camera setup failed", exc)
    }
}