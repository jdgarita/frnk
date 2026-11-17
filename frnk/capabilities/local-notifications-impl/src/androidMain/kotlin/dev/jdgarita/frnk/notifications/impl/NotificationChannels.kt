package dev.jdgarita.frnk.notifications.impl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** Idempotent: creating an existing channel only refreshes its name. */
internal fun ensureNotificationChannel(
    context: Context,
    channelId: String,
    channelName: String
) {
    context
        .getSystemService(NotificationManager::class.java)
        ?.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT))
}