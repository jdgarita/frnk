package com.tweener.kmpship._internal.libs.alarmee

import com.tweener.alarmee.PushNotificationServiceRegistry

/**
 * @author Vivien Mahe
 * @since 09/06/2025
 */
class AlarmeeHelper {

    fun onNotificationReceived(userInfo: Map<Any?, *>?) {
        val parsed = userInfo
            ?.mapNotNull { (key, value) ->
                val k = key?.toString()
                val v = value?.toString()
                if (k != null && v != null) k to v else null
            }
            ?.toMap()
            ?: emptyMap()

        PushNotificationServiceRegistry.get()?.onMessageReceived(data = parsed)
    }
}
