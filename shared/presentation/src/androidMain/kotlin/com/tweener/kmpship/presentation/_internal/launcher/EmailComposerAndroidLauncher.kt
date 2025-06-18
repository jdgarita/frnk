package com.tweener.kmpship.presentation._internal.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.aakira.napier.Napier

/**
 * @author Vivien Mahe
 * @since 09/02/2025
 */
class EmailComposerAndroidLauncher(private val context: Context) : EmailComposerLauncher {

    override fun composeEmail(recipients: List<String>, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Only email apps handle this.
                putExtra(Intent.EXTRA_EMAIL, recipients.toTypedArray())
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (throwable: Throwable) {
            Napier.e(throwable) { "Couldn't open email composer on the device!" }
        }
    }
}
