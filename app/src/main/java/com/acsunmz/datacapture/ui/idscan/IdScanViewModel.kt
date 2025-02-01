package com.acsunmz.datacapture.ui.idscan

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acsunmz.datacapture.core.data.SessionManager
import com.acsunmz.datacapture.core.navigation.Destinations
import com.acsunmz.datacapture.core.network.ScanSide
import com.acsunmz.datacapture.core.network.UrlProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

data class IdCardData(
    val documentType: String,
    val idNumber: String,
    val fullName: String,
    val dateOfBirth: String,
    val height: String,
    val sex: String,
    val birthPlace: String,
    val address: String
)

class IdScanViewModel : ViewModel() {
    private val url = UrlProvider.UPLOAD_ID_URL

    private val _currentSide = MutableStateFlow(ScanSide.FRONT)
    val currentSide: StateFlow<ScanSide> = _currentSide

    // State for ID card data with public getter
    private val _idCardData = mutableStateOf<IdCardData?>(null)
    val idCardData: IdCardData?
        get() = _idCardData.value

    private val _Id_uploadStatus = mutableStateOf<IdUploadStatus>(IdUploadStatus.Idle)
    var idUploadStatus: IdUploadStatus
        get() = _Id_uploadStatus.value
        set(value) {
            _Id_uploadStatus.value = value
            if (value is IdUploadStatus.Success ||
                value is IdUploadStatus.Error ||
                value is IdUploadStatus.FaceExtractionFailed
            ) {
                startStatusDismissTimer()
            }
        }

    private val _shouldNavigate = mutableStateOf(false)
    var shouldNavigate: Boolean
        get() = _shouldNavigate.value
        set(value) {
            _shouldNavigate.value = value
        }

    private var frontFile: File? = null
    private var backFile: File? = null

    fun setFrontImage(file: File) {
        frontFile = file
    }

    fun setBackImage(file: File) {
        backFile = file
    }

    private var statusDismissJob: Job? = null
    private val httpClient = HttpClient()

    suspend fun uploadImages() {
        idUploadStatus = IdUploadStatus.Uploading
        try {
            val front = frontFile ?: throw Exception("No front image captured")
            val back = backFile ?: throw Exception("No back image captured")

            Log.d("IdUpload", "Uploading front image: ${front.name}")
            Log.d("IdUpload", "Uploading back image: ${back.name}")

            val response = httpClient.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append("front", front.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${front.name}")
                    })
                    append("back", back.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${back.name}")
                    })
                }
            )

            val responseBody = response.bodyAsText()
            val jsonResponse = JSONObject(responseBody)
            Log.d("IdUpload", "json: $jsonResponse")

            if (response.status.isSuccess()) {
                // Parse and save the ID card data
                val frontData = jsonResponse.getJSONObject("front_data")

                if (frontData.length() == 0) {
                    // Face extraction failed
                    idUploadStatus = IdUploadStatus.FaceExtractionFailed("Failed to extract face from ID")
                    resetCapture()
                    return
                }


                _idCardData.value = IdCardData(
                    documentType = frontData.optString("document_type", "Unknown"),
                    idNumber = frontData.optString("id_number", "N/A"),
                    fullName = frontData.optString("full_name", "Unknown"),
                    dateOfBirth = frontData.optString("date_of_birth", "N/A"),
                    height = frontData.optString("height", "N/A"),
                    sex = frontData.optString("sex", "N/A"),
                    birthPlace = frontData.optString("birth_place", "N/A"),
                    address = frontData.optString("address", "N/A")
                )

                Log.d("IdUpload", "value: ${_idCardData.value}")


                idUploadStatus = IdUploadStatus.Success("ID card processed successfully")
                viewModelScope.launch {
                    delay(1000)
                    shouldNavigate = true
                }

            } else if (response.status.value == 409) {
                idUploadStatus = IdUploadStatus.FaceExtractionFailed("Failed to extract face from ID")
                resetCapture()
            }

        } catch (e: Exception) {
            Log.e("IdUpload", "Error uploading images", e)
            idUploadStatus = IdUploadStatus.Error("Id Upload failed: ${e.message}")
            resetCapture()
        }
    }

    private fun resetCapture() {
        frontFile = null
        backFile = null
        _currentSide.value = ScanSide.FRONT // Reset to front side
    }

    fun setCurrentSide(side: ScanSide) {
        _currentSide.value = side
    }

    // Function to clear the saved data if needed
    fun clearIdCardData() {
        _idCardData.value = null
    }

    sealed class IdUploadStatus {
        object Idle : IdUploadStatus()
        object Uploading : IdUploadStatus()
        data class Success(val message: String) : IdUploadStatus()
        data class FaceExtractionFailed(val message: String) : IdUploadStatus()
        data class Error(val message: String) : IdUploadStatus()
    }

    private fun startStatusDismissTimer() {
        statusDismissJob?.cancel()
        statusDismissJob = viewModelScope.launch {
            delay(5000)
            idUploadStatus = IdUploadStatus.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
        statusDismissJob?.cancel()
    }
}