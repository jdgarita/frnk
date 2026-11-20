package dev.jdgarita.frnk.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

actual fun localizedDateTime(
    instant: Instant,
    skeleton: String
): String {
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)
    return SimpleDateFormat(pattern, locale).format(Date(instant.toEpochMilliseconds()))
}