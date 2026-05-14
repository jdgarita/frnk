package dev.jdgarita.frnk.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Generic, locale-agnostic ISO-style formatter. Host apps can wrap if they need richer formatting. */
object DateTimeFormat {
    fun isoDate(instant: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String =
        instant.toLocalDateTime(tz).date.toString()
}
