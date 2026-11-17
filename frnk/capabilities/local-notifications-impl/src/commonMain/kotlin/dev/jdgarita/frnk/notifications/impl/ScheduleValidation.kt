package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.notifications.LocalNotificationError
import kotlin.time.Instant

/** Shared, pure request validation both platforms run before touching the OS scheduler. */
internal fun validateSchedule(
    id: String,
    at: Instant,
    now: Instant
): LocalNotificationError? = if (id.isBlank() || at <= now) LocalNotificationError.InvalidTime else null