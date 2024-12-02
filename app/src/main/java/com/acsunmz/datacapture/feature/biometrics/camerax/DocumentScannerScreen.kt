package com.acsunmz.datacapture.feature.biometrics.camerax

import android.content.Context
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
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognition
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

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    val previewView = remember { PreviewView(context) }

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
            cameraProvider.unbindAll()
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

    // Capture image launcher
    val imageCaptureResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scannedImageUri = it
            onDocumentScanned(it)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera Preview
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            factory = { previewView }
        )

        // Capture Button
        Button(
            onClick = {
                captureImage(
                    context,
                    imageCapture!!,
                    onImageCaptured = { uri ->
                        coroutineScope.launch(Dispatchers.IO) {
                            processDocument(
                                context,
                                uri,
                                textRecognizer
                            ) { processedUri ->
                                scannedImageUri = processedUri
                                onDocumentScanned(processedUri)
                            }
                        }
                    }
                )
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Capture Document")
        }

        // Gallery Selection Button
        Button(
            onClick = { imageCaptureResultLauncher.launch("image/*") },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Select from Gallery")
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
            // Process recognized text
            val extractedText = visionText.text
            Log.d("DocumentScanner", "Extracted Text: $extractedText")

            // Here you can add more sophisticated document validation logic
            onProcessed(imageUri)
        }
        .addOnFailureListener { e ->
            Log.e("DocumentScanner", "Text recognition failed", e)
            onProcessed(imageUri)
        }
}