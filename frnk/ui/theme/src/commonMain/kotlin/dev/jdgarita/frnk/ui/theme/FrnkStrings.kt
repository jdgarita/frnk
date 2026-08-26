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
val stringPreferences = ThemeToken<String>("string_preferences")
val stringHaptics = ThemeToken<String>("string_haptics")
val stringHapticsSubtitle = ThemeToken<String>("string_haptics_subtitle")
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
val stringProMember = ThemeToken<String>("string_pro_member")
val stringProBadge = ThemeToken<String>("string_pro_badge")
val stringShowOnboarding = ThemeToken<String>("string_show_onboarding")
val stringSendFeedback = ThemeToken<String>("string_send_feedback")
val stringRateApp = ThemeToken<String>("string_rate_app")
val stringPrivacyPolicy = ThemeToken<String>("string_privacy_policy")
val stringTermsOfService = ThemeToken<String>("string_terms_of_service")
val stringSettingsFooter = ThemeToken<String>("string_settings_footer")

// Paywall strings (shared-monetization-ui). Title is composed as "<prefix> <appName> <proName>",
// e.g. "Upgrade to Still Pro" — each piece is its own token for localization.
val stringAppName = ThemeToken<String>("string_app_name")
val stringPaywallTitlePrefix = ThemeToken<String>("string_paywall_title_prefix")
val stringProName = ThemeToken<String>("string_pro_name")
val stringPaywallContinue = ThemeToken<String>("string_paywall_continue")
val stringPaywallStartTrial = ThemeToken<String>("string_paywall_start_trial")
val stringPaywallFreeTrialBadge = ThemeToken<String>("string_paywall_free_trial_badge")
val stringPaywallTerms = ThemeToken<String>("string_paywall_terms")
val stringPaywallPrivacy = ThemeToken<String>("string_paywall_privacy")
val stringPaywallEmpty = ThemeToken<String>("string_paywall_empty")
val stringPaywallRestoring = ThemeToken<String>("string_paywall_restoring")
val stringPaywallRestored = ThemeToken<String>("string_paywall_restored")
val stringPaywallNothingToRestore = ThemeToken<String>("string_paywall_nothing_to_restore")
val stringPaywallAlreadyOwnedRestoring = ThemeToken<String>("string_paywall_already_owned_restoring")
val stringPaywallIdentityError = ThemeToken<String>("string_paywall_identity_error")
val stringPerMonthSuffix = ThemeToken<String>("string_per_month_suffix")
val stringGodMode = ThemeToken<String>("string_god_mode")
val stringGodModeSubtitle = ThemeToken<String>("string_god_mode_subtitle")
val stringDeveloper = ThemeToken<String>("string_developer")

// Feedback e-mail copy (see rememberFeedbackEmailLauncher). The subject is built as
// "<appName> <stringFeedbackSubject>"; the body hint is the editable prompt above the diagnostics.
val stringFeedbackSubject = ThemeToken<String>("string_feedback_subject")
val stringFeedbackBodyHint = ThemeToken<String>("string_feedback_body_hint")

// Search-mode a11y labels for FrnkTopAppBar's close/clear buttons.
val stringSearchClose = ThemeToken<String>("string_search_close")
val stringSearchClear = ThemeToken<String>("string_search_clear")

// User-facing texts for the MonetizationError vocabulary (see MonetizationError.toStringSource()
// in :shared-monetization-ui) — kept as tokens so billing failures localize/override like any
// other toolkit copy instead of leaking the enum's diagnostic `message`. `Unknown` maps to
// [stringGenericError] rather than a token of its own.
val stringErrorPurchaseCancelled = ThemeToken<String>("string_error_purchase_cancelled")
val stringErrorStoreUnavailable = ThemeToken<String>("string_error_store_unavailable")
val stringErrorNetworkUnavailable = ThemeToken<String>("string_error_network_unavailable")
val stringErrorNoOfferings = ThemeToken<String>("string_error_no_offerings")
val stringErrorPurchaseNotAllowed = ThemeToken<String>("string_error_purchase_not_allowed")
val stringErrorAlreadyOwned = ThemeToken<String>("string_error_already_owned")
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
        stringPreferences to "Preferences",
        stringHaptics to "Haptic feedback",
        stringHapticsSubtitle to "Vibrate on interactions",
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
        stringProMember to "Pro Member",
        stringProBadge to "PRO",
        stringShowOnboarding to "Show Onboarding",
        stringSendFeedback to "Send Feedback",
        stringRateApp to "Rate the App",
        stringPrivacyPolicy to "Privacy Policy",
        stringTermsOfService to "Terms of Service",
        stringSettingsFooter to "Built by JD in 🇨🇷",
        stringFeedbackSubject to "Feedback",
        stringFeedbackBodyHint to "Tell us what you think:",
        stringAppName to "frnk",
        stringPaywallTitlePrefix to "Upgrade to",
        stringProName to "Pro",
        stringPaywallContinue to "Continue",
        stringPaywallStartTrial to "Start free trial",
        stringPaywallFreeTrialBadge to "FREE TRIAL",
        stringPaywallTerms to "Terms",
        stringPaywallPrivacy to "Privacy",
        stringPaywallEmpty to "No plans available right now.",
        stringPaywallRestoring to "Restoring…",
        stringPaywallRestored to "Purchases restored",
        stringPaywallNothingToRestore to "Nothing to restore",
        stringPaywallAlreadyOwnedRestoring to "Already subscribed — restoring…",
        stringPaywallIdentityError to "Couldn't verify your account — check your connection and try again",
        stringPerMonthSuffix to "/mo",
        stringGodMode to "God mode",
        stringGodModeSubtitle to "Force Pro on this device (testing)",
        stringDeveloper to "Developer",
        stringSearchClose to "Close search",
        stringSearchClear to "Clear search",
        stringErrorPurchaseCancelled to "Purchase cancelled",
        stringErrorStoreUnavailable to "Store unavailable",
        stringErrorNetworkUnavailable to "Network unavailable",
        stringErrorNoOfferings to "No products available",
        stringErrorPurchaseNotAllowed to "Purchases are not allowed on this device",
        stringErrorAlreadyOwned to "Already subscribed"
    )

/**
 * Spanish catalog, applied as an **overlay** on [DefaultFrnkStrings] by [frnkStringsFor] — a token
 * missing here falls back to English rather than to a blank, so adding a token without its ES
 * translation degrades gracefully. Brand-like values ([stringAppName], [stringProName]) stay
 * identical on purpose.
 */
internal val EsFrnkStrings: Map<ThemeToken<String>, String> =
    mapOf(
        stringSignIn to "Iniciar sesión",
        stringSignOut to "Cerrar sesión",
        stringUpgrade to "Mejorar",
        stringCancel to "Cancelar",
        stringRetry to "Reintentar",
        stringConfirm to "Confirmar",
        stringClose to "Cerrar",
        stringBack to "Atrás",
        stringNext to "Siguiente",
        stringGetStarted to "Comenzar",
        stringSearch to "Buscar",
        stringGenericError to "Algo salió mal.",
        stringNavHome to "Inicio",
        stringSettings to "Ajustes",
        stringAppearance to "Apariencia",
        stringPreferences to "Preferencias",
        stringHaptics to "Vibración háptica",
        stringHapticsSubtitle to "Vibrar al interactuar",
        stringNotifications to "Notificaciones",
        stringSectionSubscription to "Suscripción",
        stringSectionSupport to "Soporte",
        stringSectionLegal to "Legal",
        stringThemeSystem to "Sistema",
        stringThemeLight to "Claro",
        stringThemeDark to "Oscuro",
        stringUpgradeToPro to "Mejorar a Pro",
        stringRestorePurchases to "Restaurar compras",
        stringManageSubscription to "Gestionar suscripción",
        stringProMember to "Miembro Pro",
        stringProBadge to "PRO",
        stringShowOnboarding to "Ver introducción",
        stringSendFeedback to "Enviar comentarios",
        stringRateApp to "Calificar la app",
        stringPrivacyPolicy to "Política de privacidad",
        stringTermsOfService to "Términos del servicio",
        stringSettingsFooter to "Hecho por JD en 🇨🇷",
        stringFeedbackSubject to "Comentarios",
        stringFeedbackBodyHint to "Cuéntanos qué piensas:",
        stringPaywallTitlePrefix to "Mejora a",
        stringPaywallContinue to "Continuar",
        stringPaywallStartTrial to "Iniciar prueba gratis",
        stringPaywallFreeTrialBadge to "PRUEBA GRATIS",
        stringPaywallTerms to "Términos",
        stringPaywallPrivacy to "Privacidad",
        stringPaywallEmpty to "No hay planes disponibles en este momento.",
        stringPaywallRestoring to "Restaurando…",
        stringPaywallRestored to "Compras restauradas",
        stringPaywallNothingToRestore to "Nada que restaurar",
        stringPaywallAlreadyOwnedRestoring to "Ya estás suscrito — restaurando…",
        stringPaywallIdentityError to "No pudimos verificar tu cuenta — revisa tu conexión e inténtalo de nuevo",
        stringPerMonthSuffix to "/mes",
        stringGodMode to "Modo dios",
        stringGodModeSubtitle to "Forzar Pro en este dispositivo (pruebas)",
        stringDeveloper to "Desarrollador",
        stringSearchClose to "Cerrar búsqueda",
        stringSearchClear to "Borrar búsqueda",
        stringErrorPurchaseCancelled to "Compra cancelada",
        stringErrorStoreUnavailable to "Tienda no disponible",
        stringErrorNetworkUnavailable to "Sin conexión",
        stringErrorNoOfferings to "No hay productos disponibles",
        stringErrorPurchaseNotAllowed to "Las compras no están permitidas en este dispositivo",
        stringErrorAlreadyOwned to "Ya estás suscrito"
    )

/**
 * The default catalog for [language]: English as the base, other languages overlaid on it so a
 * missing translation falls back to English per token. Host `stringOverrides` are applied on top
 * of this by `FrnkTheme`.
 */
internal fun frnkStringsFor(language: FrnkLanguage): Map<ThemeToken<String>, String> =
    when (language) {
        FrnkLanguage.En -> DefaultFrnkStrings
        FrnkLanguage.Es -> DefaultFrnkStrings + EsFrnkStrings
    }