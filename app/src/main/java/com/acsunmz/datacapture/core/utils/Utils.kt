package com.acsunmz.datacapture.core.utils

import android.net.Uri

import com.acsunmz.datacapture.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getVideoUri(): Uri {
    return Uri.parse("android.resource://com.acsunmz.datacapture/${R.raw.video}")
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
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