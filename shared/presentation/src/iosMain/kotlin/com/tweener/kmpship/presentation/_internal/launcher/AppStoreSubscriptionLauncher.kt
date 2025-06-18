package com.tweener.kmpship.presentation._internal.launcher

import io.github.aakira.napier.Napier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * @author Vivien Mahe
 * @since 21/01/2025
 */
class AppStoreSubscriptionLauncher : MobileStoreSubscriptionLauncher {

    override fun open(packageName: String, productId: String?) {
        val url = NSURL(string = "https://apps.apple.com/account/subscriptions")
        UIApplication.sharedApplication.openURL(url, options = mapOf<Any?, Any?>()) { success ->
            Napier.d { "App Store opened: $success" }
        }
    }
}
