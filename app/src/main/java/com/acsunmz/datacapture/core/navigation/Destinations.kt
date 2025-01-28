package com.acsunmz.datacapture.core.navigation

import kotlinx.serialization.Serializable

class Destinations {
    @Serializable
    object Onboarding

    @Serializable
    object LoginScreen

    @Serializable
    object AppointmentListScreen

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
    object IdScanner

    @Serializable
    object ConfirmScan

    @Serializable
    object IdConfirmationScreen

    @Serializable
    object ConfirmationScreen

    @Serializable
    object ChooserScreen

    @Serializable
    object SendCaptureDataScreen
}