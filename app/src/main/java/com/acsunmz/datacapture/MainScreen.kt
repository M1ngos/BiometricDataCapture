package com.acsunmz.datacapture

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.acsunmz.datacapture.core.navigation.AppNavHost
import androidx.navigation.NavHostController


@SuppressLint("NewApi")
@Composable
fun MainScreen(
    navController: NavHostController,
    onBoardingCompleted: Boolean,
) {
    Scaffold(
        content = { innerPadding ->
            AppNavHost(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                completedOnboarding = onBoardingCompleted,
            )
        }
    )
}