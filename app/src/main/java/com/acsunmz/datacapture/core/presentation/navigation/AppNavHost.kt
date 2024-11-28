package com.acsunmz.datacapture.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.acsunmz.datacapture.feature.biometrics.camerax.CaptureImageScreen
import com.acsunmz.datacapture.feature.onboarding.AppointmentIdScreen
//import com.acsunmz.datacapture.feature.biometrics.CaptureImageScreen
//import com.acsunmz.datacapture.feature.onboarding.AppointmentIdScreen
import com.acsunmz.datacapture.feature.onboarding.OnboardingScreen
//import com.acsunmz.datacapture.feature.settings.SettingsScreen

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

        composable<Destinations.CaptureImageScreen> {
            CaptureImageScreen(
                navController = navController
            )
        }
//        composable<Destinations.Settings> {
//            SettingsScreen(navController = navController)
//        }

    }

}