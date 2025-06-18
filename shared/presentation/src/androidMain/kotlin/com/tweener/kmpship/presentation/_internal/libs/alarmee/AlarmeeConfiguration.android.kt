package com.tweener.kmpship.presentation._internal.libs.alarmee

import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.tweener.alarmee._internal.applicationContext
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.kmpship.presentation.R

/**
 * @author Vivien Mahe
 * @since 09/06/2025
 */

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_stat_name,
        notificationIconColor = Color(ContextCompat.getColor(applicationContext, R.color.primary)),
        notificationChannels = listOf(
            // TODO: This is an example channel. Uncomment these lines to create a notification channel and customize it. Add as many channels as needed.
//            AlarmeeNotificationChannel(
//                id = "dailyNewsChannelId",
//                name = applicationContext.getString(R.string.notification_channel_daily_news),
//                importance = NotificationManager.IMPORTANCE_HIGH
//            ),
        )
    )
