//package com.acsunmz.datacapture.core.presentation.navigation.screens.login
//
//import android.content.Context
//import android.widget.Toast
//import androidx.lifecycle.ViewModel
//import com.acsunmz.datacapture.data.Appointment
//import com.acsunmz.datacapture.data.MockRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//
//class LoginViewModel : ViewModel() {
//    private val _loginSuccess = MutableStateFlow(false)
//    val loginSuccess: StateFlow<Boolean> = _loginSuccess
//
//    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
//    val appointments: StateFlow<List<Appointment>> = _appointments
//
//    fun authenticate(context: Context, licenseId: String, dob: String) {
//        if (MockRepository.authenticate(licenseId, dob)) {
//            _loginSuccess.value = true
//            _appointments.value = MockRepository.getAppointmentsForDriver(licenseId)
//        } else {
//            Toast.makeText(context, "Invalid credentials!", Toast.LENGTH_SHORT).show()
//        }
//    }
//}