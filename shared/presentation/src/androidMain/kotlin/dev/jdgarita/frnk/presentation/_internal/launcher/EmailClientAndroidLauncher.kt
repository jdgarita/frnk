package dev.jdgarita.frnk.presentation._internal.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.aakira.napier.Napier

/**
 * @author Vivien Mahe
 * @since 14/11/2024
 */

class EmailClientAndroidLauncher : EmailClientLauncher {

    @Composable
    override fun openEmailClient() {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            try {
                val emailIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_EMAIL)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(emailIntent)
            } catch (exception: ActivityNotFoundException) {
                Napier.e(exception) { "No email client installed on the device!" }
            }
        }
    }
}

actual fun createEmailClientLauncher(): EmailClientLauncher = EmailClientAndroidLauncher()
