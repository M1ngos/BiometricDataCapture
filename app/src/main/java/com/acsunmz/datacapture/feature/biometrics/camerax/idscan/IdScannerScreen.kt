package com.acsunmz.datacapture.feature.biometrics.camerax.idscan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun IdScannerScreen(
    viewModel: IdScannerViewModel = viewModel(),
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.currentPhotoUri?.let { uri ->
                viewModel.processImage(uri, context)
            }
        }
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) -> {
                hasPermission = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                viewModel.startCamera(context, cameraLauncher)
            },
            enabled = hasPermission
        ) {
            Text("Scan ID Document")
        }

        if (viewModel.isProcessing) {
            CircularProgressIndicator()
        }

        // Display extracted text
        viewModel.extractedText?.let { text ->
            Text(
                text = "Extracted Text:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = text)
        }

        // Display any error messages
        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(onClick = {
                if (!viewModel.extractedText.isNullOrEmpty()) {
//                    onConfirm()
                }
            }, modifier = Modifier.weight(1f)) {
                Text("Confirm")
            }
        }
    }
}