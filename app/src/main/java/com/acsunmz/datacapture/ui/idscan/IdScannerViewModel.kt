package com.acsunmz.datacapture.ui.idscan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acsunmz.datacapture.core.model.IdData
import com.acsunmz.datacapture.core.model.CardSide
import com.acsunmz.datacapture.core.network.ApiResponse
import com.acsunmz.datacapture.core.network.ScanResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File


class IdScanViewModel : ViewModel() {
    sealed class UploadStatus {
        object Idle : UploadStatus()
        object Uploading : UploadStatus()
        data class Success(
            val idData: IdData,
            val faceImage: ByteArray,
            val sessionId: String
        ) : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }

    var uploadStatus by mutableStateOf<UploadStatus>(UploadStatus.Idle)
        private set

//    private val httpClient = HttpClient()
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    private var frontFile: File? = null
    private var backFile: File? = null

    fun setFrontImage(file: File) {
        frontFile = file
    }

    fun setBackImage(file: File) {
        backFile = file
    }
    fun setError(message: String) {
        uploadStatus = UploadStatus.Error(message)
    }
    private fun compressImage(file: File, maxSizeKB: Int = 1024): ByteArray {
        var quality = 90
        var streamLength: Int
        var compressedBytes: ByteArray

        do {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Calculate scaling factor
            val scaleFactor = calculateInSampleSize(options, maxSizeKB)

            // Decode with scaling
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scaleFactor
                inJustDecodeBounds = false
            }

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: throw Exception("Failed to decode image")

            // Compress to output stream
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedBytes = outputStream.toByteArray()
            streamLength = compressedBytes.size

            // Reduce quality for next iteration
            quality -= 10
        } while (streamLength > maxSizeKB * 1024 && quality > 10)

        if (streamLength > maxSizeKB * 1024) {
            throw Exception("Image too large even after compression")
        }

        return compressedBytes
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxSizeKB: Int): Int {
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth
        var inSampleSize = 1

        val targetPixels = maxSizeKB * 1024 * 8 // Rough estimate

        while ((imageWidth * imageHeight) / (inSampleSize * inSampleSize) > targetPixels) {
            inSampleSize *= 2
        }

        return inSampleSize
    }

    suspend fun uploadImages() {
        uploadStatus = UploadStatus.Uploading
        try {
            val frontFile = frontFile ?: throw Exception("No front image captured")
            val compressedImage = compressImage(frontFile)

            val response = httpClient.submitFormWithBinaryData(
                url = "https://api.ocr.space/parse/image",
                formData = formData {
                    append("file", compressedImage, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=front.jpg")
                    })
                    append("apikey", "K82137877388957")
                    append("language", "eng")
                    append("isOverlayRequired", "true")
                    append("filetype", "JPG")
//                    append("language", "por")

                }
            )

            val responseBody = response.bodyAsText()
            println("OCR Response: $responseBody")
            Log.d("OCR_DEBUG", "Full response:\n$responseBody")

            uploadStatus = UploadStatus.Success(
                idData = IdData(
                    idNumber = "",
                    fullName = "",
                    dateOfBirth = "",
                    height = "",
                    sex = "",
                    placeOfBirth = "",
                    address = "",
                    issuanceDate = "",
                    expiryDate = "",
                    maritalStatus = "",
                    fatherName = "",
                    motherName = ""
                ), // Empty data for now
                faceImage = byteArrayOf(),
                sessionId = ""
            )

        } catch (e: Exception) {
            uploadStatus = UploadStatus.Error("Upload failed: ${e.message}")
            Log.e("Upload", "Error: ${e.message}")
        }
    }


}






