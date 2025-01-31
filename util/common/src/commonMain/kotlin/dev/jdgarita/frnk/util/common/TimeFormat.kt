package dev.jdgarita.frnk.util.common

sealed class TimeFormat(val format: String) {
    data object DoubleDigitAMPM : TimeFormat(format = "HH:mm") // 06:25 AM, 03:46 PM
}