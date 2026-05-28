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

// Bottom-nav scaffold strings. The Settings tab reuses [stringSettings].
val stringNavHome = ThemeToken<String>("string_nav_home")

// Settings scaffold strings. Default copy for the settings catalog; hosts override per token.
val stringSettings = ThemeToken<String>("string_settings")
val stringAppearance = ThemeToken<String>("string_appearance")
val stringNotifications = ThemeToken<String>("string_notifications")
val stringSectionSubscription = ThemeToken<String>("string_section_subscription")
val stringSectionSupport = ThemeToken<String>("string_section_support")
val stringSectionLegal = ThemeToken<String>("string_section_legal")
val stringThemeSystem = ThemeToken<String>("string_theme_system")
val stringThemeLight = ThemeToken<String>("string_theme_light")
val stringThemeDark = ThemeToken<String>("string_theme_dark")
val stringUpgradeToPro = ThemeToken<String>("string_upgrade_to_pro")
val stringRestorePurchases = ThemeToken<String>("string_restore_purchases")
val stringManageSubscription = ThemeToken<String>("string_manage_subscription")
val stringShowOnboarding = ThemeToken<String>("string_show_onboarding")
val stringSendFeedback = ThemeToken<String>("string_send_feedback")
val stringRateApp = ThemeToken<String>("string_rate_app")
val stringPrivacyPolicy = ThemeToken<String>("string_privacy_policy")
val stringTermsOfService = ThemeToken<String>("string_terms_of_service")
val stringSettingsFooter = ThemeToken<String>("string_settings_footer")

// Feedback e-mail copy (see rememberFeedbackEmailLauncher). The subject is built as
// "<appName> <stringFeedbackSubject>"; the body hint is the editable prompt above the diagnostics.
val stringFeedbackSubject = ThemeToken<String>("string_feedback_subject")
val stringFeedbackBodyHint = ThemeToken<String>("string_feedback_body_hint")
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
        stringNavHome to "Home",
        stringSettings to "Settings",
        stringAppearance to "Appearance",
        stringNotifications to "Notifications",
        stringSectionSubscription to "Subscription",
        stringSectionSupport to "Support",
        stringSectionLegal to "Legal",
        stringThemeSystem to "System",
        stringThemeLight to "Light",
        stringThemeDark to "Dark",
        stringUpgradeToPro to "Upgrade to Pro",
        stringRestorePurchases to "Restore Purchases",
        stringManageSubscription to "Manage Subscription",
        stringShowOnboarding to "Show Onboarding",
        stringSendFeedback to "Send Feedback",
        stringRateApp to "Rate the App",
        stringPrivacyPolicy to "Privacy Policy",
        stringTermsOfService to "Terms of Service",
        stringSettingsFooter to "Made in 🇨🇷 by JD with",
        stringFeedbackSubject to "Feedback",
        stringFeedbackBodyHint to "Tell us what you think:",
    )
