package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
//import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalLifecycleOwner

import androidx.lifecycle.viewModelScope
import com.acsunmz.datacapture.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.log

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = viewModel(),
    documentType: DocumentType,
    onDocumentProcessed: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }
    val previewView = remember { PreviewView(context) }

    // Track the current scanning state
    var currentSide by remember { mutableStateOf<CardSide>(CardSide.Front) }
    var frontSideFields by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var backSideFields by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var scanningInProgress by remember { mutableStateOf(true) }
    var isScanComplete by remember { mutableStateOf(false) }

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

    // Setup realtime scanning based on current side and document type
    LaunchedEffect(currentSide) {
        setupRealtimeScanning(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            cameraExecutor = cameraExecutor,
            documentType = documentType,
            currentSide = currentSide,
            onFieldsDetected = { fields ->
                // Determine which side's fields to update
                when (currentSide) {
                    CardSide.Front -> {
                        // For front side, check if all required front-side fields are detected
                        val requiredFrontFields = documentType.fields
                            .filter { it.side == CardSide.Front && it.required }
                            .map { it.name }

                        frontSideFields = fields

                        // Check if all required front-side fields are detected
                        if (requiredFrontFields.all { fields.keys.contains(it) }) {
                            // Move to back side
                            currentSide = CardSide.Back
                            scanningInProgress = true
                        }
                    }
                    CardSide.Back -> {
                        // For back side, check if all required back-side fields are detected
                        val requiredBackFields = documentType.fields
                            .filter { it.side == CardSide.Back && it.required }
                            .map { it.name }

                        backSideFields = fields

                        // Check if all required back-side fields are detected
                        if (requiredBackFields.all { fields.keys.contains(it) }) {
                            // Scanning is complete
                            scanningInProgress = false
                            isScanComplete = true
                        }
                    }
                }
            }
        )
    }

    // When scanning is complete, combine and process fields
    LaunchedEffect(isScanComplete) {
        if (isScanComplete) {
            // Combine front and back side fields
            val combinedFields = frontSideFields + backSideFields
            onDocumentProcessed(combinedFields)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        // UI overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with side indicator
            Text(
                text = when {
                    isScanComplete -> "Scan Complete"
                    currentSide == CardSide.Front -> "Scanning Front Side"
                    else -> "Scanning Back Side"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Detected fields
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                val currentFields = when {
                    currentSide == CardSide.Front -> frontSideFields
                    currentSide == CardSide.Back -> backSideFields
                    else -> emptyMap()
                }
                currentFields.forEach { (fieldName, value) ->
                    Text(
                        text = "$fieldName: $value",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun setupRealtimeScanning(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    cameraExecutor: java.util.concurrent.Executor,
    documentType: DocumentType,
    currentSide: CardSide? = null, // Optional parameter, only used for MozambicanID
    onFieldsDetected: (Map<String, String>) -> Unit
) {
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val detectionHistory = mutableMapOf<String, MutableList<String>>()
    val requiredDetections = 2

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val currentDetections = mutableMapOf<String, String>()
                                val fullText = visionText.text.uppercase()

                                Log.d("TextRecognition", "Full Detected Text: $fullText")

                                val extractedFields = extractFields(fullText)

                                Log.d("TextRecognition", "Extracted Fields: $extractedFields")

                                // Get the relevant fields based on document type and side
                                val relevantFields = when {
                                    documentType == DocumentType.MozambicanID && currentSide != null -> {
                                        // For MozambicanID, get fields specific to the current side
                                        documentType.fields.filter { it.side == currentSide }
                                    }
                                    else -> {
                                        // For other documents, get all fields
                                        documentType.fields
                                    }
                                }

                                // Process fields based on document type
                                relevantFields.forEach { field ->
                                    val fieldValue = when (field.name) {
                                        "Nº:" -> extractedFields["idNumber"]
                                        "Nome / Name" -> extractedFields["name"]
                                        "Data de Nascimento / Date of Birth" -> extractedFields["dateOfBirth"]
                                        "Naturalidade / Place of Birth" -> extractedFields["placeOfBirth"]
                                        "Altura / Height" -> extractedFields["height"]
                                        "Local de Residência / Address" -> extractedFields["address"]
                                        // Add more field mappings for other document types
                                        else -> null
                                    }

                                    fieldValue?.let { value ->
                                        // Update detection history
                                        detectionHistory.getOrPut(field.name) { mutableListOf() }.add(value)
                                        if (detectionHistory[field.name]!!.size > requiredDetections) {
                                            detectionHistory[field.name]!!.removeAt(0)
                                        }

                                        // Check for consistent detections
                                        val mostCommon = detectionHistory[field.name]!!
                                            .groupBy { it }
                                            .maxByOrNull { it.value.size }

                                        if (mostCommon != null &&
                                            mostCommon.value.size >= requiredDetections) {
                                            currentDetections[field.name] = mostCommon.key
                                        }
                                    }
                                }

                                // Only notify if we have detected fields
                                if (currentDetections.isNotEmpty()) {
                                    onFieldsDetected(currentDetections)
                                }
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
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("ScannerScreen", "Failed to bind camera use cases", exc)
        }
    }, cameraExecutor)
}



private fun extractFields(fullText: String): Map<String, String> {
    val fields = mutableMapOf<String, String>()

    Log.d("FieldExtraction", "Extracted Fields: $fields")

//    // ID Number extraction (format: XXXXXXXXXXXXXXXXX or with prefix Nº:)
//    "(?:Nº:|N°:)\\s*(\\d{13}[A-Z])".toRegex().find(fullText)?.groupValues?.get(1)?.let {
//        fields["idNumber"] = it
//    } ?: run {
//        // Try alternative pattern from MRZ
//        "IDMOZA[A-Z0-9]+".toRegex().find(fullText)?.value?.let {
//            fields["idNumber"] = it.substring(6, 19)
//        }
//    }

    // Name extraction
    "(?:NOME\\s*/\\s*NAME:?)\\s*([A-ZÁÉÍÓÚÇÑ\\s]+?)(?=\\s*(?:DATA|LUGAR|$))".toRegex()
        .find(fullText)?.groupValues?.get(1)?.trim()?.let {
            fields["name"] = it
        }

    // Date of Birth extraction
    "(?:DATA DE NASCIMENTO|DATE OF BIRTH)\\s*:?\\s*(\\d{2}/\\d{2}/\\d{4})".toRegex()
        .find(fullText)?.groupValues?.get(1)?.let {
            fields["dateOfBirth"] = it
        }

    // Place of Birth extraction
    "(?:NATURALIDADE|PLACE OF BIRTH)\\s*:?\\s*([A-ZÁÉÍÓÚÇÑ\\s-]+?)(?=\\s*(?:LOCAL|ALTURA|$))".toRegex()
        .find(fullText)?.groupValues?.get(1)?.trim()?.let {
            fields["placeOfBirth"] = it
        }

    // Height extraction
    "(?:ALTURA|HEIGHT)\\s*:?\\s*(\\d{1,}[.,]\\d{2})\\s*(?:M|m)?".toRegex()
        .find(fullText)?.groupValues?.get(1)?.let {
            fields["height"] = it
        }

    // Address extraction
    "(?:LOCAL DE RESIDÊNCIA|ADDRESS)\\s*:?\\s*([A-ZÁÉÍÓÚÇÑ\\s.,0-9-]+?)(?=\\s*(?:ASSINATURA|$))".toRegex()
        .find(fullText)?.groupValues?.get(1)?.trim()?.let {
            fields["address"] = it
        }

    DocumentType.MozambicanID.fields.forEach { field ->
        if (!fields.containsKey(field.name)) {
            Log.d("FieldExtraction", "Failed to extract field: ${field.name}")
            Log.d("FieldExtraction", "Regex used: ${field.regex}")
        }
    }

    return fields
}