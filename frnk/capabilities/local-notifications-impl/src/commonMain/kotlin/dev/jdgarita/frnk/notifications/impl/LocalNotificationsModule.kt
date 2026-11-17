package dev.jdgarita.frnk.notifications.impl

import dev.jdgarita.frnk.notifications.LocalNotificationScheduler
import org.koin.dsl.module

/**
 * Binds the platform [LocalNotificationScheduler] (WorkManager on Android, `UNUserNotificationCenter`
 * on iOS) and its [LocalNotificationsConfig]. No push, no backend, no native SDK beyond the OS.
 *
 * Android needs `DatabaseContext.application` set — `initializeFrnk(context, …)` does it.
 */
val localNotificationsModule =
    module {
        single { LocalNotificationsConfig() }
        single<LocalNotificationScheduler> { platformLocalNotificationScheduler(get()) }
    }

internal expect fun platformLocalNotificationScheduler(config: LocalNotificationsConfig): LocalNotificationScheduler