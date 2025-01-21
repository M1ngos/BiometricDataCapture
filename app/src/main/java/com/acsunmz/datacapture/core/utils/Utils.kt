package com.acsunmz.datacapture.core.utils

import android.net.Uri
import com.acsunmz.datacapture.R

fun getVideoUri(): Uri {
    return Uri.parse("android.resource://com.acsunmz.datacapture/${R.raw.video}")
}