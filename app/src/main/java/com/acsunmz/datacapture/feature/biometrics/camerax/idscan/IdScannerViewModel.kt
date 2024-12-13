package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IdScannerViewModel : ViewModel() {
    var extractedText by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var currentPhotoUri: Uri? = null
        private set

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun startCamera(context: Context, cameraLauncher: ActivityResultLauncher<Uri>) {
        viewModelScope.launch {
            try {
                val photoFile = createImageFile(context)
                currentPhotoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    photoFile
                )
                currentPhotoUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            } catch (e: Exception) {
                errorMessage = "Failed to start camera: ${e.message}"
            }
        }
    }

    fun processImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null

            try {
                val image = InputImage.fromFilePath(context, uri)

                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        extractedText = visionText.text
                        isProcessing = false
                    }
                    .addOnFailureListener { e ->
                        errorMessage = "Failed to process image: ${e.message}"
                        isProcessing = false
                    }
            } catch (e: Exception) {
                errorMessage = "Failed to process image: ${e.message}"
                isProcessing = false
            }
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}