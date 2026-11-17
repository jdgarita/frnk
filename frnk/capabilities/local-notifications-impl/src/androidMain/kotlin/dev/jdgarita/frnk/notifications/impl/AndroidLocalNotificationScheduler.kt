package dev.jdgarita.frnk.notifications.impl

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import dev.jdgarita.frnk.notifications.LocalNotificationError
import dev.jdgarita.frnk.notifications.LocalNotificationScheduler
import dev.jdgarita.frnk.notifications.NotificationPermissionStatus
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

/**
 * WorkManager-backed scheduler: one unique `OneTimeWorkRequest` per id (REPLACE), delayed until the
 * requested instant. WorkManager persists its queue and reschedules after reboot itself, and needs
 * no exact-alarm permission — at the cost of inexact (Doze-deferrable) delivery.
 * [LocalNotificationWorker] posts the notification on [LocalNotificationsConfig.androidChannelId].
 */
internal class AndroidLocalNotificationScheduler(
    context: Context,
    private val config: LocalNotificationsConfig,
    private val clock: Clock = Clock.System
) : LocalNotificationScheduler {
    private val context = context.applicationContext

    override suspend fun schedule(
        id: String,
        at: kotlin.time.Instant,
        title: String,
        body: String
    ): AppResult<Unit, LocalNotificationError> {
        val now = clock.now()
        validateSchedule(id, at, now)?.let { return AppResult.Failure(it) }
        if (permissionStatus() != NotificationPermissionStatus.Granted) {
            return AppResult.Failure(LocalNotificationError.PermissionDenied)
        }
        return guarded {
            ensureNotificationChannel(context, config.androidChannelId, config.androidChannelName)
            val request =
                OneTimeWorkRequestBuilder<LocalNotificationWorker>()
                    .setInitialDelay((at - now).inWholeMilliseconds, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            LocalNotificationWorker.KEY_ID to id,
                            LocalNotificationWorker.KEY_TITLE to title,
                            LocalNotificationWorker.KEY_BODY to body,
                            LocalNotificationWorker.KEY_CHANNEL_ID to config.androidChannelId,
                            LocalNotificationWorker.KEY_CHANNEL_NAME to config.androidChannelName,
                            LocalNotificationWorker.KEY_SMALL_ICON to config.androidSmallIcon
                        )
                    ).addTag(WORK_TAG)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(uniqueWorkName(id), ExistingWorkPolicy.REPLACE, request)
                .await()
        }
    }

    override suspend fun cancel(id: String): AppResult<Unit, LocalNotificationError> =
        guarded {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(id)).await()
            context.getSystemService(NotificationManager::class.java)?.cancel(id, LocalNotificationWorker.NOTIFICATION_ID)
        }

    override suspend fun permissionStatus(): NotificationPermissionStatus = currentPermissionStatus(context)

    override suspend fun requestPermission(): NotificationPermissionStatus {
        // Below 13 there is no runtime prompt; a granted app needs none.
        val current = currentPermissionStatus(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || current == NotificationPermissionStatus.Granted) {
            return current
        }
        NotificationPermissionActivity.request(context)
        markRequested(context)
        return currentPermissionStatus(context)
    }

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit, LocalNotificationError> =
        try {
            block()
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            AppResult.Failure(LocalNotificationError.Unknown)
        }

    private companion object {
        const val WORK_TAG = "frnk.local_notification"

        fun uniqueWorkName(id: String) = "frnk.local_notification.$id"
    }
}

private const val PREFS = "frnk_local_notifications"
private const val KEY_REQUESTED = "permission_requested"

internal fun currentPermissionStatus(context: Context): NotificationPermissionStatus {
    val sdk = Build.VERSION.SDK_INT
    val granted =
        sdk < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val enabled = context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() ?: false
    val requested = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_REQUESTED, false)
    return notificationPermissionStatusFor(sdk, granted, enabled, requested)
}

private fun markRequested(context: Context) {
    context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_REQUESTED, true)
        .apply()
}

/**
 * Pure status decision. Android can't tell "never asked" from "denied" by itself, so a persisted
 * "we asked" flag separates [NotificationPermissionStatus.NotDetermined] from
 * [NotificationPermissionStatus.Denied] on 13+. Notifications switched off in system settings count
 * as denied whatever the runtime permission says.
 */
internal fun notificationPermissionStatusFor(
    sdkInt: Int,
    runtimePermissionGranted: Boolean,
    notificationsEnabled: Boolean,
    requestedBefore: Boolean
): NotificationPermissionStatus =
    when {
        sdkInt < Build.VERSION_CODES.TIRAMISU ->
            if (notificationsEnabled) NotificationPermissionStatus.Granted else NotificationPermissionStatus.Denied
        runtimePermissionGranted && notificationsEnabled -> NotificationPermissionStatus.Granted
        runtimePermissionGranted || requestedBefore -> NotificationPermissionStatus.Denied
        else -> NotificationPermissionStatus.NotDetermined
    }