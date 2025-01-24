package com.acsunmz.datacapture.core.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: Int,
    val type: AppointmentType,
    val status: AppointmentStatus,
    val date: Long,
    val time: String,
    @SerializedName("license_id") val licenseId: String
)


data class AppointmentsResponse(
    val appointments: List<Appointment>?
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
