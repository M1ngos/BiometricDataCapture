package com.acsunmz.datacapture.data

data class Driver(
    val licenseId: String,
    val dateOfBirth: Long,
    val name: String
)

data class Appointment(
    val id: String,
    val type: AppointmentType,
    val date: Long,
    val time: String,
    val driverId: String,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED
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

