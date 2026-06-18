package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * The host-supplied configuration bundle for [FrnkTabbedNavScaffold], grouped into one `@Immutable`
 * sub-config per feature area. It replaces the scaffold's long flat parameter list — the host declares
 * *what the app is* here and reserves the composable's remaining parameters for *behaviour* (the
 * `@Composable` slots and event callbacks, which cannot live in an `@Immutable` bundle without breaking
 * its stability contract).
 *
 * **Naming convention.** `*Config` is reserved for host-supplied input that is declared once and rarely
 * changes (this bundle, [FrnkThemeConfig]); `*State` / `*ViewState` is reserved for state the toolkit
 * itself owns and mutates at runtime (e.g. [FrnkTabbedNavViewState], the MVI `UiState`s). Keep the two
 * apart when adding new types.
 *
 * `:ui-app`'s `FrnkAppConfig` is the batteries-included superset (it adds monetization); it carries the
 * same sub-configs and maps down to a [FrnkTabbedNavConfig].
 *
 * **Build it once and hold it stable** — `remember` it (or hoist it above the composable). Although this
 * bundle is `@Immutable`, [FrnkNavConfig.hostRoutes] is a [SerializersModule] with no value equality (and
 * [FrnkNavConfig.feature] should likewise be stable), so a fresh config constructed inline on every
 * recomposition compares unequal each frame: the scaffold stops skipping **and** the internal
 * `remember(nav.hostRoutes) { frnkNavConfiguration(...) }` re-runs, rebuilding the saved-state
 * configuration behind the per-tab back stacks. See `docs/HOST_INTEGRATION.md` §8 for the canonical
 * remembered-config call.
 *
 * @property app the app's identity ([FrnkAppInfo]) — name + version.
 * @property nav the navigation shape ([FrnkNavConfig]) — the center feature tab, host routes, etc.
 * @property theme the theme token overrides ([FrnkThemeConfig]).
 * @property home the built-in Home tab's configuration ([FrnkHomeConfig]).
 * @property settings the built-in Settings tab's configuration ([FrnkSettingsConfig]).
 * @property onboarding the optional onboarding flow's configuration ([FrnkOnboardingConfig]).
 */
@Immutable
data class FrnkTabbedNavConfig(
    val app: FrnkAppInfo,
    val nav: FrnkNavConfig,
    val theme: FrnkThemeConfig = FrnkThemeConfig.Default,
    val home: FrnkHomeConfig = FrnkHomeConfig(),
    val settings: FrnkSettingsConfig = FrnkSettingsConfig(),
    val onboarding: FrnkOnboardingConfig = FrnkOnboardingConfig()
)

/**
 * The app's identity, surfaced in the Settings footer and the feedback-email draft.
 *
 * @property name the app's display name (also the default Home top-bar title).
 * @property version the app's version string (e.g. `"v1.0.0"`).
 */
@Immutable
data class FrnkAppInfo(
    val name: String,
    val version: String
)

/**
 * The navigation shape: the one host-configurable center tab plus the route/serialization plumbing the
 * scaffold needs. Home and Settings are toolkit-owned bookends (see [FrnkBottomNavState]); the host only
 * configures the [feature] tab and its own routes.
 *
 * @property feature the bar's center tab ([FrnkFeatureItem]) — point it at the app's signature surface.
 *   Pass a stable (remembered) instance; a freshly-built item each frame makes the scaffold
 *   non-skippable.
 * @property hostRoutes the host's `@Serializable` `NavKey` polymorphic module, registered alongside the
 *   toolkit's routes for saved-state. A [SerializersModule] has **no value equality**, so build it once
 *   (`remember`/hoist the enclosing config) — a fresh module each recomposition rebuilds the nav
 *   configuration and defeats scaffold skipping.
 * @property hideBarFor predicate returning `true` for routes that should hide the bottom bar (full-screen
 *   pushes). Defaults to hiding on [FrnkFullScreenRoute]; `remember` a custom one if it captures state.
 * @property homeRoot the Home tab's back-stack root destination.
 * @property settingsRoot the Settings tab's back-stack root destination.
 */
@Immutable
data class FrnkNavConfig(
    val feature: FrnkFeatureItem,
    val hostRoutes: SerializersModule = EmptySerializersModule(),
    val hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
    val homeRoot: NavKey = ToolkitRoute.Home,
    val settingsRoot: NavKey = ToolkitRoute.Settings
)

/**
 * The built-in Home tab's configuration.
 *
 * @property topBar the Home tab's top bar; when `null` the scaffold uses [FrnkAppInfo.name] as the title.
 * @property vmKey seed key for the Home `MviViewModel`; pass a fresh value to re-seed when a dynamic
 *   [topBar] must rebuild (the VM is seeded once via `parametersOf`).
 */
@Immutable
data class FrnkHomeConfig(
    val topBar: FrnkTopAppBarState? = null,
    val vmKey: String? = null
)

/**
 * The built-in Settings tab's configuration.
 *
 * @property extraSections host-injected Settings sections appended to the default catalogue.
 * @property vmKey seed key for the Settings `MviViewModel`; pass a fresh value to re-seed when the
 *   catalogue must rebuild (e.g. when entitlement state flips Free↔Pro).
 */
@Immutable
data class FrnkSettingsConfig(
    val extraSections: List<SettingsSectionState> = emptyList(),
    val vmKey: String? = null
)

/**
 * The optional onboarding flow's configuration. When [pages] is empty no onboarding destination is
 * registered.
 *
 * @property pages the onboarding pages; empty disables onboarding entirely.
 * @property showOnFirstLaunch whether the flow is auto-presented once on first launch. Honoured by
 *   `:ui-app`'s `FrnkAppScaffold` (which wires the first-launch effect automatically); a host on the
 *   bare [FrnkTabbedNavScaffold] reads it to gate its own `FrnkFirstLaunchOnboardingEffect`.
 */
@Immutable
data class FrnkOnboardingConfig(
    val pages: List<OnboardingPageState> = emptyList(),
    val showOnFirstLaunch: Boolean = true
)