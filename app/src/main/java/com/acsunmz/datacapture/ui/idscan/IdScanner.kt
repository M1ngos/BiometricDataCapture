import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.ImageProxy
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acsunmz.datacapture.R
import com.acsunmz.datacapture.core.model.CardSide
import com.acsunmz.datacapture.core.model.IdData
import com.acsunmz.datacapture.core.model.ScanState
import com.acsunmz.datacapture.core.network.ScanSide
import com.acsunmz.datacapture.ui.idscan.IdScanViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.platform.android.BouncyCastleSocketAdapter.Companion.factory
import okhttp3.internal.platform.android.ConscryptSocketAdapter.Companion.factory
import java.io.File
import java.util.concurrent.Executors
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalGetImage::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun IdScanner(
    viewModel: IdScanViewModel = viewModel(),
    onScanComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var currentSide by remember { mutableStateOf(ScanSide.FRONT) }

    // Camera setup
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build()
                        imageCaptureUseCase = ImageCapture.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .build()

                        try {
                            cameraProvider?.unbindAll()
                            cameraProvider?.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCaptureUseCase
                            )
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        } catch (e: Exception) {
                            Log.e("IdScanner", "Camera init failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            FocusAssistanceOverlay()

            // Instruction overlays
            when (currentSide) {
                ScanSide.FRONT -> {
                    FrontInstructionOverlay()
                }
                ScanSide.BACK -> {
                    BackInstructionAnimation()
                }
            }

            // Capture button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            val file = File.createTempFile(
                                "id_${currentSide.name.lowercase()}",
                                ".jpg",
                                context.cacheDir
                            )

                            imageCaptureUseCase?.takePicture(
                                ImageCapture.OutputFileOptions.Builder(file).build(),
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        when(currentSide) {
                                            ScanSide.FRONT -> viewModel.setFrontImage(file)
                                            ScanSide.BACK -> viewModel.setBackImage(file)
                                        }

                                        if(currentSide == ScanSide.FRONT) {
                                            currentSide = ScanSide.BACK
                                        } else {
                                            viewModel.viewModelScope.launch {
                                                try {
                                                    viewModel.uploadImages()
                                                    onScanComplete()
                                                } catch (e: Exception) {
                                                    viewModel.setError("Upload failed: ${e.message}")
                                                }
                                            }
                                        }
                                    }

                                    override fun onError(exc: ImageCaptureException) {
                                        viewModel.setError("Capture failed: ${exc.message}")
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.photo_camera_24dp),
                        contentDescription = "Capture ID",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusAssistanceOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val targetWidth = canvasWidth * 0.85f
        val targetHeight = targetWidth * 0.63f

        // Create target area rect
        val targetRect = androidx.compose.ui.geometry.Rect(
            center.x - targetWidth / 2,
            center.y - targetHeight / 2,
            center.x + targetWidth / 2,
            center.y + targetHeight / 2
        )

        // Create paths for overlay
        val fullScreenPath = Path().apply {
            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
        }

        val targetAreaPath = Path().apply {
            addRect(targetRect)
        }

        // Create combined path using difference operation
        val finalPath = Path().apply {
            op(fullScreenPath, targetAreaPath, PathOperation.Difference)
        }

        // Draw overlay
        drawPath(finalPath, Color.Black.copy(alpha = 0.4f))

        // Corner line properties
        val cornerLength = 40.dp.toPx()
        val strokeWidth = 4.dp.toPx()

        // Draw corner brackets
        listOf(
            Triple(targetRect.left, targetRect.top, 1f to 1f),    // Top-left
            Triple(targetRect.right, targetRect.top, -1f to 1f),  // Top-right
            Triple(targetRect.left, targetRect.bottom, 1f to -1f),// Bottom-left
            Triple(targetRect.right, targetRect.bottom, -1f to -1f) // Bottom-right
        ).forEach { (x, y, direction) ->
            // Horizontal line
            drawLine(
                color = Color.White,
                start = Offset(x, y),
                end = Offset(x + cornerLength * direction.first, y),
                strokeWidth = strokeWidth
            )
            // Vertical line
            drawLine(
                color = Color.White,
                start = Offset(x, y),
                end = Offset(x, y + cornerLength * direction.second),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
private fun FrontInstructionOverlay() {
    var showInstructions by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showInstructions = false
    }

    AnimatedVisibility(
        visible = showInstructions,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Posicione a parte frontal do documento no quadro",
                color = Color.White,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}



@Composable
private fun BackInstructionAnimation() {
    var showInstructions by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showInstructions = false
    }

    AnimatedVisibility(
        visible = showInstructions,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coloque o verso do documento no quadro",
                color = Color.White,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
    /*
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.flip_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(300.dp)
                .alpha(0.9f)
        )
    }
    */
}

enum class ScanSide {
    FRONT, BACK
}