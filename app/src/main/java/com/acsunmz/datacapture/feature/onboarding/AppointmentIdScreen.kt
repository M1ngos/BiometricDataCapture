package com.acsunmz.datacapture.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun AppointmentIdScreen(
    navController: NavController,
    viewModel: AppointmentIdViewModel = viewModel(),
    onContinue: () -> Unit
) {
    val appointmentId by viewModel.appointmentId.collectAsState()
    val isError by viewModel.isError.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    AppointmentIdScreenContent(
        appointmentId = appointmentId,
        isError = isError,
        typeWriterTextParts = viewModel.typeWriterTextParts,
        onAppointmentIdChange = viewModel::setAppointmentId,
        onClickContinue = {
            if (viewModel.isAppointmentIdValid()) {
                keyboardController?.hide()
                viewModel.saveAppointmentId()
                onContinue()
            }
        }
    )
}

@Composable
fun AppointmentIdScreenContent(
    appointmentId: String,
    isError: Boolean,
    typeWriterTextParts: List<String>,
    onAppointmentIdChange: (String) -> Unit,
    onClickContinue: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            TypewriterText(
                baseText = "This prototype aims to ",
                parts = typeWriterTextParts
            )
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
            Text(
                text = "Please input your appointment Id:",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 20.sp
                )
            )
        }

        item {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            AppointmentIdField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                appointmentId = appointmentId,
                isError = isError,
                onIdChange = onAppointmentIdChange,
                onClickDone = onClickContinue
            )
        }

        item {
            AnimatedVisibility(
                visible = appointmentId.length == 5 && !isError
            ) {
                Column {
                    Spacer(modifier = Modifier.height(56.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        onClick = onClickContinue
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AppointmentIdField(
    modifier: Modifier,
    appointmentId: String,
    isError: Boolean,
    onIdChange: (String) -> Unit,
    onClickDone: () -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = appointmentId,
        onValueChange = { newValue ->
            // Only allow numeric input and limit to 5 digits
            if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 5)) {
                onIdChange(newValue)
            }
        },
        label = { Text("Appointment ID") },
        placeholder = { Text("Enter 5-digit ID") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onClickDone() }
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = "Please enter a valid 5-digit ID",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        ),
        singleLine = true,
        visualTransformation = VisualTransformation.None
    )
}


@Composable
private fun TypewriterText(modifier: Modifier = Modifier, baseText: String, parts: List<String>) {
    var partIndex by remember { mutableStateOf(0) }
    var partText by remember { mutableStateOf("") }
    val textToDisplay = "$baseText $partText"
    Text(
        modifier = modifier,
        text = textToDisplay,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            letterSpacing = -(1.6).sp,
            lineHeight = 32.sp,
        ),
    )

    LaunchedEffect(key1 = parts) {
        while (partIndex <= parts.size) {
            val part = parts[partIndex]

            part.forEachIndexed { charIndex, _ ->
                partText = part.substring(startIndex = 0, endIndex = charIndex + 1)
                delay(100)
            }

            delay(1500)

            part.forEachIndexed { charIndex, _ ->
                partText = part
                    .substring(startIndex = 0, endIndex = part.length - (charIndex + 1))
                delay(30)
            }

            delay(500)

            partIndex = (partIndex + 1) % parts.size
        }
    }
}



