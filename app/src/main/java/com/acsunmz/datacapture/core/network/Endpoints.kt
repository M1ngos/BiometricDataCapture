package com.acsunmz.datacapture.core.network


object UrlProvider {
    const val BASE_URL = "http://192.168.1.144:8000"
    const val LOGIN_URL = "$BASE_URL/auth/login"
    const val APPOINTMENTS_URL = "$BASE_URL/driver/appointments"
    const val UPLOAD_LIVE_URL = "$BASE_URL/upload"
    const val UPLOAD_ID_URL = "$BASE_URL/extract-id-data"
    const val SEND_CAPTURE_DATA_URL = "$BASE_URL/capture"
}