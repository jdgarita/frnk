package dev.jdgarita.frnk.ui.app

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.bottomnav.FrnkAppInfo
import dev.jdgarita.frnk.ui.bottomnav.FrnkHomeConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkOnboardingConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkSettingsConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavConfig
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig

/**
 * The host-supplied configuration bundle for [FrnkAppScaffold] — the batteries-included superset of
 * [FrnkTabbedNavConfig]. It carries the same feature sub-configs (so the host declares the app's identity,
 * navigation, theme, and built-in tabs once) plus [monetization], the one piece `:ui-bottom-nav` cannot
 * depend on.
 *
 * Same convention as [FrnkTabbedNavConfig]: `*Config` is host-supplied input declared once; runtime,
 * toolkit-owned, mutating state stays in `*State` / `*ViewState` types. The `@Composable` slots and event
 * callbacks remain parameters on [FrnkAppScaffold] rather than fields here.
 *
 * **Build it once and hold it stable** (`remember` it, or hoist it above the composable): [nav]'s
 * `hostRoutes` is a `SerializersModule` with no value equality, so a config reconstructed inline every
 * recomposition compares unequal each frame — it defeats scaffold skipping and rebuilds the nav
 * configuration. See `docs/HOST_INTEGRATION.md` §8.
 *
 * @property app the app's identity ([FrnkAppInfo]) — name + version.
 * @property nav the navigation shape ([FrnkNavConfig]) — the center feature tab, host routes, etc.
 * @property theme the theme token overrides ([FrnkThemeConfig]).
 * @property home the built-in Home tab's configuration ([FrnkHomeConfig]).
 * @property settings the built-in Settings tab's configuration ([FrnkSettingsConfig]).
 * @property onboarding the optional onboarding flow's configuration ([FrnkOnboardingConfig]); when
 *   [FrnkOnboardingConfig.showOnFirstLaunch] is true the flow is auto-presented once on first launch.
 * @property monetization the paywall configuration ([FrnkMonetizationConfig]).
 */
@Immutable
data class FrnkAppConfig(
    val app: FrnkAppInfo,
    val nav: FrnkNavConfig,
    val theme: FrnkThemeConfig = FrnkThemeConfig.Default,
    val home: FrnkHomeConfig = FrnkHomeConfig(),
    val settings: FrnkSettingsConfig = FrnkSettingsConfig(),
    val onboarding: FrnkOnboardingConfig = FrnkOnboardingConfig(),
    val monetization: FrnkMonetizationConfig = FrnkMonetizationConfig()
)

/**
 * The paywall configuration surfaced when monetization modules are installed.
 *
 * @property paywallFeatures the bullet-point feature list shown on the auto-mounted `ToolkitRoute.Paywall`.
 */
@Immutable
data class FrnkMonetizationConfig(
    val paywallFeatures: List<String> = emptyList()
)