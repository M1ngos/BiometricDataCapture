package com.acsunmz.datacapture.core.presentation.navigation.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acsunmz.datacapture.R
import com.acsunmz.datacapture.core.network.SendCaptureDataViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SendCaptureDataScreen(
    viewModel: SendCaptureDataViewModel = viewModel(),
    onQuit: () -> Unit
 ) {
    var isPhotoExist by remember { mutableStateOf(false) }
    var isSignatureExist by remember { mutableStateOf(false) }

    val context = LocalContext.current
    (LocalContext.current as? Activity)
    val coroutineScope = rememberCoroutineScope()

    val photoFile = File(context.cacheDir, "captured_image.jpg")
    val signatureFile = File(context.cacheDir, "signature.png")


    LaunchedEffect(Unit) {
        isPhotoExist = photoFile.exists()
        isSignatureExist = signatureFile.exists()
    }

    val result = when (viewModel.uploadStatus) {
        is SendCaptureDataViewModel.UploadStatus.Success -> true
        else -> false
    }

    LaunchedEffect(viewModel.terminate) {
        if(viewModel.terminate && result ) {
            onQuit()
        }
    }

    val message = when (val status = viewModel.uploadStatus) {
        is SendCaptureDataViewModel.UploadStatus.Success -> status.message
        is SendCaptureDataViewModel.UploadStatus.Error -> status.message
        else -> null
    }

    message?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val status = viewModel.uploadStatus) {
                is SendCaptureDataViewModel.UploadStatus.Uploading -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Uploading...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                is SendCaptureDataViewModel.UploadStatus.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                status.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                is SendCaptureDataViewModel.UploadStatus.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.error_24dp),
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                status.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                else -> {}
            }
            Row {
                Text(
                    text = "Captured data:",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .weight(2f)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }

            FileStatusRow(label = "Photo", isFileExist = isPhotoExist)
            Spacer(modifier = Modifier.height(8.dp))
            FileStatusRow(label = "Signature", isFileExist = isSignatureExist)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.uploadCaptureData(photoFile,signatureFile)
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    )
                ) {
                 Icon(
                     painter = painterResource(R.drawable.ic_submit),
                     contentDescription = "Take Photo",
                     modifier = Modifier.size(24.dp),
                     tint = MaterialTheme.colorScheme.onPrimaryContainer
                 )
                }
            }
        }
    }
}

@Composable
fun FileStatusRow(label: String, isFileExist: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = if (isFileExist) Color.Green else Color.Gray,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
        )
    }
}

@Preview
@Composable
fun FileStatusRowPreview(label: String = "Image", isFileExist: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = if (isFileExist) Color.Green else Color.Gray,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
        )
    }
}
