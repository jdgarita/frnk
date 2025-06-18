package dev.jdgarita.frnk.presentation._internal.launcher

import io.github.aakira.napier.Napier
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.UIKit.UIApplication

/**
 * @author Vivien Mahe
 * @since 09/02/2025
 */
class EmailComposerIosLauncher : EmailComposerLauncher {

    override fun composeEmail(recipients: List<String>, subject: String, body: String) {
        val recipientList = recipients.joinToString(",") // Combine recipients with commas
        val subjectEncoded = subject.encodeForURL()
        val bodyEncoded = body.encodeForURL()
        val mailtoUrl = "mailto:$recipientList?subject=$subjectEncoded&body=$bodyEncoded"

        NSURL.URLWithString(mailtoUrl)?.let { url ->
            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url, options = mapOf<Any?, Any?>()) { success ->
                    Napier.d { "Email client opened: $success" }
                }
            }
        }
    }

    private fun String.encodeForURL(): String {
        val queryItem = NSURLQueryItem(name = "key", value = this)
        val components = NSURLComponents().apply {
            queryItems = listOf(queryItem)
        }
        return components.percentEncodedQuery?.substringAfter("=") ?: this
    }
}
