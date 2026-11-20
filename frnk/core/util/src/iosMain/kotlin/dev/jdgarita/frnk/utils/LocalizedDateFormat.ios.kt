package dev.jdgarita.frnk.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.Instant

actual fun localizedDateTime(
    instant: Instant,
    skeleton: String
): String {
    val formatter = NSDateFormatter()
    formatter.setLocalizedDateFormatFromTemplate(skeleton)
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(instant.toEpochMilliseconds() / 1000.0))
}