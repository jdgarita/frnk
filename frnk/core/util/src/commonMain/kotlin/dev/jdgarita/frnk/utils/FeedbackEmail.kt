package dev.jdgarita.frnk.utils

/**
 * A platform-agnostic e-mail draft. Render it to a `mailto:` URI with [toMailtoUri] and hand that to
 * a `UriHandler` (or any platform launcher) to open the user's default mail composer prefilled.
 */
data class EmailDraft(
    val recipient: String,
    val subject: String,
    val body: String,
) {
    /** RFC-6068 `mailto:` URI with percent-encoded subject and body. */
    fun toMailtoUri(): String =
        buildString {
            append("mailto:")
            append(recipient)
            append("?subject=")
            append(encodeMailtoComponent(subject))
            append("&body=")
            append(encodeMailtoComponent(body))
        }
}

/**
 * Builds the toolkit's default "Send Feedback" e-mail: a short prompt for the user, followed by a
 * diagnostics block (app, OS, device) that support teams usually ask for. Pure Kotlin — the host (or
 * a `UriHandler`) decides how to open the resulting [EmailDraft].
 *
 * @param appName host app's display name, shown in the subject and the diagnostics block.
 * @param appVersion host app's version string (e.g. "1.2.0 (42)").
 * @param recipient destination address; defaults to [DEFAULT_RECIPIENT], overridable per host.
 * @param subject e-mail subject; defaults to "<appName> Feedback".
 * @param bodyHint the editable prompt placed above the diagnostics block.
 */
object FeedbackEmail {
    /** Toolkit author's inbox — the default destination when a host does not supply its own. */
    const val DEFAULT_RECIPIENT: String = "hello@jdgarita.dev"

    fun draft(
        appName: String,
        appVersion: String,
        recipient: String = DEFAULT_RECIPIENT,
        subject: String = "$appName Feedback",
        bodyHint: String = "Tell us what you think:",
    ): EmailDraft {
        val body =
            buildString {
                appendLine(bodyHint)
                appendLine()
                appendLine()
                appendLine("---")
                appendLine("App: $appName $appVersion")
                appendLine("OS: ${PlatformInfo.osName} ${PlatformInfo.osVersion}")
                append("Device: ${PlatformInfo.deviceModel}")
            }
        return EmailDraft(recipient = recipient, subject = subject, body = body)
    }
}

private const val UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
private val HEX = "0123456789ABCDEF".toCharArray()

/** Percent-encodes [value] (as UTF-8) for use in a `mailto:` query component. */
private fun encodeMailtoComponent(value: String): String =
    buildString {
        for (byte in value.encodeToByteArray()) {
            val code = byte.toInt() and 0xFF
            val char = code.toChar()
            if (char in UNRESERVED) {
                append(char)
            } else {
                append('%')
                append(HEX[code shr 4])
                append(HEX[code and 0x0F])
            }
        }
    }
