package dev.jdgarita.frnk.utils

import kotlin.time.Instant

/**
 * [instant] in the device's locale and time zone through a CLDR skeleton (`EEEMMMd`, `jmm`): the
 * platform picks the order, the separators and the 12/24-hour clock, so "Thu, Nov 19" reads
 * "jue, 19 nov" in Spanish and "11月19日(木)" in Japanese with no format string of the host's own.
 * Android uses `DateFormat.getBestDateTimePattern`, iOS `setLocalizedDateFormatFromTemplate`.
 * Upstreamed from Faint's trial dates.
 */
expect fun localizedDateTime(
    instant: Instant,
    skeleton: String
): String