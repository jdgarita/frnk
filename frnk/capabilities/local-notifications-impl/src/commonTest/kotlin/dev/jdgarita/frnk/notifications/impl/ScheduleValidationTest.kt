package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.notifications.LocalNotificationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class ScheduleValidationTest {
    private val now = Instant.fromEpochMilliseconds(1_800_000_000_000)

    @Test
    fun future_time_with_id_is_valid() {
        assertNull(validateSchedule("trial", now + 1.milliseconds, now))
    }

    @Test
    fun now_or_past_is_invalid() {
        assertEquals(LocalNotificationError.InvalidTime, validateSchedule("trial", now, now))
        assertEquals(LocalNotificationError.InvalidTime, validateSchedule("trial", now - 1.milliseconds, now))
    }

    @Test
    fun blank_id_is_invalid() {
        assertEquals(LocalNotificationError.InvalidTime, validateSchedule(" ", now + 1.milliseconds, now))
    }
}