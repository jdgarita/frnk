package com.tweener.kmpship.presentation._internal.launcher

import androidx.compose.runtime.Composable
import io.github.aakira.napier.Napier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * @author Vivien Mahe
 * @since 14/11/2024
 */
class EmailClientIosLauncher : EmailClientLauncher {

    @Composable
    override fun openEmailClient() {
        NSURL.URLWithString("message://")?.let { url ->
            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url, options = mapOf<Any?, Any?>()) { success ->
                    Napier.d { "Email client opened: $success" }
                }
            }
        }
    }
}

actual fun createEmailClientLauncher(): EmailClientLauncher = EmailClientIosLauncher()
