package dev.jdgarita.frnk.notifications.impl

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import dev.jdgarita.frnk.notifications.NotificationPermissionStatus

/**
 * Posts one scheduled local notification. Self-contained — everything it needs travels in its input
 * data — so it runs under WorkManager's default factory without Koin, including after a reboot.
 *
 * Public only so hosts can read [EXTRA_NOTIFICATION_ID] off a tapped notification's launch intent;
 * the constructor stays internal (WorkManager reaches it reflectively), so hosts never build one.
 */
class LocalNotificationWorker internal constructor(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        // Permission revoked since scheduling: drop it rather than retry forever.
        if (currentPermissionStatus(applicationContext) != NotificationPermissionStatus.Granted) return Result.success()
        val channelId = inputData.getString(KEY_CHANNEL_ID) ?: return Result.failure()
        ensureNotificationChannel(applicationContext, channelId, inputData.getString(KEY_CHANNEL_NAME) ?: channelId)
        val smallIcon = inputData.getInt(KEY_SMALL_ICON, 0).takeIf { it != 0 } ?: applicationContext.applicationInfo.icon
        val notification =
            Notification
                .Builder(applicationContext, channelId)
                .setSmallIcon(smallIcon)
                .setContentTitle(inputData.getString(KEY_TITLE))
                .setContentText(inputData.getString(KEY_BODY))
                .setStyle(Notification.BigTextStyle().bigText(inputData.getString(KEY_BODY)))
                .setAutoCancel(true)
                .apply { launchIntent(id)?.let(::setContentIntent) }
                .build()
        applicationContext.getSystemService(NotificationManager::class.java)?.notify(id, NOTIFICATION_ID, notification)
        return Result.success()
    }

    /** Tapping opens the app's launcher activity, with the notification id as [EXTRA_NOTIFICATION_ID]. */
    private fun launchIntent(id: String): PendingIntent? {
        val intent =
            applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName) ?: return null
        intent.putExtra(EXTRA_NOTIFICATION_ID, id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            applicationContext,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        /** Intent extra carrying the tapped notification's id into the host's launcher activity. */
        const val EXTRA_NOTIFICATION_ID = "dev.jdgarita.frnk.notifications.ID"
        internal const val NOTIFICATION_ID = 0x0F12
        internal const val KEY_ID = "id"
        internal const val KEY_TITLE = "title"
        internal const val KEY_BODY = "body"
        internal const val KEY_CHANNEL_ID = "channel_id"
        internal const val KEY_CHANNEL_NAME = "channel_name"
        internal const val KEY_SMALL_ICON = "small_icon"
    }
}