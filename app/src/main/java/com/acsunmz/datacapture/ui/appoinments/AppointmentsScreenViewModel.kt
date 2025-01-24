package com.acsunmz.datacapture.core.presentation.screens.appoinments

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.acsunmz.datacapture.core.model.Appointment
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AppointmentsScreenViewModel(context: Context) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val _appointments = mutableStateOf<List<Appointment>>(emptyList())
    val appointments: State<List<Appointment>> = _appointments

    // Fetch appointments directly in the ViewModel
    fun fetchAppointments() {
        // Retrieve the token from SharedPreferences
        val token = sharedPreferences.getString("auth_token", null)

        if (token.isNullOrEmpty()) {
            Log.e("AppointmentsViewModel", "Token not found!")
            return
        }

        viewModelScope.launch {
            try {
                val fetchedAppointments = getAppointmentsFromApi(token)
                _appointments.value = fetchedAppointments
            } catch (e: Exception) {
                Log.e("AppointmentsViewModel", "Error fetching appointments", e)
            }
        }
    }

    // Make the API call directly from the ViewModel
    private fun getAppointmentsFromApi(token: String): List<Appointment> {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://your-api-url.com/appointments")
            .addHeader("Authorization", "Bearer $token")
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Parse the response to your model
                val responseBody = response.body?.string()
                // Assuming you have a way to convert the response to List<Appointment>
                // For simplicity, let's return an empty list
                listOf<Appointment>()
            } else {
                Log.e("AppointmentsViewModel", "Failed to fetch data: ${response.message}")
                emptyList()
            }
        } catch (e: IOException) {
            Log.e("AppointmentsViewModel", "Network error: ${e.message}")
            emptyList()
        }
    }
}
