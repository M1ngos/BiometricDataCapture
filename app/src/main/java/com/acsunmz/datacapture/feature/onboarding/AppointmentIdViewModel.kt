package com.acsunmz.datacapture.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentIdViewModel : ViewModel() {
    private val _appointmentId = MutableStateFlow("")
    val appointmentId: StateFlow<String> = _appointmentId.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    // Typewriter text parts for the animation
    val typeWriterTextParts = listOf(
        "streamline data capture",
        "improve efficiency",
        "enhance the current process"
    )

    fun setAppointmentId(id: String) {
        viewModelScope.launch {
            _appointmentId.emit(id)
            validateAppointmentId(id)
        }
    }

    private fun validateAppointmentId(id: String) {
        _isError.value = id.isNotEmpty() && (id.length != 5 || !id.all { it.isDigit() })
    }


    fun saveAppointmentId() {
        viewModelScope.launch {
            // TODO: Implement saving logic (e.g., to DataStore or Repository)
            // For now, we'll just print the value
            println("Saving appointment ID: ${appointmentId.value}")
        }
    }

    fun isAppointmentIdValid(): Boolean {
        return appointmentId.value.length == 5 && appointmentId.value.all { it.isDigit() }
    }
}