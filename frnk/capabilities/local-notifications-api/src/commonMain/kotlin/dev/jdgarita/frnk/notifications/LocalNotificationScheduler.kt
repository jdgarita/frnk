package dev.jdgarita.frnk.notifications

import dev.jdgarita.frnk.utils.AppResult
import kotlin.time.Instant

/**
 * Schedules **local** (on-device, no push, no backend) one-shot notifications — e.g. a reminder
 * before a free trial ends. Bound by `localNotificationsModule` (`:local-notifications-impl`):
 * WorkManager + a notification channel on Android, `UNUserNotificationCenter` on iOS.
 *
 * Delivery time is **inexact** on Android (WorkManager, no exact-alarm permission): the OS may
 * defer it under Doze, so schedule with slack rather than to the minute. Scheduled notifications
 * survive app restarts and device reboots on both platforms.
 *
 * Every method returns [AppResult] (or a status); nothing throws.
 */
interface LocalNotificationScheduler {
    /**
     * Schedule a notification showing [title] / [body] at [at]. Re-using an [id] replaces the
     * notification previously scheduled under it, so hosts can reschedule idempotently (e.g. on
     * every launch from the current trial expiry).
     *
     * Fails with [LocalNotificationError.PermissionDenied] when notifications are not allowed —
     * call [requestPermission] first — and [LocalNotificationError.InvalidTime] when [at] is not in
     * the future.
     */
    suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String
    ): AppResult<Unit, LocalNotificationError>

    /** Cancel the pending (and remove the delivered) notification scheduled under [id]. Unknown ids succeed. */
    suspend fun cancel(id: String): AppResult<Unit, LocalNotificationError>

    /** Whether the app may post notifications right now, without prompting. */
    suspend fun permissionStatus(): NotificationPermissionStatus

    /**
     * Ask the user for notification permission (Android 13+ `POST_NOTIFICATIONS` / iOS
     * authorization) and return the resulting status. Returns at once when already granted. A
     * user who denied is re-prompted only where the platform allows it: iOS asks once; Android 13+
     * shows the dialog again until the user has denied twice. Android 12 and below has no runtime
     * prompt — the result reflects the app's notification setting.
     */
    suspend fun requestPermission(): NotificationPermissionStatus
}