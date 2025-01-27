package com.acsunmz.datacapture.core.presentation.navigation

import IdScanner
import SignatureScreenWrapper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.acsunmz.datacapture.MainActivity
import com.acsunmz.datacapture.core.data.SessionManager
import com.acsunmz.datacapture.core.presentation.screens.SendCaptureDataScreen
import com.acsunmz.datacapture.ui.login.LoginScreen
import com.acsunmz.datacapture.core.utils.getVideoUri
import com.acsunmz.datacapture.feature.biometrics.camerax.capture.CameraScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.LivenessDetectionScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ConfirmScan
import com.acsunmz.datacapture.ui.documents.ChooserScreen
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.DocumentType
import com.acsunmz.datacapture.feature.biometrics.camerax.idscan.ScannerScreen
import com.acsunmz.datacapture.feature.docscanner.DocumentScanner
import com.acsunmz.datacapture.feature.onboarding.AppointmentIdScreen
import com.acsunmz.datacapture.feature.onboarding.OnboardingScreen
import com.acsunmz.datacapture.ui.appoinments.AppointmentsScreen
import com.acsunmz.datacapture.ui.documents.IdConfirmationScreen
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
                getVideoUri(),
                navigate = {
                    navController.navigate(Destinations.AppointmentListScreen) {
                        popUpTo(Destinations.LoginScreen) { inclusive = true }
                    }
                }
            )
        }

        composable<Destinations.AppointmentListScreen> {
            AppointmentsScreen(
                navController = navController,
                onLogout = {
                    SessionManager.clearSession()
                    navController.navigate(Destinations.LoginScreen){
                        launchSingleTop = true
                    }
                },
                onAppointmentClick = {
                    navController.navigate(Destinations.ChooserScreen)
                }
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

        composable<Destinations.IdScanner> {
            IdScanner(
                onScanComplete = {
                    navController.navigate(Destinations.ConfirmScan)
                }
            )
        }

        composable<Destinations.ConfirmScan> {
            ConfirmScan(
                onProceed = {
                    navController.navigate(Destinations.DocumentScanner)
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
                onDocumentScanned = {
                    navController.navigate(Destinations.CameraScreen)
                }
            )
        }

        composable<Destinations.ChooserScreen> {
            ChooserScreen(
                navController = navController,
                onDocumentTypeSelected = { documentType ->
//                    navController.navigate("${Destinations.ScannerScreen}/${documentType.title}")
                    navController.navigate(Destinations.IdScanner)
                },
                onBackPress = {}
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
