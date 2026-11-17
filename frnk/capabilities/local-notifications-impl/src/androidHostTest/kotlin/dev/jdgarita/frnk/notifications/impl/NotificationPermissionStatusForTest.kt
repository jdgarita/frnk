package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.notifications.NotificationPermissionStatus.Denied
import dev.jdgarita.frnk.notifications.NotificationPermissionStatus.Granted
import dev.jdgarita.frnk.notifications.NotificationPermissionStatus.NotDetermined
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationPermissionStatusForTest {
    @Test
    fun pre_13_follows_the_app_notification_setting() {
        assertEquals(Granted, notificationPermissionStatusFor(32, true, notificationsEnabled = true, requestedBefore = false))
        assertEquals(Denied, notificationPermissionStatusFor(32, true, notificationsEnabled = false, requestedBefore = false))
    }

    @Test
    fun api_33_never_asked_is_not_determined() {
        assertEquals(NotDetermined, notificationPermissionStatusFor(33, false, notificationsEnabled = false, requestedBefore = false))
    }

    @Test
    fun api_33_asked_and_refused_is_denied() {
        assertEquals(Denied, notificationPermissionStatusFor(33, false, notificationsEnabled = false, requestedBefore = true))
    }

    @Test
    fun api_33_granted_but_switched_off_in_settings_is_denied() {
        assertEquals(Denied, notificationPermissionStatusFor(34, true, notificationsEnabled = false, requestedBefore = true))
    }

    @Test
    fun api_33_granted_and_enabled_is_granted() {
        assertEquals(Granted, notificationPermissionStatusFor(34, true, notificationsEnabled = true, requestedBefore = false))
    }
}