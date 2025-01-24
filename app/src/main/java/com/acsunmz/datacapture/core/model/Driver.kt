package com.acsunmz.datacapture.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    @SerialName("id") val id: Int,
    @SerialName("license_id") val licenseId: String,
    @SerialName("name") val name: String,
    @SerialName("date_of_birth") val dateOfBirth: Long
)
