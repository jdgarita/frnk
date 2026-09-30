package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Immutable

/** One of the paywall footer's legal links. */
enum class PaywallLegalLink { Terms, Privacy }

/**
 * The host's Terms of Use and Privacy Policy pages. Handed to the paywall, they turn its
 * "Terms · Privacy" footer into links opened through the ambient `LocalUriHandler` (provide your own
 * `UriHandler` to open them another way, e.g. in an in-app browser or with a failure message).
 * Without them the footer stays plain text.
 */
@Immutable
data class PaywallLegalLinks(
    val termsUrl: String,
    val privacyUrl: String
) {
    fun urlFor(link: PaywallLegalLink): String =
        when (link) {
            PaywallLegalLink.Terms -> termsUrl
            PaywallLegalLink.Privacy -> privacyUrl
        }
}