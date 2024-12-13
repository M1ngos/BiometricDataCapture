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
    object IdScannerScreen



//
//    @Serializable
//    data class AllTasks(val type: String)
//
//    @Serializable
//    data class AddTask(val taskId: Int = -1)
//
//    @Serializable
//    object Calendar
//
//    @Serializable
//    object Statistics
//
//    @Serializable
//    object AllStatistics
//
//    @Serializable
//    data class TaskProgress(val taskId: Int)
}