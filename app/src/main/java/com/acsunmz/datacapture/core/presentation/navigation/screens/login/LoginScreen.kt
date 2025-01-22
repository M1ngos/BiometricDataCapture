package com.acsunmz.datacapture.core.presentation.navigation.screens.login

import LoginViewModel
import android.content.Context
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acsunmz.datacapture.R
import com.acsunmz.datacapture.data.Appointment
import com.acsunmz.datacapture.data.AppointmentStatus
import com.acsunmz.datacapture.data.AppointmentType
import com.acsunmz.datacapture.data.Driver
import com.acsunmz.datacapture.ui.components.DatePickerFieldToModal
import com.acsunmz.datacapture.ui.theme.Shapes
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    videoUri: Uri,
    onLoginSuccess: (Driver, List<Appointment>) -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.loginState.collectAsState()

    val focusManager = LocalFocusManager.current
    val exoPlayer = remember { context.buildExoPlayer(videoUri) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val fontName = GoogleFont("Unbounded")

    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    val fontFamily = FontFamily(
        Font(googleFont = fontName, fontProvider = provider)
    )

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                onLoginSuccess(state.driver, state.appointments)
            }
            is LoginState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> { /* Initial state, do nothing */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { it.buildPlayerView(exoPlayer) },
            modifier = Modifier
                .fillMaxSize()
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = "INATRO",
                    fontFamily = fontFamily,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextInput(
                inputType = InputType.DriverLicense,

                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                onValueChange = { licenseId ->
                    viewModel.updateLicenseId(licenseId)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DatePickerFieldToModal(
                onDateSelected = { date ->
                    selectedDate = date
                    viewModel.updateDateOfBirth(date)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.attemptLogin() },
                enabled = viewModel.isLoginEnabled(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Entrar", Modifier.padding(vertical = 8.dp))
            }

//            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.height(60.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "© 2025 INATRO. Desenvolvido por {placeholder}.",
                    color = Color.White,
                    fontSize = 12.sp
                )
//                TextButton(onClick = {}) {
//                    Text("Portal gestor", fontSize = 12.sp)
//                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

sealed class InputType(
    val label: String,
    val icon: ImageVector,
    val keyboardOptions: KeyboardOptions,
    val visualTransformation: VisualTransformation
) {
    object DriverLicense : InputType(
        label = "N.º da Carta de Condução/Código",
        icon = Icons.Default.CreditCard,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        visualTransformation = VisualTransformation.None
    )
}

@Composable
fun TextInput(
    inputType: InputType,
    focusRequester: FocusRequester? = null,
    keyboardActions: KeyboardActions,
    onValueChange: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }

    TextField(
        value = value,
        onValueChange = { input ->
            if (input.length <= 8 && input.all { it.isDigit() }) {
                value = input
                onValueChange(input)
            }},
        modifier = Modifier
            .fillMaxWidth()
            .focusOrder(focusRequester ?: FocusRequester()),
        leadingIcon = { Icon(imageVector = inputType.icon, null) },
        label = { Text(text = inputType.label) },
        shape = Shapes.small,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = inputType.keyboardOptions,
        visualTransformation = inputType.visualTransformation,
        keyboardActions = keyboardActions
    )
}

private fun Context.doLogin() {
    Toast.makeText(
        this,
        "Something went wrong, try again later!",
        Toast.LENGTH_SHORT
    ).show()
}

private fun Context.buildExoPlayer(uri: Uri) =
    ExoPlayer.Builder(this).build().apply {
        setMediaItem(MediaItem.fromUri(uri))
        repeatMode = Player.REPEAT_MODE_ALL
        playWhenReady = true
        prepare()
    }

private fun Context.buildPlayerView(exoPlayer: ExoPlayer) =
    StyledPlayerView(this).apply {
        player = exoPlayer
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        useController = false
        resizeMode = RESIZE_MODE_ZOOM
    }



// Mock


@Composable
fun AppointmentsList(
    appointments: List<Appointment>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Suas Consultas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (appointments.isEmpty()) {
            Text(
                "Nenhuma consulta agendada",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        } else {
            appointments.forEach { appointment ->
                AppointmentItem(appointment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = when (appointment.type) {
                    AppointmentType.RENOVACAO -> "Renovação"
                    AppointmentType.SEGUNDA_VIA -> "Segunda Via"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Data: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(appointment.date))}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Horário: ${appointment.time}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Status: ${appointment.status.name.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = when (appointment.status) {
                    AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
                    AppointmentStatus.COMPLETED -> Color.Green
                    AppointmentStatus.CANCELLED -> Color.Red
                }
            )
        }
    }
}