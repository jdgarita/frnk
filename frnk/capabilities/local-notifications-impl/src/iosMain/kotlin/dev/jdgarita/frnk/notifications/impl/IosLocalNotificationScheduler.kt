package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.notifications.LocalNotificationError
import dev.jdgarita.frnk.notifications.LocalNotificationScheduler
import dev.jdgarita.frnk.notifications.NotificationPermissionStatus
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * `UNUserNotificationCenter`-backed scheduler. A calendar trigger at the requested wall-clock instant
 * (the OS persists pending requests across reboots); the request identifier is the frnk id, so
 * re-scheduling an id replaces it. Foreground presentation is the host's call — iOS hides
 * notifications while the app is active unless the host's `UNUserNotificationCenterDelegate` opts in.
 */
internal class IosLocalNotificationScheduler(
    private val clock: Clock = Clock.System
) : LocalNotificationScheduler {
    private val center: UNUserNotificationCenter get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String
    ): AppResult<Unit, LocalNotificationError> {
        validateSchedule(id, at, clock.now())?.let { return AppResult.Failure(it) }
        if (permissionStatus() != NotificationPermissionStatus.Granted) {
            return AppResult.Failure(LocalNotificationError.PermissionDenied)
        }
        val content =
            UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(body)
                setSound(UNNotificationSound.defaultSound())
            }
        val date = NSDate.dateWithTimeIntervalSince1970(at.toEpochMilliseconds() / 1000.0)
        val components =
            NSCalendar.currentCalendar.components(
                NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
                    NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
                fromDate = date
            )
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)
        val added =
            suspendCancellableCoroutine { continuation ->
                center.addNotificationRequest(request) { error ->
                    if (continuation.isActive) continuation.resume(error == null)
                }
            }
        return if (added) AppResult.Success(Unit) else AppResult.Failure(LocalNotificationError.Unknown)
    }

    override suspend fun cancel(id: String): AppResult<Unit, LocalNotificationError> {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(id))
        return AppResult.Success(Unit)
    }

    override suspend fun permissionStatus(): NotificationPermissionStatus =
        suspendCancellableCoroutine { continuation ->
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val status = settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined
                if (continuation.isActive) continuation.resume(status.toPermissionStatus())
            }
        }

    override suspend fun requestPermission(): NotificationPermissionStatus {
        // iOS prompts once; afterwards the call just reports the stored decision.
        val current = permissionStatus()
        if (current != NotificationPermissionStatus.NotDetermined) return current
        suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { _, _ ->
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
        return permissionStatus()
    }
}

private fun UNAuthorizationStatus.toPermissionStatus(): NotificationPermissionStatus =
    when (this) {
        UNAuthorizationStatusAuthorized,
        UNAuthorizationStatusProvisional,
        UNAuthorizationStatusEphemeral -> NotificationPermissionStatus.Granted
        UNAuthorizationStatusDenied -> NotificationPermissionStatus.Denied
        else -> NotificationPermissionStatus.NotDetermined
    }