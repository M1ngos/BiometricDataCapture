package com.acsunmz.datacapture.core.network

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraViewModel
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraViewModel.UploadStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class SendCaptureDataViewModel : ViewModel() {
    private val _uploadStatus = mutableStateOf<UploadStatus>(UploadStatus.Idle)
    var uploadStatus:UploadStatus
        get() = _uploadStatus.value
        set(value) {
            _uploadStatus.value = value
            // Start auto-dismiss timer for relevant statuses
            if (
                value is UploadStatus.Success ||
                value is UploadStatus.Error
            ) {
                startStatusDismissTimer()
            }
        }

    private val _terminate = mutableStateOf(false)
    var terminate: Boolean
        get() = _terminate.value
        set(value) {
            _terminate.value = value
        }


    private var statusDismissJob: Job? = null

    private val httpClient = HttpClient()

    suspend fun uploadCaptureData(imageFile: File, signatureFile: File) {
        try {
            val response = httpClient.submitFormWithBinaryData(
                url = "http://192.168.1.144:8000/capture",
                formData = formData {
                    append("photo", imageFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${imageFile.name}")
                    })
                    append("signature", signatureFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=${signatureFile.name}")
                    })
                }
            )


            val responseBody = response.bodyAsText()
            val jsonResponse = JSONObject(responseBody)
            val message = jsonResponse.getString("message")

            uploadStatus = if (response.status.value == 409) {
                UploadStatus.Error(message)
            } else if (response.status.isSuccess()) {
                UploadStatus.Success(message)
            } else {
                UploadStatus.Error("Could not process face features")
            }

            if (uploadStatus is UploadStatus.Success) {
                delay(3000)
                terminate = true
            }

        }  catch (e: Exception) {
            uploadStatus = UploadStatus.Error("Server error: ${e.message}")
            Log.d("Upload", "${e.message}")
        } finally {
            httpClient.close()
        }
    }


    sealed class UploadStatus {
        object Idle : UploadStatus()
        object Uploading : UploadStatus()
        data class Success(val message: String) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }

    private fun startStatusDismissTimer() {
        // Cancel any existing timer
        statusDismissJob?.cancel()

        // Start new timer
        statusDismissJob = viewModelScope.launch {
            delay(5000)
            uploadStatus = UploadStatus.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
        statusDismissJob?.cancel()
    }
}


