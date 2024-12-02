package com.acsunmz.datacapture.feature.biometrics.camerax

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
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
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun DocumentScannerScreen(
    navController: NavHostController,
    onDocumentScanned: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    )}

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Request permission if not granted
    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val previewView = remember { PreviewView(context) }


    // Only attempt to set up camera if permission is granted
    if (hasCameraPermission) {
        DisposableEffect(lifecycleOwner) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any previous use cases
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("DocumentScanner", "Camera binding failed", exc)
            }

            onDispose {
                cameraProvider.unbindAll()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show camera preview only if permission is granted
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                factory = { previewView }
            )

            // Capture Button
            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        captureImage(
                            context,
                            capture,
                            onImageCaptured = { uri ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    processDocument(
                                        context,
                                        uri,
                                        textRecognizer
                                    ) { processedUri ->
                                        onDocumentScanned(processedUri)
                                    }
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Capture Document")
            }
        } else {
            Text("Camera permission is required", modifier = Modifier.padding(16.dp))
        }
    }
}

// Image capture function
private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val outputFile = File(context.cacheDir, "document_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(outputFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e("DocumentScanner", "Photo capture failed", exc)
            }
        }
    )
}

private fun processDocument(
    context: Context,
    imageUri: Uri,
    textRecognizer: TextRecognizer,
    onProcessed: (Uri) -> Unit
) {
    val inputImage = InputImage.fromFilePath(context, imageUri)

    textRecognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            // Log all extracted text
            Log.d("DocumentScanner", "Full Extracted Text: ${visionText.text}")

            // Log details of recognized text blocks
            visionText.textBlocks.forEachIndexed { index, block ->
                Log.d("DocumentScanner", "Text Block $index:")
                Log.d("DocumentScanner", "Block Text: ${block.text}")
//                Log.d("DocumentScanner", "Block Confidence: ${block.confidence}")
//                Log.d("DocumentScanner", "Block Languages: ${block.recognizedLanguages}")

                // Log lines within each block
                block.lines.forEachIndexed { lineIndex, line ->
                    Log.d("DocumentScanner", "  Line $lineIndex: ${line.text}")
                    Log.d("DocumentScanner", "  Line Confidence: ${line.confidence}")
                }
            }

            // Proceed with processing
            onProcessed(imageUri)
        }
        .addOnFailureListener { e ->
            Log.e("DocumentScanner", "Text recognition failed", e)
            onProcessed(imageUri)
        }
}