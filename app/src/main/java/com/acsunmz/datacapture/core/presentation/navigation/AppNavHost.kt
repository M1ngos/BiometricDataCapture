package com.acsunmz.datacapture.core.presentation.navigation

import SignatureScreenWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.acsunmz.datacapture.feature.biometrics.camerax.CameraScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.DocumentScannerScreen
import com.acsunmz.datacapture.feature.onboarding.AppointmentIdScreen
import com.acsunmz.datacapture.feature.onboarding.OnboardingScreen

@Composable
fun AppNavHost(
    completedOnboarding: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
//        startDestination = Destinations.Onboarding
//        startDestination = Destinations.CameraScreen
        startDestination = Destinations.DocumentScannerScreen
    ) {
        composable<Destinations.Onboarding> {
            OnboardingScreen(
                navController = navController
            )
        }

        composable<Destinations.AppointmentIdScreen> {
            AppointmentIdScreen(
                navController = navController
            )
        }

        composable<Destinations.CameraScreen> {
            CameraScreen(
                navController = navController
            )
        }

        composable<Destinations.SignatureScreenWrapper> {
            SignatureScreenWrapper(
                navController = navController
            )
        }

        composable<Destinations.DocumentScannerScreen> {
            DocumentScannerScreen(
                navController = navController,
                onDocumentScanned = { scannedUri ->
                    // Navigate to preview or process scanned document
//                    navController.navigate("document_preview/${scannedUri}")
                }
            )
        }

    }

}