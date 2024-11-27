package com.acsunmz.datacapture.feature.biometrics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.acsunmz.datacapture.core.presentation.navigation.Destinations

@Composable
fun CaptureImageScreen(
    navController: NavHostController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text("Camera...working on it!")
    }
}