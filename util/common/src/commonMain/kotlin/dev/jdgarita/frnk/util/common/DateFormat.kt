package dev.jdgarita.frnk.util.common

import kotlinx.serialization.Serializable

/**
 * Frnk supported date formats.
 * If you need to add a new one - make sure to also add a test to [DefaultDateFormatterTest]
 */
@Serializable(with = DateFormatSerializer::class)
sealed class DateFormat(val format: String) {
    data object MonthDaySingleDigit : DateFormat("M/d") // 7/31

    data object MonthDayDoubleDigit : DateFormat("MM/dd") // 07/31

    data object MonthDayYearSingleDigit : DateFormat("M/d/yy") // 7/31/23

    data object MonthDayYearDoubleDigit : DateFormat("MM/dd/yyyy") // 07/01/2023

    data object ThreeLetterMonthDoubleDigitDayFullYear : DateFormat("MMM dd, yyyy") // Jul 07, 2023

    data object FullMonthDoubleDigitDayFullYear : DateFormat("MMMM dd, yyyy") // July 07, 2023

    data object FullMonthDoubleDigitDay : DateFormat("MMMM dd") // July 07
}