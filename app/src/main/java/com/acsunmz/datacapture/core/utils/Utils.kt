package com.acsunmz.datacapture.core.utils

import android.net.Uri

import com.acsunmz.datacapture.R
import com.acsunmz.datacapture.core.data.SessionManager
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun getVideoUri(): Uri {
    return Uri.parse("android.resource://com.acsunmz.datacapture/${R.raw.video}")
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun displayFormattedDate(epoch: Long): String? {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = Instant.ofEpochSecond(epoch)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
    return formattedDate
}

// Utility function to format date input
private fun formatDateInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return buildString {
        digitsOnly.forEachIndexed { index, char ->
            if (index == 2 || index == 4) append('.')
            append(char)
        }
    }
}