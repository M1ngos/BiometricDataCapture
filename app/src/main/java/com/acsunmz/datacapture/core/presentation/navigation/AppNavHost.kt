package com.acsunmz.datacapture.core.presentation.navigation

import IdScanner
import SignatureScreenWrapper
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.acsunmz.datacapture.MainActivity
import com.acsunmz.datacapture.core.presentation.navigation.screens.SendCaptureDataScreen
import com.acsunmz.datacapture.core.presentation.navigation.screens.login.LoginScreen
import com.acsunmz.datacapture.core.utils.getVideoUri
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.LivenessDetectionScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ChooserScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ConfirmationScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.DocumentType
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ScannerScreen
import com.acsunmz.datacapture.feature.docscanner.DocumentScanner
import com.acsunmz.datacapture.feature.onboarding.AppointmentIdScreen
import com.acsunmz.datacapture.feature.onboarding.OnboardingScreen
import kotlin.system.exitProcess

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
        startDestination = Destinations.LoginScreen
    ) {
        composable<Destinations.Onboarding> {
            OnboardingScreen(
                navController = navController,
            )
        }

        composable<Destinations.LoginScreen> {
            LoginScreen(
                getVideoUri()
            )
        }

        composable<Destinations.AppointmentIdScreen> {
            AppointmentIdScreen(
                navController = navController,
                onContinue = {
                    navController.navigate(Destinations.CameraScreen) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Destinations.CameraScreen> {
            CameraScreen(
                navigate = {
                    navController.navigate(Destinations.SignatureScreenWrapper) {
                        popUpTo(Destinations.CameraScreen) { inclusive = true }
                    }
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

        composable<Destinations.IdScanner> {
            IdScanner (
                 onScanComplete = {
                     navController.navigate(Destinations.CameraScreen)
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
            DocumentScanner(
                navController = navController,
                onDocumentScanned = { scannedUri ->
                    navController.navigate(Destinations.SendCaptureDataScreen)
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
                ScannerScreen(
                    documentType = documentType,
                    onDocumentProcessed = { result ->
                        result.forEach { (key, value) ->
                            println("$key: $value")
                            Log.d("scanner-results","${key}:${value}")
                        }
                        navController.navigate(Destinations.SendCaptureDataScreen)
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable<Destinations.SendCaptureDataScreen> {
            SendCaptureDataScreen(
                onQuit = {
                    val activity = MainActivity()
                    activity.finish()
                    exitProcess(0)
                }
            )
        }
    }
}

fun getDocumentTypeByTitle(title: String?): DocumentType? {
    return when (title) {
        DocumentType.MozambicanID.title -> DocumentType.MozambicanID
        DocumentType.Passport.title -> DocumentType.Passport
        DocumentType.ElectionCard.title -> DocumentType.ElectionCard
        else -> null
    }
}
