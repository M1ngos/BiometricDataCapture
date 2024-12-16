package com.acsunmz.datacapture.core.presentation.navigation

import SignatureScreenWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.LivenessDetectionScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ChooserScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ConfirmationScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.DocumentType
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ScannerScreen
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
        startDestination = Destinations.ChooserScreen
    ) {

        composable<Destinations.Onboarding> {
            OnboardingScreen(
                navController = navController,
            )
        }

        composable<Destinations.AppointmentIdScreen> {
            AppointmentIdScreen(
                navController = navController,
                onContinue = {
                    navController.popBackStack()
                    navController.navigate(Destinations.CameraScreen) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Destinations.CameraScreen> {
            CameraScreen(
                navigate = {
                    navController.navigate(Destinations.DocumentScanner) {
                        popUpTo(Destinations.CameraScreen) { inclusive = true }
                    }
                }
            )
        }


        composable<Destinations.LivenessDetectionScreen> {
            LivenessDetectionScreen(
                onLivenessComplete = {
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
                    navController.navigate(Destinations.SignatureScreenWrapper)
                }
            )
        }

        composable<Destinations.ChooserScreen> {
            ChooserScreen(
                onDocumentTypeSelected = { documentType ->
                    navController.navigate("${Destinations.ScannerScreen}/${documentType.title}")
                }
            )
        }

        composable(
            route = "${Destinations.ScannerScreen}/{documentTypeTitle}",
            arguments = listOf(navArgument("documentTypeTitle") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentTypeTitle = backStackEntry.arguments?.getString("documentTypeTitle")
            val documentType = getDocumentTypeByTitle(documentTypeTitle)
            if (documentType != null) {
                ScannerScreen(documentType = documentType, onScanComplete = { extractedData ->
                    // Handle extracted data
                })
            } else {
                // Handle the case where the document type is not found
                // e.g., navigate back, show an error, etc.
            }
        }
    }
}

fun getDocumentTypeByTitle(title: String?): DocumentType? {
    return when (title) {
        DocumentType.IdCard.title -> DocumentType.IdCard
        DocumentType.Passport.title -> DocumentType.Passport
        DocumentType.ElectionCard.title -> DocumentType.ElectionCard
        else -> null
    }
}
