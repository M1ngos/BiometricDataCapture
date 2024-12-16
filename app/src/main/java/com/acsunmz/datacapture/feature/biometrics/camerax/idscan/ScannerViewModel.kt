package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File

class ScannerViewModel : ViewModel() {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build())

    fun processDocument(
        context: Context,
        file: File,
        documentType: DocumentType,
        onComplete: (Map<String, String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, Uri.fromFile(file))
                val extractedData = mutableMapOf<String, String>()

                // Detect text
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        documentType.fields.forEach { field ->
                            if (field.name != "Photo Location") {
                                val fieldValue = extractFieldValue(visionText.text, field)
                                if (fieldValue != null) {
                                    extractedData[field.name] = fieldValue
                                }
                            }
                        }

                        // Detect face
                        faceDetector.process(image)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    extractedData["Photo Location"] = "${face.boundingBox}"
                                }
                                onComplete(extractedData)
                            }
                    }
            } catch (e: Exception) {
                Log.e("ScannerViewModel", "Document processing failed", e)
            }
        }
    }

    private fun extractFieldValue(text: String, field: DocumentField): String? {
        return if (field.regex != null) {
            val regex = Regex(field.regex)
            regex.find(text)?.value
        } else {
            // For fields without regex, try to find relevant text based on field name
            val lines = text.split("\n")
            lines.find { it.contains(field.name, ignoreCase = true) }
                ?.substringAfter(":")
                ?.trim()
        }
    }
}