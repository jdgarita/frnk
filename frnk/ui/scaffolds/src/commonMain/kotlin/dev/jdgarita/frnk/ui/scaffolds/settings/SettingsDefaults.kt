package dev.jdgarita.frnk.ui.scaffolds.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.jdgarita.frnk.ui.haptics.HAPTICS_TOGGLE_ID
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.iconFeedback
import dev.jdgarita.frnk.ui.theme.iconHaptics
import dev.jdgarita.frnk.ui.theme.iconManageSubscription
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.iconOnboarding
import dev.jdgarita.frnk.ui.theme.iconPrivacy
import dev.jdgarita.frnk.ui.theme.iconRate
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.iconTerms
import dev.jdgarita.frnk.ui.theme.iconUpgrade
import dev.jdgarita.frnk.ui.theme.stringAppearance
import dev.jdgarita.frnk.ui.theme.stringHaptics
import dev.jdgarita.frnk.ui.theme.stringHapticsSubtitle
import dev.jdgarita.frnk.ui.theme.stringManageSubscription
import dev.jdgarita.frnk.ui.theme.stringNotifications
import dev.jdgarita.frnk.ui.theme.stringPreferences
import dev.jdgarita.frnk.ui.theme.stringPrivacyPolicy
import dev.jdgarita.frnk.ui.theme.stringProBadge
import dev.jdgarita.frnk.ui.theme.stringProMember
import dev.jdgarita.frnk.ui.theme.stringRateApp
import dev.jdgarita.frnk.ui.theme.stringRestorePurchases
import dev.jdgarita.frnk.ui.theme.stringSectionLegal
import dev.jdgarita.frnk.ui.theme.stringSectionSubscription
import dev.jdgarita.frnk.ui.theme.stringSectionSupport
import dev.jdgarita.frnk.ui.theme.stringSendFeedback
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.stringSettingsFooter
import dev.jdgarita.frnk.ui.theme.stringShowOnboarding
import dev.jdgarita.frnk.ui.theme.stringTermsOfService
import dev.jdgarita.frnk.ui.theme.stringThemeDark
import dev.jdgarita.frnk.ui.theme.stringThemeLight
import dev.jdgarita.frnk.ui.theme.stringThemeSystem
import dev.jdgarita.frnk.ui.theme.stringUpgradeToPro

/**
 * Where [defaultSettingsState]'s `extraSections` slot into the default catalogue order
 * (Appearance → Preferences → Subscription → Support → Legal).
 */
enum class SettingsExtraSectionsPlacement {
    /** Directly after the Appearance section (above Preferences). */
    AfterAppearance,

    /** Between Preferences and the Subscription section. */
    BeforeSubscription,

    /** Between Support and the Legal section — app-specific rows above the legal boilerplate. */
    BeforeLegal,

    /** After every default section (below Legal, above the footer). */
    End,
}

/**
 * Builds a batteries-included [SettingsScreenState] from the toolkit's default catalogue: a theme
 * toggle, an optional notifications switch, a subscription section driven by [isPro] (see below), a
 * support section (Send Feedback, Rate, Show Onboarding), a legal section (Privacy, Terms), and the
 * "Built by JD in 🇨🇷 / [version]" footer.
 *
 * **Token-in-state.** All copy and icons are emitted as theme-token [FrnkStringSource]/[FrnkIconSource]
 * references — resolved to real strings/glyphs by the leaf atoms at render time, so hosts re-skin every
 * label and glyph through `FrnkThemeConfig`. Because no token is resolved here, this is a **plain
 * function** (not `@Composable`): the ViewModel can build/rebuild its own catalogue without composition,
 * and the state is locale-/override-independent (it re-resolves automatically when the theme changes).
 *
 * The subscription section follows a strict Free/Pro visibility matrix:
 *  - **Free** ([isPro] = false): "Upgrade to Pro" (opens the paywall) + "Restore Purchases".
 *  - **Pro** ([isPro] = true): a non-interactive "Pro Member" status badge + "Manage Subscription"
 *    (the host deep-links the OS subscriptions page). Upgrade and Restore are hidden.
 *
 * Hosts that need a different set of rows can ignore this and assemble [SettingsScreenState] by hand
 * — this is a convenience, not the only entry point.
 *
 * @param version the host app's version string, rendered verbatim in the footer (e.g. "v1.2.0 (42)").
 * @param appearance the currently-selected theme, reflected in the segmented control.
 * @param isPro switches the subscription section between the Free and Pro layouts (see above).
 * @param notificationsEnabled initial checked state of the notifications toggle.
 * @param hapticsEnabled initial checked state of the haptic-feedback toggle. The host resolves this
 *   from the live [LocalFrnkHaptics][dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics] (a composition
 *   read) and passes it in, since this builder is no longer composable.
 * @param showNotifications when false, the notifications section is omitted entirely.
 * @param showHaptics when false, the haptic-feedback toggle is omitted entirely.
 * @param extraSections host-specific sections injected into the default catalogue at
 *   [extraSectionsPlacement]. Custom rows flow through the existing contract:
 *   `SettingsAction.Custom(id)` on a clickable row surfaces as `SettingsEffect.ActionInvoked`.
 * @param extraSectionsPlacement where [extraSections] slot into the default order; defaults to
 *   [SettingsExtraSectionsPlacement.BeforeLegal] (app-specific rows above the legal boilerplate).
 */
fun defaultSettingsState(
    version: String,
    appearance: Appearance,
    isPro: Boolean = false,
    notificationsEnabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    showNotifications: Boolean = true,
    showHaptics: Boolean = true,
    title: FrnkStringSource = FrnkStringSource.Token(stringSettings),
    extraSections: List<SettingsSectionState> = emptyList(),
    extraSectionsPlacement: SettingsExtraSectionsPlacement = SettingsExtraSectionsPlacement.BeforeLegal,
): SettingsScreenState {
    val sections =
        buildList {
            fun addExtrasAt(placement: SettingsExtraSectionsPlacement) {
                if (extraSectionsPlacement == placement) addAll(extraSections)
            }
            add(
                SettingsSectionState(
                    rows =
                        listOf(
                            SettingsThemeRowState(
                                title = FrnkStringSource.Token(stringAppearance),
                                selected = appearance,
                                optionLabels =
                                    listOf(
                                        FrnkStringSource.Token(stringThemeSystem),
                                        FrnkStringSource.Token(stringThemeLight),
                                        FrnkStringSource.Token(stringThemeDark),
                                    ),
                            ),
                        ),
                ),
            )
            addExtrasAt(SettingsExtraSectionsPlacement.AfterAppearance)
            if (showNotifications || showHaptics) {
                add(
                    SettingsSectionState(
                        title = FrnkStringSource.Token(stringPreferences),
                        rows =
                            buildList {
                                if (showNotifications) {
                                    add(
                                        SettingsToggleRowState(
                                            id = "notifications",
                                            icon = FrnkIconSource.Token(iconNotifications),
                                            title = FrnkStringSource.Token(stringNotifications),
                                            checked = notificationsEnabled,
                                        ),
                                    )
                                }
                                if (showHaptics) {
                                    add(
                                        SettingsToggleRowState(
                                            id = HAPTICS_TOGGLE_ID,
                                            icon = FrnkIconSource.Token(iconHaptics),
                                            title = FrnkStringSource.Token(stringHaptics),
                                            subtitle = FrnkStringSource.Token(stringHapticsSubtitle),
                                            checked = hapticsEnabled,
                                        ),
                                    )
                                }
                            },
                    ),
                )
            }
            addExtrasAt(SettingsExtraSectionsPlacement.BeforeSubscription)
            add(
                SettingsSectionState(
                    title = FrnkStringSource.Token(stringSectionSubscription),
                    // Strict Free/Pro visibility matrix:
                    //  - Free → Upgrade-to-Pro (opens the paywall) + Restore Purchases.
                    //  - Pro  → a "Pro Member" status badge + Manage Subscription (deep-links the OS
                    //    subscriptions page). Upgrade and Restore are hidden — dead weight once entitled.
                    rows =
                        if (isPro) {
                            listOf(
                                SettingsStatusRowState(
                                    id = "pro_member",
                                    icon = FrnkIconSource.Token(iconUpgrade),
                                    title = FrnkStringSource.Token(stringProMember),
                                    badge = FrnkStringSource.Token(stringProBadge),
                                ),
                                SettingsClickableRowState(
                                    id = "manage_subscription",
                                    icon = FrnkIconSource.Token(iconManageSubscription),
                                    title = FrnkStringSource.Token(stringManageSubscription),
                                    action = SettingsAction.ManageSubscription,
                                ),
                            )
                        } else {
                            listOf(
                                SettingsClickableRowState(
                                    id = "upgrade_to_pro",
                                    icon = FrnkIconSource.Token(iconUpgrade),
                                    title = FrnkStringSource.Token(stringUpgradeToPro),
                                    action = SettingsAction.UpgradeToPro,
                                ),
                                SettingsClickableRowState(
                                    id = "restore_purchases",
                                    icon = FrnkIconSource.Token(iconRestore),
                                    title = FrnkStringSource.Token(stringRestorePurchases),
                                    action = SettingsAction.RestorePurchases,
                                ),
                            )
                        },
                ),
            )
            add(
                SettingsSectionState(
                    title = FrnkStringSource.Token(stringSectionSupport),
                    rows =
                        listOf(
                            SettingsClickableRowState(
                                id = "send_feedback",
                                icon = FrnkIconSource.Token(iconFeedback),
                                title = FrnkStringSource.Token(stringSendFeedback),
                                action = SettingsAction.SendFeedback,
                            ),
                            SettingsClickableRowState(
                                id = "rate_app",
                                icon = FrnkIconSource.Token(iconRate),
                                title = FrnkStringSource.Token(stringRateApp),
                                action = SettingsAction.RateApp,
                            ),
                            SettingsClickableRowState(
                                id = "show_onboarding",
                                icon = FrnkIconSource.Token(iconOnboarding),
                                title = FrnkStringSource.Token(stringShowOnboarding),
                                action = SettingsAction.ShowOnboarding,
                            ),
                        ),
                ),
            )
            addExtrasAt(SettingsExtraSectionsPlacement.BeforeLegal)
            add(
                SettingsSectionState(
                    title = FrnkStringSource.Token(stringSectionLegal),
                    rows =
                        listOf(
                            SettingsClickableRowState(
                                id = "privacy_policy",
                                icon = FrnkIconSource.Token(iconPrivacy),
                                title = FrnkStringSource.Token(stringPrivacyPolicy),
                                action = SettingsAction.PrivacyPolicy,
                            ),
                            SettingsClickableRowState(
                                id = "terms_of_service",
                                icon = FrnkIconSource.Token(iconTerms),
                                title = FrnkStringSource.Token(stringTermsOfService),
                                action = SettingsAction.TermsOfService,
                            ),
                        ),
                ),
            )
            addExtrasAt(SettingsExtraSectionsPlacement.End)
        }

    return SettingsScreenState(
        title = title,
        sections = sections,
        footer = SettingsFooterState(text = FrnkStringSource.Token(stringSettingsFooter), version = FrnkStringSource.Raw(version)),
    )
}

/**
 * `@Composable` convenience over [defaultSettingsState]: seeds the haptic-feedback toggle from the live
 * ambient [LocalFrnkHaptics] (a composition read) and memoizes the result with [remember] so the catalogue
 * only rebuilds when an input changes — the stable `config` [SyncMviConfig][dev.jdgarita.frnk.ui.mvi.SyncMviConfig]
 * expects.
 *
 * This is now a thin wrapper: the substantive builder ([defaultSettingsState]) is composition-free, so a
 * ViewModel/Koin factory can build the catalogue without composition. Reach for this wrapper only when
 * building the state *inside* a composable that wants the live haptics state baked in.
 */
@Composable
fun rememberDefaultSettingsState(
    version: String,
    appearance: Appearance,
    isPro: Boolean = false,
    notificationsEnabled: Boolean = true,
    showNotifications: Boolean = true,
    showHaptics: Boolean = true,
    title: FrnkStringSource = FrnkStringSource.Token(stringSettings),
    extraSections: List<SettingsSectionState> = emptyList(),
    extraSectionsPlacement: SettingsExtraSectionsPlacement = SettingsExtraSectionsPlacement.BeforeLegal,
): SettingsScreenState {
    // Seed from the live haptics state so the row reflects reality on first paint; runtime flips are
    // owned by SettingsViewModel + applied by rememberFrnkSettingsHandler. .value (not a lifecycle
    // collect) is enough — the VM owns the row's checked state after the first reduction.
    val hapticsEnabled = LocalFrnkHaptics.current.isEnabled.value
    return remember(
        version,
        appearance,
        isPro,
        notificationsEnabled,
        hapticsEnabled,
        showNotifications,
        showHaptics,
        title,
        extraSections,
        extraSectionsPlacement,
    ) {
        defaultSettingsState(
            version = version,
            appearance = appearance,
            isPro = isPro,
            notificationsEnabled = notificationsEnabled,
            hapticsEnabled = hapticsEnabled,
            showNotifications = showNotifications,
            showHaptics = showHaptics,
            title = title,
            extraSections = extraSections,
            extraSectionsPlacement = extraSectionsPlacement,
        )
    }
}
