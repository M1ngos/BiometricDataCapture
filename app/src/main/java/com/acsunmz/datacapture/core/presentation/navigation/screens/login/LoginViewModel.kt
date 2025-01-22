import com.acsunmz.datacapture.data.Appointment
import com.acsunmz.datacapture.data.AppointmentType
import com.acsunmz.datacapture.data.Driver

//package com.acsunmz.datacapture.core.presentation.navigation.screens.login
//
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState = _loginState.asStateFlow()

    private var currentLicenseId: String = ""
    private var currentDateOfBirth: Long? = null

    // Simulated database of valid drivers
    private val validDrivers = listOf(
        Driver("12345678", 802947600000, "João Silva"), // DOB: 1995-06-12
        Driver("87654321", 686361600000, "Maria Santos"), // DOB: 1991-09-25
        Driver("11223344", 749779200000, "Pedro Oliveira") // DOB: 1993-10-05
    )

    // Simulated appointments
    private val appointments = listOf(
        Appointment("1", AppointmentType.RENOVACAO, 1706832000000, "09:00", "12345678"), // 2024-02-01
        Appointment("2", AppointmentType.SEGUNDA_VIA, 1706918400000, "14:30", "87654321"), // 2024-02-02
        Appointment("3", AppointmentType.RENOVACAO, 1707004800000, "11:15", "12345678"), // 2024-02-03
    )


    fun updateLicenseId(licenseId: String) {
        currentLicenseId = licenseId
    }

    fun updateDateOfBirth(date: Long?) {
        currentDateOfBirth = date
    }

    fun isLoginEnabled(): Boolean {
        return currentLicenseId.length == 8 && currentDateOfBirth != null
    }

    fun attemptLogin() {
        val dateOfBirth = currentDateOfBirth
        if (dateOfBirth == null) {
            _loginState.value = LoginState.Error("Please select your date of birth")
            return
        }

        val driver = validDrivers.find {
            it.licenseId == currentLicenseId && it.dateOfBirth == dateOfBirth
        }

        if (driver != null) {
            val driverAppointments = appointments.filter { it.driverId == currentLicenseId }
            _loginState.value = LoginState.Success(driver, driverAppointments)
        } else {
            _loginState.value = LoginState.Error("Invalid credentials")
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    data class Success(val driver: Driver, val appointments: List<Appointment>) : LoginState()
    data class Error(val message: String) : LoginState()
}
