package dev.jdgarita.frnk.util.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

data class Formatters(
    val dateTimeFormatter: MultiplatformDateTimeFormatter,
    val phoneNumberFormatter: PhoneNumberFormatter,
    val numberFormatter: FrnkNumberFormatter
) {
    companion object {
        val Fake: Formatters = Formatters(
            object : MultiplatformDateTimeFormatter {
                override fun format(
                    instant: Instant,
                    dateFormat: DateFormat,
                    timeZone: TimeZone
                ): String = ""

                override fun format(
                    year: Int,
                    month: Int,
                    day: Int,
                    dateFormat: DateFormat
                ): String = ""

                override fun format(localDate: LocalDate, dateFormat: DateFormat): String = ""
                override fun formatTime(localTime: LocalTime, timeFormat: TimeFormat): String = ""
            },
            object : PhoneNumberFormatter {
                override fun formatPhoneNumber(phoneNumber: String): String = ""
            },
            object : FrnkNumberFormatter {
                override fun formatNumber(number: Int): String = ""
            }
        )
    }
}