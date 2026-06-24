# ui-bottom-nav

The toolkit's **platform-adaptive bottom navigation** module: a Material3 *Expressive*
`HorizontalFloatingToolbar` (floating pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3
bar (older) on iOS, plus the tabbed scaffold + tab builder that wire it up. Depends on `:ui-scaffolds`
(tokens, theme, `EffectCollector`).

## The adaptive bar (`FrnkBottomFloatingBar`)

`FrnkBottomFloatingBar` is an **`expect`/`actual` composable** — each platform renders with its own engine, so
Material3 never leaves Android:
- **Android** (`androidMain/FrnkBottomFloatingBar.android.kt`) — a Material3 *Expressive*
  `HorizontalFloatingToolbar` floating pill, drawing each item's `ImageVector` directly via `IconButton` +
  `Icon`. Renders the items only — no FAB.
- **iOS** (`iosMain/FrnkBottomFloatingBar.ios.kt`) — the toolkit's **vendored** `AdaptiveNavigationBar`
  (under `ui.bottomnav.vendor`, adapted from narendraanjana09's
  [adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)): a native glassy `UITabBar`
  (iOS 26+) / Material3 Compose bar (older iOS), driven by each item's **SF-Symbol** `iosSystemIcon`. Renders
  the items only — no FAB. Vendored so the toolkit owns the bar's `UIKitView` update block (the upstream
  artifact exposed no Compose-driven hook), which re-applies theme changes in place instead of recreating.

The bar is a **fixed three-tab** shape — `Home · feature · Settings`, in that order — shown on every
screen (no FAB, no dynamically-injected item). The **center "feature" tab is the host's only
configurable slot** (`FrnkFeatureItem`: `route` + `label` + `icon: ImageVector` + `iosSystemIcon`); Home
and Settings are toolkit-owned bookends built from theme tokens. It is a **real navigable tab** (own
back stack, selection highlight, re-tap-to-root) — point it at the app's signature surface (a "New X"
flow, a capture screen, the main library) and register its `route` in the host's `entryProvider`.
`rememberFrnkBottomNavState(homeRoot, settingsRoot, feature)` returns the fixed `FrnkBottomNavState`
(`internal` constructor — only `feature` is settable). The old dynamic primary-action / FAB / Mode-B
mechanism (`FrnkNavPrimaryAction`, `FrnkPrimaryActionRegistry`, `FrnkPrimaryActionHandler`,
`onPrimaryAction`) was removed — see `mobiai brain search "fixed three-tab bottom bar"`.

> **History.** Originally one of *two* engines: [Calf](https://github.com/MohamedRejeb/Calf) and
> adaptive-nav-bar, A/B-selectable via `FrnkAdaptiveNavEngine`. Calf was removed when adaptive-nav-bar became
> the default; then the **Android** engine was swapped from adaptive-nav-bar's Material3 `NavigationBar` to a
> Material3 Expressive `HorizontalFloatingToolbar` and the common API moved from `DrawableResource` to
> `ImageVector` (adaptive-nav-bar kept on iOS only, then **vendored** into `ui.bottomnav.vendor`); the FAB was
> retired for a centered bar item (Mode B), and the dynamic primary-action mechanism was later dropped
> entirely for the fixed three-tab `Home · feature · Settings` bar. The spike + decision history is in the
> MobiAI brain (`mobiai brain search "adaptive bottom nav"`).

## Icons: `ImageVector` (Android) + SF-Symbol (iOS)

The common item (`FrnkNavBarItem`) and tab (`FrnkBottomNavTab`) carry **two** icon forms because the
engines consume different things:
- `icon: ImageVector` — a clean shared Compose vector, rendered directly by the Android floating toolbar.
  The Home/Settings bookends default to theme icon tokens (`iconNavHome`/`iconSettings`), host-overridable
  via `FrnkThemeConfig.iconOverrides`. **No `DrawableResource` anywhere on Android** — so there is **no
  host-side asset-packaging step** (the old AGP-9 wart is gone).
- `iosSystemIcon: String` — an SF-Symbol name. The native iOS 26+ `UITabBar` renders a UIKit symbol, not a
  Compose vector, so this identifier stays explicit. `rememberFrnkBottomNavState(...)` supplies `"house"` /
  `"gearshape"` for the bookends; the host gives the center `feature` tab its own.

The vendored bar's `NavigationItem.icon` is a non-null `DrawableResource` (used only on the older-iOS Compose
fallback bar), but the API no longer carries one — so the **iOS** actual feeds the
vendored bar a single bundled placeholder, `commonMain/composeResources/drawable/frnk_nav_placeholder.xml` (in
commonMain because the Compose-resources plugin generates the `Res` accessor there regardless; only the iOS
actual references it). On iOS 26+ it is never shown (the glass bar uses `systemIcon`). `compose.components.resources`
is a commonMain dependency for that generated accessor; no Material3 rides on it. The library adds no native
cinterop, so the XCFrameworks still link under `dynamic_lookup`.

## Why this is its own module

It is the **one place in the toolkit that intentionally takes a Material3 dependency.** The Android bar is a
Material3 Expressive `HorizontalFloatingToolbar`, and the **vendored** iOS bar's older-iOS fallback
(`ComposeNavigationBar`) needs `compose.material3`. Isolating both here keeps Material3 to a single, named module
rather than smeared across the design-system modules `:ui-theme`/`:ui-components`/`:ui-scaffolds` (which stay
Material-free, `compose-unstyled` only). The dependency is **split by source set** — `compose.material3`
(pinned to `1.10.0-alpha05` for `HorizontalFloatingToolbar`) in `androidMain`, `compose.material3` (for the
vendored fallback) + `compose.components.resources` in `iosMain` — but it's all in this one module's `build.gradle.kts`, so the
quarantine holds. `:ui-app` `api`-depends on this module, so **Material3 reaches every consumer of the nav
layer** — a deliberate, host-approved trade.

> **Why `1.10.0-alpha05`?** Compose Multiplatform 1.11.1 resolves `compose.material3` to **1.9.0**, which
> ships only `FloatingToolbarTokens` — the `HorizontalFloatingToolbar` composable (`@ExperimentalMaterial3ExpressiveApi`)
> first appears in `1.10.0-alpha05`. The catalog pins it as `compose-material3-expressive`; bump it (and drop
> the override) once CMP's bundled material3 catches up.

adaptive-nav-bar is pure Kotlin/Compose (no extra native cinterop / SPM package), so the XCFramework still
links under the consumer's existing `-undefined dynamic_lookup`.

## Contents (`ui/bottomnav/`)

> The one-call app shell **`FrnkAppShell` was merged into `FrnkTabbedNavScaffold`** below — there is now a
> single public tabbed-app composable. The two helpers it exposed to hosts moved here too (was the
> module's separate `ui/app/` package, now folded into `…ui.bottomnav` so the module is single-package):

- `FrnkAppScope.kt` — `@Stable` handle (`tabbed: FrnkTabbedBackStacks` +
  `navigateTo`/`back`/`clearAndNavigateTo`) handed to every `FrnkTabbedNavScaffold` extension point.
- `FrnkFirstLaunchOnboardingEffect.kt` — drops into the `effects` slot to present onboarding once on
  first launch (persisted via `:ui-scaffolds`' `rememberOnboardingGate`; session-only when no
  `KeyValueStore` is bound). `:ui-app`'s `FrnkAppScaffold` wires it automatically; hosts on the bare
  `FrnkTabbedNavScaffold` (e.g. the demo) call it themselves.

- `FrnkBottomFloatingBar.kt` (common) — the `expect fun FrnkBottomFloatingBar(items, selectedIndex,
  onItemSelected, modifier)` + `FrnkNavBarItem` (`key`, `icon: ImageVector`, `iosSystemIcon`,
  `label`) + the shared `FrnkNavBarDefaults` (`reservedHeight`, read by `FrnkTabbedNavScaffold` to inset
  content behind the overlaid bar). The bar renders items only — no FAB (the primary action is a centered
  item injected by the scaffold). The two actuals:
  - `FrnkBottomFloatingBar.android.kt` — a Material3 Expressive `HorizontalFloatingToolbar` (floating pill),
    centered at the bottom; each item is an `IconButton` over its `ImageVector` (selected = `colorPrimary`,
    idle = `colorOnSurfaceVariant`). Colors come from
    `FloatingToolbarDefaults.standardFloatingToolbarColors(...)` themed with `FrnkTheme` tokens, not
    `MaterialTheme`. Never touches `DrawableResource`. **Shadow parity:** the plain (no-FAB) overload defaults
    to `ContainerExpandedElevation` (`Level0` = 0.dp, no shadow); it pins `expandedShadowElevation` to
    `ContainerExpandedElevationWithFab` (`Level1`) so the pill still casts the standard floating-toolbar shadow.
  - `FrnkBottomFloatingBar.ios.kt` — the **vendored** `AdaptiveNavigationBar` (`ui.bottomnav.vendor`), themed
    from `FrnkTheme` tokens via `AdaptiveNavigationBarDefaults.colors(...)` (selected = `colorPrimary`,
    unselected = `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`).
    Items render from `iosSystemIcon` + the bundled placeholder drawable. The vendored bar splits its
    `UIKitView` factory/update so selection + theme changes re-apply in place (no recreate/snap) — that
    in-place control is exactly why it was vendored. The bar is full-width and static.
  Most hosts use the scaffold; call the bar directly only when wiring your own selected-tab state /
  navigation. This is the toolkit's sole bottom-nav bar.
- `FrnkTabbedNavScaffold.kt` — **the one-call tabbed app** (host-enablement); the single public composable a
  host calls to get a complete tabbed app. `FrnkTabbedNavScaffold(config: FrnkTabbedNavConfig, …) { homeContent }`
  stands it all up: `FrnkTheme(config.theme)` wrap, `frnkNestedNavConfig(config.nav.hostRoutes)`,
  `rememberFrnkBottomNavState` (the fixed `Home · feature · Settings` tabs; the center tab supplied as
  `config.nav.feature: FrnkFeatureItem`), `rememberFrnkTabbedBackStacks`, built-in **Home** (`HomeScreen` with
  the host's `homeContent` `ColumnScope` slot; `config.home.topBar`/`config.home.vmKey`/`onHomeEffect`),
  **Settings** (default catalogue + `config.settings.extraSections`, overridable via the
  `settingsState`/`settingsEffects` composable factories), optional **Onboarding**
  (`config.onboarding.pages` → registers `FrnkRoute.Onboarding`; back/close pops), and deep-links
  (`pendingRoutes: FrnkPendingRouteRequest?`). The host's declarative input is bundled in
  `config` (`FrnkTabbedNavConfig`); the composable keeps only behaviour as params — the `@Composable`
  slots/factories, the `onHomeEffect` callback, and the runtime controllers (`appearanceController`,
  `pendingRoutes`). Host extension points — `effects` (the single `EffectCollector` home), `entries`
  (`EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit`; register the **`feature` tab's route** here, and
  **never** re-register the built-in routes — nav3 `require`-throws on duplicates), and the effect handlers
  — all receive the `FrnkAppScope`. Assumes **Koin is already started** (`frnkUiModules()` in `:ui-app`
  carries all scaffold VM modules). `:ui-app`'s `FrnkAppScaffold` layers the monetization batteries over
  this; `:demo-shared` uses it directly (it can't see `:ui-app`) and is the reference integration.
  - **Private `TabbedNavHost` render core (same file).** The nav3 multiple-back-stack mechanics live in a
    `private` helper the public composable hands its created `tabbed` + `entryProvider`: the `FrnkNavDisplay`
    (driven by `tabbed.current`), the persistent `FrnkBottomFloatingBar` overlay (one item per tab; tab
    switch / re-tap-to-root), `FrnkTabbedBackHandler` (back-from-non-home-root→home), full-screen bar hiding
    (`hideBarFor`, **defaults to `{ it is FrnkFullScreenRoute }`** — the `:core-nav` marker), and the
    bottom-inset bookkeeping (provides `LocalFrnkBottomBarInset` = the bar's `reservedHeight` while it shows,
    so screens on `FrnkScreenScaffold`/`FrnkMviScreen` reserve it automatically). This is the **Material3
    adaptive-bar** path by design; a host that wants multiple-back-stack nav3 **without** Material3 — or a
    tab shape other than the fixed three — hand-wires the primitives instead (`rememberFrnkTabbedBackStacks`
    + `FrnkNavDisplay` + `FrnkTabbedBackHandler` + its own bar). **History:** `FrnkTabbedNavScaffold` was
    previously the *generic* host-owned-state scaffold (`(tabbed, tabs, hideBarFor, entryProvider)`) and
    `FrnkAppShell` (`ui/app/`) was the opinionated assembly above it; they were merged into this one public
    composable (the generic form became the private render core).
- `FrnkBottomNavTab.kt` / `FrnkFeatureItem.kt` / `FrnkBottomNavState.kt` / `FrnkAdaptiveNavDefaults.kt` —
  the per-tab type (`FrnkBottomNavTab`, a `sealed class` with one subtype per fixed slot — `Home` /
  `Feature` / `Settings` — each carrying `key` + `root` + `icon: ImageVector` + `iosSystemIcon` SF-Symbol;
  it superseded the old `FrnkAdaptiveNavTab` data class so the three-tab shape is typed), the host-facing
  center-tab config (`FrnkFeatureItem`: `route` + `label` + `icon` + `iosSystemIcon` + `key`), the view
  state (`FrnkBottomNavState`, `internal` ctor, holding the fixed `home`/`feature`/`settings` tabs +
  `tabs: List<FrnkBottomNavTab>`), and `rememberFrnkBottomNavState(homeRoot, settingsRoot, feature, …)`,
  which builds the fixed three-tab shape: a Home tab, the host's `feature` tab, then a Settings tab (bookend
  labels from `FrnkStrings`, icons from the `iconNavHome` / `iconSettings` theme tokens + `"house"` /
  `"gearshape"` SF-Symbols). The product rule **every app has Home + Settings** is structural — they cannot
  be omitted.
- `FrnkTabbedNavConfig.kt` — the host's `@Immutable` config bundle for `FrnkTabbedNavScaffold` + its feature
  sub-configs (`FrnkAppInfo`, `FrnkNavConfig`, `FrnkHomeConfig`, `FrnkSettingsConfig`, `FrnkOnboardingConfig`;
  theme reuses `FrnkThemeConfig`). **Convention:** `*Config` = host input declared once; runtime, toolkit-owned
  state stays in `*State`/`*ViewState`. `:ui-app`'s `FrnkAppConfig` is the batteries-included superset.
- `FrnkTabbedNavViewState.kt` — the bar's runtime view state (`navBarItems` + `navBarItemIndexSelected`),
  derived inside the private `TabbedNavHost` from the active tab and fed to `FrnkBottomFloatingBar`.

## Override model

- **Configure the center tab**: pass a `feature: FrnkFeatureItem` to `rememberFrnkBottomNavState` (the
  Home/Settings bookends are fixed), or build the `List<FrnkBottomNavTab>` by hand for a fully custom shape.
- **Wire the navigation**: use `FrnkTabbedNavScaffold` with a host-owned `rememberFrnkTabbedBackStacks` + an
  `entryProvider` — frnk owns the display + bar + tab switching + back convention + bar-inset; the host owns
  the back stacks and registers destinations.

## Dependencies

- `api(projects.uiScaffolds)` — tokens, theme, `EffectCollector`, `LocalFrnkBottomBarInset`.
- `api(compose.runtime / foundation / ui)`.
- `commonMain`: `implementation(compose.components.resources)` — runtime for the generated `Res` accessor
  (the iOS placeholder drawable). Not Material3.
- `androidMain`: `implementation(libs.compose.material3.expressive)` (material3 `1.10.0-alpha05`) — the
  `HorizontalFloatingToolbar`. **The sole Material3 dependency in the toolkit.** Don't add Material3 to any
  other shared module.
- `iosMain`: `implementation(libs.compose.material3.expressive)` — Material3 for the **vendored** bar's
  older-iOS `ComposeNavigationBar` fallback (previously transitive from the dropped `adaptive-nav-bar`
  artifact). The bar engine itself is vendored under `ui.bottomnav.vendor` — no external bar dependency.

## Rules

- Material3 lives **here only**. Other modules (especially `:ui-theme`/`:ui-components`/`:ui-scaffolds`) stay `compose-unstyled`.
- New adaptive-nav surface goes in `ui/bottomnav/` with the same conventions as atoms/scaffolds (`@Immutable`
  state where applicable, callbacks before `modifier`, tokens-only styling).
