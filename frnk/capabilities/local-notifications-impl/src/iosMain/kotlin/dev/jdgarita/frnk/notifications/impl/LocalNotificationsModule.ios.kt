package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.notifications.LocalNotificationScheduler

internal actual fun platformLocalNotificationScheduler(config: LocalNotificationsConfig): LocalNotificationScheduler =
    IosLocalNotificationScheduler()