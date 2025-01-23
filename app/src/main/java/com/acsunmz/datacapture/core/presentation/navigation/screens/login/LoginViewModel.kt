package com.acsunmz.datacapture.core.presentation.navigation.screens.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraViewModel.UploadStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class LoginViewModel : ViewModel() {
    private var licenseId by mutableStateOf("")
    private var dateOfBirth by mutableStateOf<Long?>(null)

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var statusDismissJob: Job? = null

    var shouldNavigate by mutableStateOf(false)
        internal set

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    @Serializable
    private data class LoginRequest(
        @SerialName("license_id") val licenseId: String,
        @SerialName("date_of_birth") val dateOfBirth: Long
    )

    @Serializable
    data class LoginResponse(
        @SerialName("token") val token: String,
        @SerialName("driver") val driver: Driver
    )

    @Serializable
    data class Driver(
        @SerialName("id") val id: Int,
        @SerialName("license_id") val licenseId: String,
        @SerialName("name") val name: String,
        @SerialName("date_of_birth") val dateOfBirth: Long
    )

    sealed class LoginUiState {
        data object Initial : LoginUiState()
        data object Loading : LoginUiState()
        data class Error(val message: String) : LoginUiState()
        data class Success(val message: String) : LoginUiState()
    }


    fun setToken(token: String) {
        _authToken.value = token
    }

    fun clearToken() {
        _authToken.value = null
    }

    fun updateLicenseId(id: String) {
        licenseId = id
        _uiState.value = LoginUiState.Initial // Reset error state when input changes
    }

    fun updateDateOfBirth(date: Long) {
        dateOfBirth = date
        _uiState.value = LoginUiState.Initial // Reset error state when input changes
    }


    fun isLoginEnabled(): Boolean {
        return licenseId.length == 8 && dateOfBirth != null && _uiState.value !is LoginUiState.Loading
    }

    fun attemptLogin() {
        if (!isLoginEnabled()) return

        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading

                val response: HttpResponse = client.post("http://192.168.1.144:8000/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(
                        licenseId = licenseId,
                        dateOfBirth = dateOfBirth ?: 0
                    ))
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        _uiState.value = LoginUiState.Success("Conectado com sucesso")
                        val responseBody = response.bodyAsText()
                        val loginResponse = Json.decodeFromString<LoginResponse>(responseBody)
                        setToken(loginResponse.token)

//                        Log.d("login","OK\n" +
//                                "Credentials:${licenseId} and ${dateOfBirth}")
                        delay(2000)
                        shouldNavigate = true
                    }
                    HttpStatusCode.Unauthorized -> {
                        _uiState.value = LoginUiState.Error("Credenciais inválidas. Por favor, verifique e tente novamente.")
//                        Log.d("login","Unauthorized\n" +
//                                "Credentials:${licenseId} and ${dateOfBirth}")
                    }
                    else -> {
                        _uiState.value = LoginUiState.Error("Erro no login: ${response.status.description}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Erro de conexão: ${e.localizedMessage}")
            }
        }
    }

    fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
        Log.d("auth_token","saved!")
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("auth_token").apply()
    }


    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
