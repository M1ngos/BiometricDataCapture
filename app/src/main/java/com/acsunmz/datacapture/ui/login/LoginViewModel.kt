package com.acsunmz.datacapture.ui.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acsunmz.datacapture.core.data.SessionManager
import com.acsunmz.datacapture.core.model.Driver
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
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class LoginViewModel : ViewModel() {
//    private val _loginStatus = mutableStateOf<LoginUiState>(LoginUiState.Initial)
    private var licenseId by mutableStateOf("")
    private var dateOfBirth by mutableStateOf<Long?>(null)

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken

    private val _uiState = mutableStateOf<LoginUiState>(LoginUiState.Initial)
//    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    var uiState: LoginUiState
        get() = _uiState.value
        set(value) {
            _uiState.value = value
            // Start auto-dismiss timer for relevant statuses
            if (value is LoginUiState.Success ||
                value is LoginUiState.Error ||
                value is LoginUiState.AutoLogin
            ) {
                startStatusDismissTimer()
            }
        }
    private var statusDismissJob: Job? = null

    private val _shouldNavigate = mutableStateOf(false)
    var shouldNavigate: Boolean
        get() = _shouldNavigate.value
        set(value) {
            _shouldNavigate.value = value
        }

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

    sealed class LoginUiState {
        data object Initial : LoginUiState()
        data object Loading : LoginUiState()
        data class Error(val message: String) : LoginUiState()
        data class AutoLogin(val message: String) : LoginUiState()
        data class Success(val message: String) : LoginUiState()
    }

    init {
        // Check if the driver is already logged in
        val driver = SessionManager.getDriver()
        if (driver != null) {
            // If driver exists, attempt login to refresh token
            licenseId = driver.licenseId
            dateOfBirth = driver.dateOfBirth
            _uiState.value = LoginUiState.AutoLogin("Auto login usando credenciais salvas!")
            attemptLogin()
        }
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
        uiState = LoginUiState.Loading
//        if (!isLoginEnabled()) return

        viewModelScope.launch {
            try {
                val response: HttpResponse = client.post("http://192.168.1.144:8000/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        LoginRequest(
                        licenseId = licenseId,
                        dateOfBirth = dateOfBirth ?: 0
                    )
                    )
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        uiState = LoginUiState.Success("Conectado com sucesso")
                        val responseBody = response.bodyAsText()
                        val loginResponse = Json.decodeFromString<LoginResponse>(responseBody)
                        setToken(loginResponse.token)

                        val driver = Driver(
                            id = loginResponse.driver.id,
                            licenseId = loginResponse.driver.licenseId,
                            name = loginResponse.driver.name,
                            dateOfBirth = loginResponse.driver.dateOfBirth,
                            licenceNumber = loginResponse.driver.licenceNumber,
                            issueNumber = loginResponse.driver.issueNumber,
                            expiryDate = loginResponse.driver.expiryDate,
                            placeOfIssue = loginResponse.driver.placeOfIssue,
                            gender = loginResponse.driver.gender,
                            restrictions = loginResponse.driver.restrictions
                        )

                        // Save driver info in SharedPreferences
                        SessionManager.saveDriver(driver)

//                        Log.d("login","OK\n" +
//                                "Credentials:${licenseId} and ${dateOfBirth}")
//                        delay(2000)
                        shouldNavigate = true
                    }
                    HttpStatusCode.Unauthorized -> {
                        uiState=
                            LoginUiState.Error("Credenciais inválidas. Por favor, verifique e tente novamente.")
//                        Log.d("login","Unauthorized\n" +
//                                "Credentials:${licenseId} and ${dateOfBirth}")
                    }
                    else -> {
                        uiState =
                            LoginUiState.Error("Erro no login: ${response.status.description}")
                    }
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error("Erro de conexão: ${e.localizedMessage}")
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

    private fun startStatusDismissTimer() {
        // Cancel any existing timer
        statusDismissJob?.cancel()

        // Start new timer
        statusDismissJob = viewModelScope.launch {
            delay(3000)
            Log.d("uiState", "Resetting to Initial state")
            uiState = LoginUiState.Initial
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
