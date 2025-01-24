package com.acsunmz.datacapture.ui.appoinments

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.acsunmz.datacapture.core.model.Appointment
import com.acsunmz.datacapture.core.model.AppointmentsResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AppointmentsScreenViewModel(context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val _appointments = mutableStateOf<List<Appointment>>(emptyList())
    val appointments: State<List<Appointment>> = _appointments

    private val gson = Gson()
    private val client = OkHttpClient()

    fun fetchAppointments() {
        Log.d("getAppointmentsFromApi", "debug1")
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

    private suspend fun getAppointmentsFromApi(token: String): List<Appointment> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AppointmentsViewModel", "Fetching appointments from API")
                val request = Request.Builder()
                    .url("http://192.168.1.209:8000/driver/appointments")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.e("AppointmentsViewModel", "response: $responseBody")
                    if (!responseBody.isNullOrEmpty()) {
                        // Parse the response into a wrapper class
                        val wrapperType = object : TypeToken<AppointmentsResponse>() {}.type
                        val parsedResponse: AppointmentsResponse = gson.fromJson(responseBody, wrapperType)

                        // Return the list of appointments
                        parsedResponse.appointments ?: emptyList()
                    } else {
                        Log.e("AppointmentsViewModel", "Empty response body")
                        emptyList()
                    }
                } else {
                    Log.e("AppointmentsViewModel", "API call failed: ${response.message}")
                    emptyList()
                }
            } catch (e: IOException) {
                Log.e("AppointmentsViewModel", "Network error: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e("AppointmentsViewModel", "Unexpected error: ${e.message}")
                emptyList()
            }
        }
    }


}