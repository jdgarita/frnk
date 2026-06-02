package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.iconFeedback
import dev.jdgarita.frnk.ui.theme.iconManageSubscription
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.iconOnboarding
import dev.jdgarita.frnk.ui.theme.iconPrivacy
import dev.jdgarita.frnk.ui.theme.iconRate
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.iconTerms
import dev.jdgarita.frnk.ui.theme.iconUpgrade
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.stringAppearance
import dev.jdgarita.frnk.ui.theme.stringManageSubscription
import dev.jdgarita.frnk.ui.theme.stringNotifications
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
import dev.jdgarita.frnk.ui.theme.strings

/**
 * Builds a batteries-included [SettingsScreenState] from the toolkit's default catalogue: a theme
 * toggle, an optional notifications switch, a subscription section driven by [isPro] (see below), a
 * support section (Send Feedback, Rate, Show Onboarding), a legal section (Privacy, Terms), and the
 * "Made in 🇨🇷 by JD with ☕ / [version]" footer. All copy and icons resolve from `FrnkStrings` /
 * `FrnkIcons`, so hosts re-skin every label and glyph through `FrnkThemeConfig`.
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
 * @param showNotifications when false, the notifications section is omitted entirely.
 */
@Composable
fun rememberDefaultSettingsState(
    version: String,
    appearance: Appearance,
    isPro: Boolean = false,
    notificationsEnabled: Boolean = true,
    showNotifications: Boolean = true,
    title: String = Theme[strings][stringSettings],
): SettingsScreenState {
    // Resolve every token up front so `remember` can key on the resolved values: if a host swaps a
    // string/icon override, the catalogue rebuilds.
    val labelAppearance = Theme[strings][stringAppearance]
    val labelSystem = Theme[strings][stringThemeSystem]
    val labelLight = Theme[strings][stringThemeLight]
    val labelDark = Theme[strings][stringThemeDark]
    val labelNotifications = Theme[strings][stringNotifications]
    val labelUpgrade = Theme[strings][stringUpgradeToPro]
    val labelManage = Theme[strings][stringManageSubscription]
    val labelRestore = Theme[strings][stringRestorePurchases]
    val labelProMember = Theme[strings][stringProMember]
    val badgePro = Theme[strings][stringProBadge]
    val labelFeedback = Theme[strings][stringSendFeedback]
    val labelRate = Theme[strings][stringRateApp]
    val labelOnboarding = Theme[strings][stringShowOnboarding]
    val labelPrivacy = Theme[strings][stringPrivacyPolicy]
    val labelTerms = Theme[strings][stringTermsOfService]
    val headerSubscription = Theme[strings][stringSectionSubscription]
    val headerSupport = Theme[strings][stringSectionSupport]
    val headerLegal = Theme[strings][stringSectionLegal]
    val footerText = Theme[strings][stringSettingsFooter]

    val iconNotificationsVec = Theme[icons][iconNotifications]
    val iconUpgradeVec = Theme[icons][iconUpgrade]
    val iconManageVec = Theme[icons][iconManageSubscription]
    val iconRestoreVec = Theme[icons][iconRestore]
    val iconFeedbackVec = Theme[icons][iconFeedback]
    val iconRateVec = Theme[icons][iconRate]
    val iconOnboardingVec = Theme[icons][iconOnboarding]
    val iconPrivacyVec = Theme[icons][iconPrivacy]
    val iconTermsVec = Theme[icons][iconTerms]

    return remember(
        version,
        appearance,
        isPro,
        notificationsEnabled,
        showNotifications,
        title,
        labelAppearance,
        labelSystem,
        labelLight,
        labelDark,
        labelNotifications,
        labelUpgrade,
        labelManage,
        labelRestore,
        labelProMember,
        badgePro,
        labelFeedback,
        labelRate,
        labelOnboarding,
        labelPrivacy,
        labelTerms,
        headerSubscription,
        headerSupport,
        headerLegal,
        footerText,
        iconNotificationsVec,
        iconUpgradeVec,
        iconManageVec,
        iconRestoreVec,
        iconFeedbackVec,
        iconRateVec,
        iconOnboardingVec,
        iconPrivacyVec,
        iconTermsVec,
    ) {
        fun rowIcon(vector: ImageVector) = FrnkIconState(imageVector = vector, contentDescription = null, tint = colorPrimary)

        val sections =
            buildList {
                add(
                    SettingsSectionState(
                        rows =
                            listOf(
                                SettingsThemeRowState(
                                    title = labelAppearance,
                                    selected = appearance,
                                    optionLabels = listOf(labelSystem, labelLight, labelDark),
                                ),
                            ),
                    ),
                )
                if (showNotifications) {
                    add(
                        SettingsSectionState(
                            rows =
                                listOf(
                                    SettingsToggleRowState(
                                        id = "notifications",
                                        icon = rowIcon(iconNotificationsVec),
                                        title = labelNotifications,
                                        checked = notificationsEnabled,
                                    ),
                                ),
                        ),
                    )
                }
                add(
                    SettingsSectionState(
                        title = headerSubscription,
                        // Strict Free/Pro visibility matrix:
                        //  - Free → Upgrade-to-Pro (opens the paywall) + Restore Purchases.
                        //  - Pro  → a "Pro Member" status badge + Manage Subscription (deep-links the OS
                        //    subscriptions page). Upgrade and Restore are hidden — dead weight once entitled.
                        rows =
                            if (isPro) {
                                listOf(
                                    SettingsStatusRowState(
                                        id = "pro_member",
                                        icon = rowIcon(iconUpgradeVec),
                                        title = labelProMember,
                                        badge = badgePro,
                                    ),
                                    SettingsClickableRowState(
                                        id = "manage_subscription",
                                        icon = rowIcon(iconManageVec),
                                        title = labelManage,
                                        action = SettingsAction.ManageSubscription,
                                    ),
                                )
                            } else {
                                listOf(
                                    SettingsClickableRowState(
                                        id = "upgrade_to_pro",
                                        icon = rowIcon(iconUpgradeVec),
                                        title = labelUpgrade,
                                        action = SettingsAction.UpgradeToPro,
                                    ),
                                    SettingsClickableRowState(
                                        id = "restore_purchases",
                                        icon = rowIcon(iconRestoreVec),
                                        title = labelRestore,
                                        action = SettingsAction.RestorePurchases,
                                    ),
                                )
                            },
                    ),
                )
                add(
                    SettingsSectionState(
                        title = headerSupport,
                        rows =
                            listOf(
                                SettingsClickableRowState(
                                    id = "send_feedback",
                                    icon = rowIcon(iconFeedbackVec),
                                    title = labelFeedback,
                                    action = SettingsAction.SendFeedback,
                                ),
                                SettingsClickableRowState(
                                    id = "rate_app",
                                    icon = rowIcon(iconRateVec),
                                    title = labelRate,
                                    action = SettingsAction.RateApp,
                                ),
                                SettingsClickableRowState(
                                    id = "show_onboarding",
                                    icon = rowIcon(iconOnboardingVec),
                                    title = labelOnboarding,
                                    action = SettingsAction.ShowOnboarding,
                                ),
                            ),
                    ),
                )
                add(
                    SettingsSectionState(
                        title = headerLegal,
                        rows =
                            listOf(
                                SettingsClickableRowState(
                                    id = "privacy_policy",
                                    icon = rowIcon(iconPrivacyVec),
                                    title = labelPrivacy,
                                    action = SettingsAction.PrivacyPolicy,
                                ),
                                SettingsClickableRowState(
                                    id = "terms_of_service",
                                    icon = rowIcon(iconTermsVec),
                                    title = labelTerms,
                                    action = SettingsAction.TermsOfService,
                                ),
                            ),
                    ),
                )
            }

        SettingsScreenState(
            title = title,
            sections = sections,
            footer = SettingsFooterState(text = footerText, version = version),
        )
    }
}
