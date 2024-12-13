package com.acsunmz.datacapture.core.presentation.navigation

import SignatureScreenWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.IdScannerScreen
//import com.acsunmz.datacapture.feature.biometrics.camerax.CameraLivenessScreen
//import com.acsunmz.datacapture.feature.biometrics.camerax.CameraScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.LivenessDetectionScreen
import com.acsunmz.datacapture.feature.docscanner.DocumentScanner
import com.acsunmz.datacapture.feature.onboarding.AppointmentIdScreen
import com.acsunmz.datacapture.feature.onboarding.OnboardingScreen

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppNavHost(
    completedOnboarding: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Destinations.Onboarding
//        startDestination = Destinations.CameraScreen
//            startDestination = Destinations.LivenessDetectionScreen
//        startDestination = Destinations.DocumentScanner
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
                navController = navController,
            )
        }


        composable<Destinations.LivenessDetectionScreen> {
            LivenessDetectionScreen(
                onLivenessComplete = {
                    // Navigate to next screen or perform next action
                    navController.navigate(Destinations.LivenessDetectionScreen)
                }
            )
        }

        composable<Destinations.SignatureScreenWrapper> {
            SignatureScreenWrapper(
                navController = navController
            )
        }

        composable<Destinations.DocumentScanner> {
            DocumentScanner (
                navController = navController,
                onDocumentScanned = { scannedUri ->
//                     Navigate to preview or process scanned document
//                        navController.navigate("document_preview/${scannedUri}")
                    navController.navigate(Destinations.SignatureScreenWrapper)
                }
            )
        }

        composable<Destinations.IdScannerScreen> {
            IdScannerScreen(
                onStart  = {},
                onCancel = {}
            )
        }

    }

}