package com.acsunmz.datacapture.feature.biometrics.camerax

import android.Manifest
import android.content.Context
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
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.res.painterResource
import com.acsunmz.datacapture.R
import com.acsunmz.datacapture.core.presentation.navigation.Destinations
import com.acsunmz.datacapture.ui.theme.YellowStatusBackground
import com.acsunmz.datacapture.ui.theme.YellowStatusContent


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val coroutineScope = rememberCoroutineScope()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denial
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) {
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(context.display?.rotation ?: 0)
            .build()

        setupCamera(
            context,
            lifecycleOwner,
            previewView,
            imageCapture!!
        )
    }

    LaunchedEffect(viewModel.shouldNavigate) {
        if (viewModel.shouldNavigate) {
            navController.navigate(Destinations.DocumentScanner) {
                popUpTo(Destinations.CameraScreen) { inclusive = true }
            }
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
        // Camera Preview - Now takes up the full screen
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        // Overlay content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
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

            // Capture button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            imageCapture?.let { capture ->
                                val file = File(context.cacheDir, "captured_image.jpg")

                                capture.takePicture(
                                    ImageCapture.OutputFileOptions.Builder(file).build(),
                                    executor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            coroutineScope.launch {
                                                viewModel.uploadImage(file)
                                            }
                                        }

                                        override fun onError(exc: ImageCaptureException) {
                                            viewModel.uploadStatus = CameraViewModel.UploadStatus.Error(
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
                }
            }
        }
    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    try {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview,
            imageCapture
        )
    } catch (exc: Exception) {
        exc.printStackTrace()
    }
}