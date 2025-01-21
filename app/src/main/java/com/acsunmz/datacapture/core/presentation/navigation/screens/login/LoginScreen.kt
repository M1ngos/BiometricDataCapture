package com.acsunmz.datacapture.core.presentation.navigation.screens.login

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.Layout
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.acsunmz.datacapture.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("unbounded")

val fontFamily = FontFamily(
    Font(
        googleFont = fontName,
        fontProvider = provider,
        weight = FontWeight.Bold,
    )
)

@SuppressLint("InvalidColorHexValue")
@Composable
fun LoginScreen(videoUri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember { context.buildExoPlayer(videoUri) }

    AndroidView(
        factory = { it.buildPlayerView(exoPlayer) },
        modifier = Modifier.fillMaxSize()
    )
    DisposableEffect(
        Unit
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
    Loginform(context)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Loginform(context: Context) {
    val focusManager = LocalFocusManager.current
    var showDatePickerDialog by remember {
        mutableStateOf(false)
    }
    val datePickerState = rememberDatePickerState()
    var selectedDate by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "INATRO",
            fontFamily = fontFamily,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF00) // Neon green
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Portal do Utente",
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("N.º da Carta de Condução/Código") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
//                    backgroundColor = Color.White,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
//        Button(
//            onClick = { showDatePicker.value = true },
//            modifier = Modifier
//                .fillMaxWidth()
////                .background(Color.White)
//            ,
//            colors = ButtonDefaults.filledTonalButtonColors(
//                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
//            )
//        ) {
//            Text(
//                text = date.value.ifEmpty { "Data de nascimento" },
//            )
//        }
        if (showDatePickerDialog) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState
                                .selectedDateMillis?.let { millis ->
                                    selectedDate = millis.toLocalDateFormat()
                                }
                            showDatePickerDialog = false
                        }) {
                        Text(text = "Data de nascimento")
                    }
                }) {
                DatePicker(state = datePickerState)
            }
        }
        TextField(
            value = selectedDate,
            onValueChange = { },
            Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.isFocused) {
                        showDatePickerDialog = true
                        focusManager.clearFocus(force = true)
                    }
                },
            label = {
                Text("dd.mm.aaaa")
            },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Handle login */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x72ff72)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "© 2025 INATRO. Desenvolvido por {placeholder} - {placeholder}.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

    }
}



fun Long.toLocalDateFormat(
    pattern: String = "dd/MM/yyyy"
): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(
        pattern, Locale("pt-pt")
    ).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    return formatter.format(date)
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

