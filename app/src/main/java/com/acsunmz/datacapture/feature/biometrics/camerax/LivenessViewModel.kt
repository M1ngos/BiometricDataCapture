package com.acsunmz.datacapture.feature.biometrics.camerax

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.tasks.await
import java.io.File

class LivenessViewModel : ViewModel() {
    // Liveness Detection Stages
    enum class LivenessStage {
        INITIAL,           // Starting state
        HEAD_PITCH_LEFT,   // Tilt head left
        HEAD_PITCH_RIGHT,  // Tilt head right
        SMILE,             // Smile detection
        BLINK,             // Eye blink detection
        COMPLETED          // All checks passed
    }

    // State variables
    var currentStage by mutableStateOf(LivenessStage.INITIAL)
    var stageProgress by mutableStateOf(0f)
    var isLivenessPassed by mutableStateOf(false)
    var uploadStatus by mutableStateOf<UploadStatus>(UploadStatus.Idle)

    // Upload status handling
    sealed class UploadStatus {
        object Idle : UploadStatus()
        object Uploading : UploadStatus()
        data class Success(val message: String) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }

    // Liveness Detector
    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    private val faceDetector = FaceDetection.getClient(highAccuracyOpts)
    private val httpClient = HttpClient()

    // Stage progression logic
    suspend fun progressStage(bitmap: Bitmap): Boolean {
        val image = InputImage.fromBitmap(bitmap, 0)

        return try {
            val faces = faceDetector.process(image).await()
            if (faces.isEmpty()) return false

            val face = faces.first()

            val result = when (currentStage) {
                LivenessStage.HEAD_PITCH_LEFT -> {
                    // Check for left head tilt
                    face.headEulerAngleY?.let {
                        it > 20 && it < 45
                    } ?: false
                }
                LivenessStage.HEAD_PITCH_RIGHT -> {
                    // Check for right head tilt
                    face.headEulerAngleY?.let {
                        it < -20 && it > -45
                    } ?: false
                }
                LivenessStage.SMILE -> {
                    // Check for smile
                    face.smilingProbability?.let {
                        it > 0.7
                    } ?: false
                }
                LivenessStage.BLINK -> {
                    // Check for blink
                    face.leftEyeOpenProbability?.let { leftEye ->
                        face.rightEyeOpenProbability?.let { rightEye ->
                            leftEye < 0.3 && rightEye < 0.3
                        }
                    } ?: false
                }
                else -> false
            }

            result
        } catch (e: Exception) {
            Log.e("LivenessDetector", "Error in stage detection", e)
            false
        }
    }

    // Upload image to endpoint
    suspend fun uploadImage(imageFile: File): Unit {
        uploadStatus = UploadStatus.Uploading
        try {
            val response = httpClient.submitFormWithBinaryData(
                url = "https://your-fastapi-endpoint.com/liveness-upload",
                formData = formData {
                    append("file", imageFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${imageFile.name}")
                    })
                    append("liveness_stages", currentStage.name)
                }
            )

            uploadStatus = if (response.status.isSuccess()) {
                UploadStatus.Success("Liveness verification complete")
            } else {
                UploadStatus.Error("Upload failed: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            uploadStatus = UploadStatus.Error("Upload error: ${e.message}")
            Log.e("ImageUpload", "Error uploading image", e)
        }
    }

    // Manage stage progression
    fun advanceStage() {
        currentStage = when (currentStage) {
            LivenessStage.INITIAL -> LivenessStage.HEAD_PITCH_LEFT
            LivenessStage.HEAD_PITCH_LEFT -> LivenessStage.HEAD_PITCH_RIGHT
            LivenessStage.HEAD_PITCH_RIGHT -> LivenessStage.SMILE
            LivenessStage.SMILE -> LivenessStage.BLINK
            LivenessStage.BLINK -> {
                isLivenessPassed = true
                LivenessStage.COMPLETED
            }
            LivenessStage.COMPLETED -> LivenessStage.COMPLETED
        }
        stageProgress = 0f
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}