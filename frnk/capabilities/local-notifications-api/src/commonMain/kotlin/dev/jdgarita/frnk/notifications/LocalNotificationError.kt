package dev.jdgarita.frnk.notifications

import dev.jdgarita.frnk.utils.AppError

/** Typed [LocalNotificationScheduler] failures — never thrown, always returned in an `AppResult.Failure`. */
enum class LocalNotificationError(
    override val message: String
) : AppError {
    /** Notifications are not allowed (not granted yet, denied, or turned off in system settings). */
    PermissionDenied("Notifications are not allowed"),

    /** The requested time is not in the future, or the id is blank. */
    InvalidTime("Notification time must be in the future"),

    /** The platform scheduler rejected the request. */
    Unknown("Couldn't schedule the notification")
}