package dev.jdgarita.frnk.notifications

import dev.jdgarita.frnk.utils.AppResult
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * In-memory [LocalNotificationScheduler] for host tests (the `FakeAnalyticsTracker` pattern): records
 * what is pending, honours [permission], and applies the contract's validation so a test exercises
 * the same failure paths the real impl returns.
 */
class FakeLocalNotificationScheduler(
    var permission: NotificationPermissionStatus = NotificationPermissionStatus.Granted,
    /** What [requestPermission] grants when the user is asked ([NotificationPermissionStatus.NotDetermined]). */
    var grantOnRequest: Boolean = true,
    private val now: () -> Instant = { Clock.System.now() }
) : LocalNotificationScheduler {
    data class Scheduled(
        val at: Instant,
        val title: String,
        val body: String
    )

    val pending = linkedMapOf<String, Scheduled>()
    var permissionRequests = 0
        private set

    override suspend fun schedule(
        id: String,
        at: Instant,
        title: String,
        body: String
    ): AppResult<Unit, LocalNotificationError> {
        if (permission != NotificationPermissionStatus.Granted) return AppResult.Failure(LocalNotificationError.PermissionDenied)
        if (id.isBlank() || at <= now()) return AppResult.Failure(LocalNotificationError.InvalidTime)
        pending[id] = Scheduled(at, title, body)
        return AppResult.Success(Unit)
    }

    override suspend fun cancel(id: String): AppResult<Unit, LocalNotificationError> {
        pending.remove(id)
        return AppResult.Success(Unit)
    }

    override suspend fun permissionStatus(): NotificationPermissionStatus = permission

    override suspend fun requestPermission(): NotificationPermissionStatus {
        permissionRequests++
        if (permission == NotificationPermissionStatus.NotDetermined) {
            permission = if (grantOnRequest) NotificationPermissionStatus.Granted else NotificationPermissionStatus.Denied
        }
        return permission
    }
}