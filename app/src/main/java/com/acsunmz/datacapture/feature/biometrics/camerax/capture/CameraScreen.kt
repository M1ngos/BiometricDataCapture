package com.acsunmz.datacapture.feature.biometrics.camerax.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.LifecycleOwner
import com.acsunmz.datacapture.R
import com.acsunmz.datacapture.core.presentation.navigation.Destinations
import com.acsunmz.datacapture.ui.theme.YellowStatusBackground
import com.acsunmz.datacapture.ui.theme.YellowStatusContent
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors


@OptIn(ExperimentalGetImage::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(),
    navigate: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val coroutineScope = rememberCoroutineScope()

    // Camera provider state
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }

    // Liveness check states
    var faceDetected by remember { mutableStateOf(false) }
    var eyesOpen by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var hasMoved by remember { mutableStateOf(false) }
    var liveness_passed by remember { mutableStateOf(false) }

    liveness_passed = faceDetected && eyesOpen && isBlinking && hasMoved


    // Anti-spoofing variables
    var lastHeadEulerY by remember { mutableStateOf<Float?>(null) }
    var movementCounter by remember { mutableStateOf(0) }
    var blinkCounter by remember { mutableStateOf(0) }
    var lastBlinkTime by remember { mutableStateOf<Long?>(null) }


    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(isBlinking && faceDetected && eyesOpen && hasMoved) {
        liveness_passed = true
    }
    LaunchedEffect(viewModel.shouldNavigate) {
        if (viewModel.shouldNavigate) {
            navigate()
            // Reset the navigation flag
            viewModel.shouldNavigate = false
        }
    }

    val message = when (val status = viewModel.uploadStatus) {
        is CameraViewModel.UploadStatus.Success -> status.message
        is CameraViewModel.UploadStatus.Error -> status.message
        else -> null
    }


    message?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                var previewUseCase by remember { mutableStateOf<Preview?>(null) }
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context).apply {
                            this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val executor = Executors.newSingleThreadExecutor()
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                        val faceDetector = FaceDetection.getClient(
                            FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                .enableTracking()
                                .build()
                        )

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            previewUseCase = Preview.Builder().build()

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .apply {
                                    setAnalyzer(executor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )

                                            faceDetector.process(image)
                                                .addOnSuccessListener { faces ->
                                                    if (faces.isNotEmpty()) {
                                                        val face = faces[0]
                                                        faceDetected = true

                                                        // Head movement detection
                                                        val currentHeadEulerY =
                                                            face.headEulerAngleY
                                                        if (lastHeadEulerY != null) {
                                                            val movement =
                                                                kotlin.math.abs(
                                                                    currentHeadEulerY - lastHeadEulerY!!
                                                                )
                                                            if (movement > 15f) {
                                                                movementCounter++
                                                                if (movementCounter >= 2) {
                                                                    hasMoved = true
                                                                }
                                                            }
                                                        }
                                                        lastHeadEulerY = currentHeadEulerY

                                                        // Eye state detection
                                                        val leftEyeOpen =
                                                            face.leftEyeOpenProbability ?: 0f
                                                        val rightEyeOpen =
                                                            face.rightEyeOpenProbability ?: 0f
                                                        val currentlyOpen =
                                                            leftEyeOpen > 0.8f && rightEyeOpen > 0.8f

                                                        // Blink detection
                                                        if (!currentlyOpen && eyesOpen) {
                                                            val currentTime =
                                                                System.currentTimeMillis()
                                                            if (lastBlinkTime == null || currentTime - lastBlinkTime!! > 500) {
                                                                blinkCounter++
                                                                lastBlinkTime = currentTime
                                                                if (blinkCounter >= 1) {
                                                                    isBlinking = true
                                                                }
                                                            }
                                                        }
                                                        eyesOpen = currentlyOpen
                                                    } else {
                                                        faceDetected = false
                                                        eyesOpen = false
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    // Handle any errors
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    previewUseCase,
                                    imageAnalyzer
                                )

                                previewUseCase?.setSurfaceProvider(previewView.surfaceProvider)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = getInstructions(faceDetected, hasMoved, isBlinking, eyesOpen),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onPrimary
                )

                // Overlay content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    DisplayStatus(viewModel)
                }

                // Bottom section with verification status and camera button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (!liveness_passed) {
                        Text(
                            text = "Status de verificação:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            StatusRow("Rosto detectado", faceDetected)
                            StatusRow("Olhos abertos", eyesOpen)
                            StatusRow("Piscada detectada", isBlinking)
                            StatusRow("Movimento da cabeça", hasMoved)
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))

                    FilledTonalButton(
                        enabled = liveness_passed,
                        onClick = {
                            Log.d("uploadImage", "debug2")

                            coroutineScope.launch {
                                cameraProvider?.let {
                                    val imageCapture = ImageCapture.Builder()
                                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                                        .build()

                                    cameraProvider!!.bindToLifecycle(
                                        context as LifecycleOwner,
                                        cameraSelector,
                                        imageCapture
                                    )

                                    val file = File(context.cacheDir, "captured_image.jpg")
                                    Log.d("uploadImage", "debug1")

                                    val outputOptions =
                                        ImageCapture.OutputFileOptions.Builder(file).build()

                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                coroutineScope.launch {
                                                    viewModel.uploadImage(file)
                                                    Log.d("uploadImage", "debug0")
                                                }
                                            }

                                            override fun onError(exc: ImageCaptureException) {
                                                viewModel.uploadStatus =
                                                    CameraViewModel.UploadStatus.Error(
                                                        "Failed to capture image: ${exc.message}"
                                                    )
                                            }
                                        }
                                    )
                                }
                            }

                        },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.photo_camera_24dp),
                            contentDescription = "Take Photo",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


private fun getInstructions(
    faceDetected: Boolean,
    hasMoved: Boolean,
    isBlinking: Boolean,
    eyesOpen: Boolean
): String = when {
    !faceDetected -> "Position your face in the frame"
    !hasMoved -> "Slowly turn your head to the left"
    !isBlinking -> "Blink naturally"
    !eyesOpen -> "Keep your eyes open"
    else -> ""
}

@Composable
private fun StatusRow(
    label: String,
    status: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (status) {
                androidx.compose.material.icons.Icons.Default.CheckCircle
            } else {
                androidx.compose.material.icons.Icons.Default.Error
            },
            contentDescription = null,
            tint = if (status) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}


@Composable
fun DisplayStatus(viewModel: CameraViewModel) {
    // Status area at the top
    when (val status = viewModel.uploadStatus) {
        is CameraViewModel.UploadStatus.Uploading -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Uploading...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        is CameraViewModel.UploadStatus.Success -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        status.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        is CameraViewModel.UploadStatus.Error -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.error_24dp),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        status.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        is CameraViewModel.UploadStatus.FaceAlreadyExists -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = YellowStatusBackground.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Warning",
                        tint = YellowStatusContent
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        status.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = YellowStatusContent
                    )
                }
            }
        }

        else -> {} // Idle state
    }
}