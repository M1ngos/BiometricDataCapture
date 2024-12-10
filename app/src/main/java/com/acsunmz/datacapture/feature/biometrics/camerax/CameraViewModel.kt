package com.acsunmz.datacapture.feature.biometrics.camerax

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import org.json.JSONObject
import java.io.File

class CameraViewModel : ViewModel() {
    var uploadStatus by mutableStateOf<UploadStatus>(UploadStatus.Idle)
    private val httpClient = HttpClient()

    sealed class UploadStatus {
        object Idle : UploadStatus()
        object Uploading : UploadStatus()
        data class Success(val message: String) : UploadStatus()
        data class FaceAlreadyExists(val message: String) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }

    suspend fun uploadImage(imageFile: File) {
        uploadStatus = UploadStatus.Uploading
        try {
            val response = httpClient.submitFormWithBinaryData(
//                url = "https://your-fastapi-endpoint.com/upload",
                url = "http://192.168.1.209:8000/upload",
                formData = formData {
                    append("file", imageFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${imageFile.name}")
                    })
                }
            )

            val responseBody = response.bodyAsText()
            val jsonResponse = JSONObject(responseBody)
            val message = jsonResponse.getString("message")

            uploadStatus = if (response.status.value == 409) {
                UploadStatus.FaceAlreadyExists(message)
            } else if (response.status.isSuccess()) {
                UploadStatus.Success(message)
            } else {
                UploadStatus.Error("Could not process face features")
            }
        } catch (e: Exception) {
            uploadStatus = UploadStatus.Error("Server error: ${e.message}")
            Log.d("Upload", "${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}