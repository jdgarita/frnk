package dev.jdgarita.frnk.notifications.impl

/**
 * Host knobs for `localNotificationsModule`. Override the binding (Koin `allowOverride`) to change them.
 *
 * @property androidChannelId / [androidChannelName] the Android notification channel every local
 *   notification posts to (created on first schedule; the name is user-visible in system settings,
 *   so hosts should pass localized copy).
 * @property androidSmallIcon a monochrome drawable resource id for the status-bar icon; `0` falls back
 *   to the app's launcher icon. Ignored on iOS, which always uses the app icon.
 */
data class LocalNotificationsConfig(
    val androidChannelId: String = "frnk_reminders",
    val androidChannelName: String = "Reminders",
    val androidSmallIcon: Int = 0
)