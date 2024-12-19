package com.acsunmz.datacapture.core.presentation.navigation

import kotlinx.serialization.Serializable

class Destinations {
    @Serializable
    object Onboarding

    @Serializable
    object AppointmentIdScreen

    @Serializable
    object CameraScreen

    @Serializable
    object SignatureScreenWrapper

    @Serializable
    object DocumentScanner

    @Serializable
    object LivenessDetectionScreen

    @Serializable
    object ScannerScreen

    @Serializable
    object ConfirmationScreen

    @Serializable
    object ChooserScreen

    @Serializable
    object SendCaptureDataScreen
}