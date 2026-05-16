package dev.jdgarita.frnk.ui.theme

import com.composeunstyled.theme.ThemeToken

// region String tokens — toolkit-internal labels the host can override for localization.
val stringSignIn = ThemeToken<String>("string_sign_in")
val stringSignOut = ThemeToken<String>("string_sign_out")
val stringUpgrade = ThemeToken<String>("string_upgrade")
val stringCancel = ThemeToken<String>("string_cancel")
val stringRetry = ThemeToken<String>("string_retry")
val stringConfirm = ThemeToken<String>("string_confirm")
val stringClose = ThemeToken<String>("string_close")
val stringBack = ThemeToken<String>("string_back")
val stringNext = ThemeToken<String>("string_next")
val stringGetStarted = ThemeToken<String>("string_get_started")
val stringSearch = ThemeToken<String>("string_search")
val stringGenericError = ThemeToken<String>("string_generic_error")
// endregion

internal val DefaultFrnkStrings: Map<ThemeToken<String>, String> =
    mapOf(
        stringSignIn to "Sign In",
        stringSignOut to "Sign Out",
        stringUpgrade to "Upgrade",
        stringCancel to "Cancel",
        stringRetry to "Retry",
        stringConfirm to "Confirm",
        stringClose to "Close",
        stringBack to "Back",
        stringNext to "Next",
        stringGetStarted to "Get Started",
        stringSearch to "Search",
        stringGenericError to "Something went wrong.",
    )
