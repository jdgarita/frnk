package dev.jdgarita.frnk.utils

import android.os.Build

actual object PlatformInfo {
    actual val osName: String = "Android"

    actual val osVersion: String =
        Build.VERSION.RELEASE?.takeIf { it.isNotBlank() } ?: "API ${Build.VERSION.SDK_INT}"

    actual val deviceModel: String =
        listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
            .ifBlank { "Android device" }
}