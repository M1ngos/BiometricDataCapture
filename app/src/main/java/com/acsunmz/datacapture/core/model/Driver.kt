package com.acsunmz.datacapture.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    @SerialName("id") val id: Int,
    @SerialName("license_id") val licenseId: String,
    @SerialName("name") val name: String,
    @SerialName("date_of_birth") val dateOfBirth: Long,
    @SerialName("licence_number") val licenceNumber: String,
    @SerialName("issue_number") val issueNumber: Int,
    @SerialName("expiry_date") val expiryDate: Long,
    @SerialName("place_of_issue") val placeOfIssue: String,
    @SerialName("gender") val gender: String,
    @SerialName("restrictions") val restrictions: String
)