package com.acsunmz.datacapture.core.model

import android.graphics.Rect
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

enum class CardSide {
    FRONT,
    BACK
}

enum class ScanState {
    START,
    FRONT_COMPLETE,
    BACK_COMPLETE
}

@Serializable
data class IdData(
    val idNumber: String,
    val fullName: String,
    val dateOfBirth: String,
    val height: String,
    val sex: String,
    val placeOfBirth: String,
    val address: String,
    val issuanceDate: String,
    val expiryDate: String,
    val maritalStatus: String,
    val fatherName: String,
    val motherName: String
)
