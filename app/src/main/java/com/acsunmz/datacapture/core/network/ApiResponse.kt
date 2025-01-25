package com.acsunmz.datacapture.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val data: ResponseData
)

@Serializable
data class ResponseData(
    @SerialName("front_text") val frontText: String,
    @SerialName("back_text") val backText: String,
    @SerialName("face_image_url") val faceImageUrl: String
)

data class ScanResult(
    val frontText: String,
    val backText: String,
    val faceImage: ByteArray,
    val sessionId: String
)

enum class ScanSide {
    FRONT, BACK
}