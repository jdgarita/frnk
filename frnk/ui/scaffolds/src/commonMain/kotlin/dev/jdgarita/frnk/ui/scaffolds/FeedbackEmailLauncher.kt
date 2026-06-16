package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.stringFeedbackBodyHint
import dev.jdgarita.frnk.ui.theme.stringFeedbackSubject
import dev.jdgarita.frnk.ui.theme.strings
import dev.jdgarita.frnk.utils.FeedbackEmail

/**
 * Returns a callback that opens the user's default mail composer prefilled with a feedback e-mail —
 * a subject, a short prompt, and an app/OS/device diagnostics block (see [FeedbackEmail]). It is
 * pure Compose Multiplatform: it builds a `mailto:` URI and hands it to [LocalUriHandler], so it
 * works on Android and iOS with no platform wiring. Wire the returned lambda to
 * [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction.SendFeedback] from a [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreen] `onEffect` handler.
 *
 * Subject and prompt default to `FrnkStrings` tokens, so hosts re-skin them through
 * `FrnkThemeConfig.stringOverrides`; pass [subject] / [bodyHint] to override directly. [recipient]
 * defaults to [FeedbackEmail.DEFAULT_RECIPIENT] — override it to route feedback to the host's inbox.
 *
 * @param appName host app's display name, shown in the subject and diagnostics.
 * @param appVersion host app's version string, shown in the diagnostics block.
 */
@Composable
fun rememberFeedbackEmailLauncher(
    appName: String,
    appVersion: String,
    recipient: String = FeedbackEmail.DEFAULT_RECIPIENT,
    subject: String = "$appName ${Theme[strings][stringFeedbackSubject]}",
    bodyHint: String = Theme[strings][stringFeedbackBodyHint],
): () -> Unit {
    val uriHandler = LocalUriHandler.current
    return remember(appName, appVersion, recipient, subject, bodyHint, uriHandler) {
        {
            val draft =
                FeedbackEmail.draft(
                    appName = appName,
                    appVersion = appVersion,
                    recipient = recipient,
                    subject = subject,
                    bodyHint = bodyHint,
                )
            // openUri throws if no app can handle mailto: (e.g. a bare emulator with no mail
            // client). Swallow it so a missing mail app can never crash the host.
            runCatching { uriHandler.openUri(draft.toMailtoUri()) }
        }
    }
}
