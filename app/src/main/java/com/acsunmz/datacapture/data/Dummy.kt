package com.acsunmz.datacapture.data

data class Driver(
    val licenseId: String,
    val dob: String
)

data class Appointment(
    val id: String,
    val type: String, // "Renovação" or "Segunda Via"
    val date: String
)


// Mock Repository
object MockRepository {
    private val drivers = listOf(
        Pair("11135480", "24.05.2004"), // licenseId, dob
        Pair("11111111", "20.12.2000")
    )

    private val appointments = listOf(
        Appointment("1234234", "Renovação", "2025-01-01"),
        Appointment("2454364", "Segunda Via", "2025-02-15")
    )

    fun authenticate(licenseId: String, dob: String): Boolean {
        return drivers.any { it.first == licenseId && it.second == dob }
    }

    fun getAppointmentsForDriver(licenseId: String): List<Appointment> {
        // In a real-world app, filter appointments by driver ID
        return appointments
    }
}

