package dev.jdgarita.frnk.notifications

import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** Pins the contract the fake mirrors, so host tests built on it see the real impl's semantics. */
class FakeLocalNotificationSchedulerTest {
    private val now = Instant.fromEpochMilliseconds(1_800_000_000_000)

    @Test
    fun schedule_replaces_same_id_and_cancel_removes_it() =
        runTest {
            val scheduler = FakeLocalNotificationScheduler(now = { now })
            scheduler.schedule("trial", now + 1.days, "A", "a")
            scheduler.schedule("trial", now + 2.days, "B", "b")
            assertEquals(mapOf("trial" to FakeLocalNotificationScheduler.Scheduled(now + 2.days, "B", "b")), scheduler.pending)
            assertEquals(AppResult.Success(Unit), scheduler.cancel("trial"))
            assertTrue(scheduler.pending.isEmpty())
            assertEquals(AppResult.Success(Unit), scheduler.cancel("unknown"))
        }

    @Test
    fun schedule_without_permission_fails() =
        runTest {
            val scheduler = FakeLocalNotificationScheduler(permission = NotificationPermissionStatus.NotDetermined, now = { now })
            assertEquals(
                AppResult.Failure(LocalNotificationError.PermissionDenied),
                scheduler.schedule("trial", now + 1.days, "t", "b")
            )
        }

    @Test
    fun schedule_in_the_past_fails() =
        runTest {
            val scheduler = FakeLocalNotificationScheduler(now = { now })
            assertEquals(AppResult.Failure(LocalNotificationError.InvalidTime), scheduler.schedule("trial", now, "t", "b"))
        }

    @Test
    fun request_permission_only_prompts_when_not_determined() =
        runTest {
            val scheduler = FakeLocalNotificationScheduler(permission = NotificationPermissionStatus.NotDetermined, grantOnRequest = false)
            assertEquals(NotificationPermissionStatus.Denied, scheduler.requestPermission())
            // A denied user is not re-prompted into Granted.
            scheduler.grantOnRequest = true
            assertEquals(NotificationPermissionStatus.Denied, scheduler.requestPermission())
        }
}