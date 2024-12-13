package com.acsunmz.datacapture.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.acsunmz.datacapture.core.presentation.navigation.AppNavHost
import androidx.navigation.NavHostController


@SuppressLint("NewApi")
@Composable
fun MainScreen(
    navController: NavHostController,
    onBoardingCompleted: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
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

}