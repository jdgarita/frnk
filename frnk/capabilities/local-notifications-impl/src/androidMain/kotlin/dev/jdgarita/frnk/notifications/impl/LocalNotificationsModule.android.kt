package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.di.DatabaseContext
import dev.jdgarita.frnk.notifications.LocalNotificationScheduler

internal actual fun platformLocalNotificationScheduler(config: LocalNotificationsConfig): LocalNotificationScheduler =
    AndroidLocalNotificationScheduler(DatabaseContext.application, config)