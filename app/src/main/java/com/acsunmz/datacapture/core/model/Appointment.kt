package com.acsunmz.datacapture.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: Int,
    val type: AppointmentType,
    val status: AppointmentStatus,
    val date: Long, // Unix timestamp
    val time: String,
    val licenseId: String
)

enum class AppointmentType {
    RENOVACAO,
    SEGUNDA_VIA
}

enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}
