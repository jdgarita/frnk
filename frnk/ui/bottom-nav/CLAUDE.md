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

The common item (`FrnkNavBarItem`) and tab (`FrnkAdaptiveNavTab`) carry **two** icon forms because the
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

## Contents (`ui/app/`)

- `FrnkAppShell.kt` — **the one-call app shell** (host-enablement). Stands up a complete tabbed app:
  `FrnkTheme(themeConfig)` wrap, `frnkNavConfiguration(hostRoutes)`, `rememberFrnkBottomNavState`
  (the fixed `Home · feature · Settings` tabs; the center tab supplied as `feature: FrnkFeatureItem`),
  `rememberFrnkTabbedBackStacks`, `FrnkTabbedNavScaffold`, built-in
  **Home** (`HomeScreen` with the host's `homeContent` `ColumnScope` slot; `homeTopBar`/`homeVmKey`/
  `onHomeEffect`), **Settings** (default catalogue + `settingsExtraSections`,
  overridable via the `settingsState`/`settingsEffects` composable factories + `settingsVmKey`),
  optional **Onboarding** (`onboardingPages` → registers `ToolkitRoute.Onboarding`; back/close pops),
  and deep-links (`pendingRoutes: FrnkPendingRouteRequest?`).
  Host extension points — `effects` (the single `EffectCollector` home), `entries`
  (`EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit`; register the **`feature` tab's route** here, and
  **never** re-register the built-in routes — nav3 `require`-throws on duplicates), and the effect
  handlers — all receive the `FrnkAppScope`.
  Assumes **Koin is already started** (`frnkUiModules()` in `:ui-app` carries all scaffold VM modules). `:ui-app`'s
  `FrnkAppScaffold` layers the monetization batteries over this; `:demo-shared` uses the shell
  directly (it can't see `:ui-app`) and is the reference integration.
- `FrnkAppScope.kt` — `@Stable` handle (`tabbed: FrnkTabbedBackStacks` +
  `navigateTo`/`back`/`clearAndNavigateTo`) handed to every shell extension point.

## Contents (`ui/bottomnav/`)

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
- `FrnkTabbedNavScaffold.kt` — `FrnkTabbedNavScaffold(tabbed, tabs, modifier, hideBarFor, entryProvider)`.
  The **nav3 multiple-back-stack** tabbed scaffold: the single composable a host calls to get a standard
  tabbed app. It absorbs the `FrnkNavDisplay` (driven by `tabbed.current`), the persistent
  `FrnkBottomFloatingBar` overlay (one item per tab; tab switch / re-tap-to-root),
  `FrnkTabbedBackHandler` (back-from-non-home-root→home), full-screen bar hiding (`hideBarFor`), and the
  bottom-inset bookkeeping (provides `LocalFrnkBottomBarInset` = the bar's `reservedHeight` while it shows, so
  screens on `FrnkScreenScaffold`/`FrnkMviScreen` reserve it automatically — no per-screen `bottomInset`
  threading). **The host still owns `tabbed`** (`rememberFrnkTabbedBackStacks(tabs = …)`) and the same
  (remembered) `List<FrnkAdaptiveNavTab>`, so it can drive effect-based navigation from its own
  `EffectCollector` — this scaffold structures/renders, the host owns state. `hideBarFor` **defaults to
  `{ it is FrnkFullScreenRoute }`** (the marker in `:core-nav`), so full-screen routes declare the
  intent on themselves rather than the host keeping a predicate in sync with `entryProvider`; override only
  for ad-hoc rules. `entryProvider` defaults to `koinEntryProvider()` (pair with the `navigation<Route>`
  DSL); pass an inline `entryProvider { entry<…> { … } }` when screens share one host-scoped VM (the demo
  does). This is the **Material3 adaptive-bar** scaffold by design; a host that wants multiple-back-stack
  nav3 **without** Material3 hand-wires the primitives instead (`rememberFrnkTabbedBackStacks` +
  `FrnkNavDisplay` + `FrnkTabbedBackHandler` + its own bar).
  `@OptIn(KoinExperimentalAPI::class)` (for the `koinEntryProvider()` default — doesn't propagate to callers
  passing their own provider).
- `FrnkAdaptiveNavTab.kt` / `FrnkFeatureItem.kt` / `FrnkBottomNavState.kt` / `FrnkAdaptiveNavDefaults.kt` —
  the per-tab type (`key` + `root` + `icon: ImageVector` + `iosSystemIcon` SF-Symbol), the host-facing
  center-tab config (`FrnkFeatureItem`: `route` + `label` + `icon` + `iosSystemIcon` + `key`), the view
  state (`FrnkBottomNavState`, `internal` ctor, holding the fixed `home`/`feature`/`settings` tabs +
  `tabs`), and `rememberFrnkBottomNavState(homeRoot, settingsRoot, feature, …)`, which builds the fixed
  three-tab shape: a Home tab, the host's `feature` tab, then a Settings tab (bookend labels from
  `FrnkStrings`, icons from the `iconNavHome` / `iconSettings` theme tokens + `"house"` / `"gearshape"`
  SF-Symbols). The product rule **every app has Home + Settings** is structural — they cannot be omitted.

## Override model

- **Configure the center tab**: pass a `feature: FrnkFeatureItem` to `rememberFrnkBottomNavState` (the
  Home/Settings bookends are fixed), or build the `List<FrnkAdaptiveNavTab>` by hand for a fully custom shape.
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
