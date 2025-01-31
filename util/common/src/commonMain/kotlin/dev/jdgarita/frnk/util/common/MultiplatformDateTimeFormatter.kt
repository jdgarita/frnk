package dev.jdgarita.frnk.util.common

import kotlin.js.JsName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * Originally called [DateFormatter] - however this conflicts with Foundation.DateFormatter in iOS tests
 */
interface MultiplatformDateTimeFormatter {

    /**
     * Formats the given [instant] using the given [pattern].
     * Supported patterns:
     * - dd: day of month, 2 digits with leading zeros
     * - d: day of month, no leading zeros
     * - MM: month of year, 2 digits with leading zeros
     * - M: month of year, no leading zeros
     * - MMMM: month of year, full name
     * - yyyy: year, 4 digits
     * - yy: year, last 2 digits
     * @param instant the instant to format
     * @param pattern the pattern to use
     */
    @JsName("formatWithInstant")
    fun format(instant: Instant, dateFormat: DateFormat, timeZone: TimeZone = TimeZone.currentSystemDefault()): String

    /**
     * Formats the given [year], [month], and [day] using the given [pattern].
     * Supported patterns:
     * - dd: day of month, 2 digits with leading zeros
     * - d: day of month, no leading zeros
     * - MM: month of year, 2 digits with leading zeros
     * - M: month of year, no leading zeros
     * - MMMM: month of year, full name
     * - yyyy: year, 4 digits
     * - yy: year, last 2 digits
     * @param year year of date to format
     * @param month month of date to format
     * @param day day of date to format
     * @param pattern the pattern to use
     */
    @JsName("formatWithYMD")
    fun format(year: Int, month: Int, day: Int, dateFormat: DateFormat): String

    /**
     * Formats the given [LocalDate] using the given [pattern].
     * Supported patterns:
     * - dd: day of month, 2 digits with leading zeros
     * - d: day of month, no leading zeros
     * - MM: month of year, 2 digits with leading zeros
     * - M: month of year, no leading zeros
     * - MMMM: month of year, full name
     * - yyyy: year, 4 digits
     * - yy: year, last 2 digits
     * @param instant the instant to format
     * @param pattern the pattern to use
     */
    @JsName("formatWithLocalDate")
    fun format(localDate: LocalDate, dateFormat: DateFormat): String

    /**
     * Formats the given [localTime] using the given [timeFormat].
     * Supported patterns:
     * - HH: Hours, 2 digits with leading zeros
     * - mm: Minutes, 2 digits with leading zeros
     * @param localTime the instant to format
     * @param timeFormat the pattern to use
     */
    fun formatTime(localTime: LocalTime, timeFormat: TimeFormat): String
}